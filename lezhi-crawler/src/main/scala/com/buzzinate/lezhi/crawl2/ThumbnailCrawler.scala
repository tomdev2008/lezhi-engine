package com.buzzinate.lezhi.crawl2

import collection.JavaConverters._
import com.buzzinate.lezhi.crawl.Vars
import org.buzzinate.lezhi.api.Client
import com.buzzinate.http.{ImageResp, Http, DefaultHttpScheduler}
import java.util.concurrent.Executors
import com.buzzinate.lezhi.zk.ZkCluster
import com.buzzinate.crawl.{CrawlStream, FrontierClient}
import com.twitter.util.Future
import com.buzzinate.imagescaling.FastResampleOp
import com.mortennobel.imagescaling.AdvancedResizeOp
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import com.buzzinate.lezhi.util.{Loggable, ImageUploader}
import com.buzzinate.lezhi.store.{HTableUtil, HbaseTable}

object ThumbnailCrawler extends Loggable {
  val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")
  val vars = new Vars(prop.asInstanceOf[java.util.Map[String, String]].asScala.toMap)

  val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)
  val frontierClient = new FrontierClient(prop.getProperty("thumbnail.frontier.host", "localhost"))
  val client = new Client(vars.elasticsearchHosts.asJava)
  val http = new DefaultHttpScheduler(Http.buildAgent(200, 2), Executors.newFixedThreadPool(64))
  
  val pool = HTableUtil.createHTablePool(vars.hbaseZookeeperQuorum, 1000)
  val metadata = new HbaseTable(pool, vars.crawlTable, "metadata")

  def main(args: Array[String]): Unit = {
    val zkconn = prop.getProperty("zookeeper.hosts", "localhost:2181")
    val parentPath = prop.getProperty("thumbnail.zkpath", "/crawlers/thumbnail")
    val nodeId = prop.getProperty("node.id", ZkCluster.nodeName)
    val source = new FrontierSource(frontierClient.client, client, new ZkCluster(zkconn, parentPath+"/"+ nodeId), false)

    val ts = CrawlStream.from(source).mapFuture { case UrlItem(url, id, item) =>
      item.meta.get("custom.thumbnail").map { customThumb =>
        info(url + " => " + customThumb)
        crawlImage(customThumb, url).map { thumb =>
        (url, thumb)
        }
      }.getOrElse {
        Future.value(url, Thumbnail.Null)
      }
    }.mapFuture { case (url, thumb) =>
      if (thumb != Thumbnail.Null) {
        ImageUploader.upload(thumb.imgsrc, url, thumb.format, thumb.data).map { uf =>
          (url, Some(thumb.imgsrc, uf))
        }
      } else {
        Future.value((url, None))
      }
    }.map { case (url, uf) =>
      uf.map { case (imgsrc, thumbsrc) =>
        info("upload: " + url + " => " + thumbsrc)
        client.update(url, "thumbnail", thumbsrc)
        (url, imgsrc, thumbsrc)
      }.getOrElse {
        (url, "", "")
      }
    }.batch(20, 1000 * 600) { case results =>
      val metaRows = results.map { case (url, imgsrc, thumbsrc) =>
        url -> Map("imgSrc" -> imgsrc, "thumbnail" -> thumbsrc)
      }
      info("Save thumbnail meta#: " + metaRows)
      metadata.putStrRows(metaRows.toList)
    }.commit

    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run(): Unit = {
        pool.close
        client.close
      }
    })

    source.start
  }

  def crawlImage(imgsrc: String, url: String): Future[Thumbnail] = {
    http.get(imgsrc, true, Some(url)).map { httpresp =>
      val ImageResp(format, img, time) = httpresp.toImage
      if (isGoodThumbnail(img.getWidth, img.getHeight)) {
        val w = img.getWidth
        val h = img.getHeight
        val cut = if (w > h) img.getSubimage((w - h) / 2, 0, h, h) else img.getSubimage(0, 0, w, w)
        val resampleOp = new FastResampleOp(threadPool, 100, 100)
        resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Normal)
        val small = resampleOp.filter(cut, null)
        val baos = new ByteArrayOutputStream
        ImageIO.write(small, format, baos)
        Thumbnail(imgsrc, baos.toByteArray, format)
      } else Thumbnail.Null
    }
  }

  @inline
  def isGoodThumbnail(width: Int, height: Int) = width >= 200 && height >= 150
}