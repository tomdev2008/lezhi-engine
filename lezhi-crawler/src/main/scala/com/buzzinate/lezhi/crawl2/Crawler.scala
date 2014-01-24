package com.buzzinate.lezhi.crawl2

import com.buzzinate.lezhi.crawl.Vars
import collection.JavaConverters._
import com.buzzinate.crawl.FrontierClient
import org.buzzinate.lezhi.api.Client
import com.twitter.util.Future
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.crawl.CrawlStream
import org.jsoup.Jsoup
import com.buzzinate.keywords.util.ExtractUtil
import com.buzzinate.keywords.util.ThumbnailExtractor
import com.buzzinate.util.DateRegMatcher
import com.buzzinate.keywords.LezhiKeywordsExtractor
import org.jsoup.nodes.Document
import com.buzzinate.lezhi.crawl.LinkUtil
import scala.collection.mutable.ListBuffer
import com.buzzinate.crawl.Item
import com.buzzinate.lezhi.util.DomainNames
import org.buzzinate.lezhi.util.SignatureUtil
import com.buzzinate.imagescaling.FastResampleOp
import java.util.concurrent.Executors
import com.mortennobel.imagescaling.AdvancedResizeOp
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import com.buzzinate.lezhi.util.ImageUploader
import com.buzzinate.lezhi.crawl.batch.KeywordInfoUtil
import com.buzzinate.model.DelimitedKeywordText
import org.buzzinate.lezhi.api.Doc
import org.buzzinate.lezhi.util.LargestTitle
import com.buzzinate.lezhi.util.RegexUtil
import com.buzzinate.lezhi.util.HtmlText
import com.buzzinate.http.PolicyHttpScheduler
import com.buzzinate.http.Http
import com.buzzinate.http.DefaultHttpScheduler
import com.buzzinate.http.ImageResp
import org.ansj.splitWord.Segment
import com.buzzinate.lezhi.zk.ZkCluster
import com.buzzinate.lezhi.store.HbaseTable
import com.buzzinate.lezhi.store.HTableUtil

object Crawler extends Loggable {
  val MAX_DOC_BFS = 20000
  val siteprefixCount = new DocCounting(MAX_DOC_BFS)
    
  val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")
  val vars = new Vars(prop.asInstanceOf[java.util.Map[String, String]].asScala.toMap)
  val client = new Client(vars.elasticsearchHosts.asJava)
  
  val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4)
  
  val http = new PolicyHttpScheduler(Http.buildAgent(200, 2), Executors.newFixedThreadPool(64), 100, 100)
  val imghttp = new DefaultHttpScheduler(Http.buildAgent(200, 2), Executors.newFixedThreadPool(64))
  
  def hasArticleMark(raw: String) = vars.articlePatterns.exists(ap => raw.contains(ap))
  
  def main(args: Array[String]): Unit = { 
    val batchSize = 100
    val flushInterval = 1000 * 600
    
    val pool = HTableUtil.createHTablePool(vars.hbaseZookeeperQuorum, prop.getInt("htable.pool.size", 100))
    val metadata = new HbaseTable(pool, vars.crawlTable, "metadata")
    val keywords = new HbaseTable(pool, vars.searchTable, "keywords")
    
    val frontierClient = new FrontierClient(prop.getProperty("frontier.host", "localhost"))

    val zkconn = prop.getProperty("zookeeper.hosts", "localhost:2181")
    val parentPath = prop.getProperty("zokeeper.path","/lezhi-crawlers")
    val nodeId = prop.getProperty("node.id", ZkCluster.nodeName)
    val source = new FrontierSource(frontierClient.client, client, new ZkCluster(zkconn, parentPath+"/"+ nodeId), true)
    
//    val urls = List(Item("http://test.buzzinate.com/wordpress/?p=4095", "siteprefix" -> "http://test.buzzinate.com"))
//    val source = new MemorySource(urls.map(item => UrlItem(item.url, item.url, item))) with DumyUrlSource[UrlItem]
    
    val cs = CrawlStream.from(source).mapFuture { case UrlItem(url, id, item) =>
      val siteprefix = item.meta.getOrElse("siteprefix", "http://" + DomainNames.safeGetHost(url))
      http.get(url, true, Some(url)).map {  resp =>
        (url, id, siteprefix, item.ndepth, resp)
      }
    }.map { case (url, id, siteprefix, ndepth, bytesResp) =>
      val resp = bytesResp.toHtml(url)
      val doc = Jsoup.parse(resp.html, url)
        
      // handle sunlinks
      val sublinks = subLinks(url, siteprefix, ndepth, doc)
      source.submit(sublinks.map(x => UrlItem(x.url, SignatureUtil.signature(x.url), x)))
        
      (url, ndepth, resp, doc)
    }.filter { case (url, ndepth, resp, doc) =>
      ndepth == 0 || hasArticleMark(resp.html)
    }.map { case (url, ndepth, resp, doc) =>
      val realurl = resp.lastRedirectedUri.getOrElse(url)
      val canonicalUrl = ExtractUtil.extractCanonicalUrl(doc, url)
      val rawTitle = Jsoup.parse(doc.title).text()
      val title = ExtractUtil.extractTitle(doc.body, rawTitle)
      val metaKeywords = ExtractUtil.extractMeta(doc, "keywords")
      val htmltime = DateRegMatcher.getTime(DateRegMatcher.matcher(doc.body.text), url)
      val lastModified = if (htmltime > 0) htmltime else System.currentTimeMillis
        
      val webcontent = WebContent(realurl, canonicalUrl, rawTitle, title, resp.html, metaKeywords, resp.statusCode, lastModified)
      (url, ndepth, resp, doc, webcontent)
    }.batch(batchSize, flushInterval) { case results =>
      // save raw data
      val webcontents = results.map { case (_, _, _, _, webcontent) => webcontent }
      val metaRows = webcontents.filter(c => c.isOK).flatMap { c =>
        val cols = Map("canonicalUrl" -> c.canonicalUrl.getOrElse(c.realurl), "title" -> c.title, "rawTitle" -> c.rawTitle, "keywords" -> c.metaKeywords, "lastModified" -> c.lastModified.toString, "statusCode" -> c.statusCode.toString)
        Set(c.realurl, c.canonicalUrl.getOrElse(c.realurl)) map { url => url -> cols }
      }
      
      info("Save meta#: " + webcontents.map(x => x.realurl + "/" + x.title))
      metadata.putStrRows(metaRows.toList)
      info("Save meta count:"+metaRows.size)
    }.map { case (url, ndepth, resp, doc, webcontent) =>
      // async extract thumbnail, download, upload
      val imgsrcOpt = if (url.contains("docin.com")) None else ThumbnailExtractor.extractThumbnail(imghttp, doc, webcontent.title, url)
//      info(url + " => " + imgsrcOpt)
      (url, ndepth, resp, doc, webcontent, imgsrcOpt)
    }.mapFuture { case (url, ndepth, resp, doc, webcontent, imgsrcOpt) =>
      imgsrcOpt.map { imgsrc =>
        crawlImage(imgsrc, url).map { thum =>
//          info("crawled: " + url + " => " + imgsrc)
          (url, ndepth, resp, doc, webcontent, thum)
        }
      }.getOrElse {
        Future.value((url, ndepth, resp, doc, webcontent, Thumbnail.Null))
      }
    }.mapFuture { case (url, ndepth, resp, doc, webcontent, thum) =>
      if (thum != Thumbnail.Null) {
        ImageUploader.upload(thum.imgsrc, url, thum.format, thum.data).map { uf =>
//          info("uploaded: " + url + " => " + uf)
          (url, ndepth, resp, doc, webcontent, Some(thum.imgsrc, uf))
        }
      } else {
        Future.value((url, ndepth, resp, doc, webcontent, None))
      }
    }.batch(batchSize, flushInterval) { case results =>
      val metaRows = results.flatMap { case (url, ndepth, resp, doc, webcontent, thumbnail) =>
        thumbnail.map { case (imgsrc, thumbnail) =>
          webcontent.canonicalUrl.getOrElse(url) -> Map("imgSrc" -> imgsrc, "thumbnail" -> thumbnail)
        }
      }
      info("Save thumbnail meta#: " + metaRows)
      metadata.putStrRows(metaRows.toList)
      info("Save thumbnail meta count:"+metaRows.size)
    }.map { case (url, ndepth, resp, doc, webcontent, thumbnail) =>
      val keywords = LezhiKeywordsExtractor.extract(url, resp.html)
      info(url + " => " + webcontent.title + thumbnail.map(x => "/" + x._1).getOrElse("") + "\n" + keywords)
      (url, doc, webcontent, thumbnail, keywords)
    }.batch(batchSize, flushInterval) { results =>
      val url2keywords = results map { case (url, doc, webcontent, thumbnail, keywords) =>
        webcontent.canonicalUrl.getOrElse(url) -> KeywordInfoUtil.convert(keywords)
      }
      keywords.putStrRows(url2keywords.map(urlkws => urlkws._1 -> KeywordInfoUtil.toMap(urlkws._2)))
    }.batch(batchSize, flushInterval) { results =>
      val docs = results.map { case (url, doc, webcontent, thumbnailOpt, keywords) =>
        val keywordstr = DelimitedKeywordText.toText(KeywordInfoUtil.convert(keywords))            
        val title = webcontent.title
        val thumbnail = thumbnailOpt.map { case (_, thumb) => thumb } getOrElse("")
        new Doc(url, webcontent.rawTitle, SignatureUtil.signature(LargestTitle.parseLargest(title)), thumbnail, keywordstr, webcontent.lastModified)
      }
      info("index #" + docs.map(d => d.url + "/" + d.title))
      client.bulkAdd(docs.toList.asJava)
      info("index # count:"+docs.size)
    }.commit
    
    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run(): Unit = {
        pool.close
        client.close
      }
    })
    
    Segment.split("bShare乐知是目前最强大，最精准的个性化阅读类应用软件，登录乐知便可以立即获取最精准的个性化的喜好信息。")
    source.start
  }
  
  def crawlImage(imgsrc: String, url: String): Future[Thumbnail] = {
    imghttp.get(imgsrc, true, Some(url)).map { httpresp =>
      val ImageResp(format, img, time) = httpresp.toImage
      if (img != null && isGoodThumbnail(img.getWidth, img.getHeight)) {
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
  
  def subLinks(url: String, siteprefix: String, ndepth: Int, doc: Document): List[Item] = {
    val total = siteprefixCount.countOrElse(siteprefix, client.numDocs(siteprefix))
    if (total >= MAX_DOC_BFS) {
      info("ignore sublinks for " + siteprefix + ", doc size: " + total)
    }
    if (total < MAX_DOC_BFS && ndepth < 5) {
      val hrefs = LinkUtil.extractLinks(url, doc, href => LinkUtil.verifyPrefix(href, siteprefix))
      hrefs.remove(url)
      
      val links = new ListBuffer[Item]
      for ((href, ancorText) <- hrefs) {
        links += Item(href, ndepth + 1, 0, Map("siteprefix" -> siteprefix))
      }
      links.result
    } else List()
  }
}