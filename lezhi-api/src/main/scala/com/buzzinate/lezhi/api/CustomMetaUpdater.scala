package com.buzzinate.lezhi.api

import java.util.concurrent.Executor
import scala.collection.JavaConverters._
import scala.collection.mutable.HashMap
import org.apache.hadoop.hbase.client.HTablePool
import org.buzzinate.lezhi.api.Client
import org.buzzinate.lezhi.util.LargestTitle
import org.buzzinate.lezhi.util.SignatureUtil
import com.buzzinate.crawl.FrontierClient
import com.buzzinate.crawl.Item
import com.buzzinate.lezhi.async.Batch
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.thrift.frontier.{Item => TItem}
import com.buzzinate.lezhi.store.HbaseTable
import com.buzzinate.lezhi.Servers

trait CustomMeta
case class CustomTitle(url: String, title: String) extends CustomMeta
case class CustomThumbnail(url: String, thumbnail: String) extends CustomMeta

class CustomMetaUpdater(threadPool: Executor, htableool: HTablePool, client: Client, frontierClient: FrontierClient) extends Batch[CustomMeta](threadPool, 100, 1000 * 600) with Loggable {
  val hbaseStore = new HbaseTable(htableool, "crawl", "metadata")

  def flush(metas: java.util.ArrayList[CustomMeta]): Unit = {
    val url2title = new HashMap[String, String]
    val url2thumb = new HashMap[String, String]
    metas.asScala.foreach { m =>
      m match {
        case CustomTitle(url, title) => {
          info("custom.title: " + url + " => " + title)
          url2title += url -> title
        }
        case CustomThumbnail(url, thumbnail) => {
          info("custom.thumbnail: " + url + " => " + thumbnail)
          url2thumb += url -> thumbnail
        }
      }
    }

    if (url2title.size > 0) flushTitle(url2title)
    if (url2thumb.size > 0) flushThumb(url2thumb)
  }

  def flushTitle(url2title: HashMap[String, String]): Unit = {
    val storeTitles = hbaseStore.getRows(url2title.keySet) flatMap { case (url, cols) =>
      cols.get("title") map { title => (url, title) }
    } toMap
    
    url2title foreach { case (url, title) =>
      val storeTitle = storeTitles.getOrElse(url, "")
      if (storeTitle != title) {
        hbaseStore.putStr(url, Map("title" -> title))
      }
    }
    
    CustomMetaUpdater.updateTitle(client, url2title)
  }

  def flushThumb(url2thumb: HashMap[String, String]): Unit = {
    val existurls = client.exists(url2thumb.keys.toList.asJava).asScala.toSet
    val items = url2thumb flatMap { case (url, thumb) =>
      if (existurls.contains(url)) {
        info("Send custom thumbnail: " + url + " => " + thumb)
        Some(Item(url, "custom.thumbnail" -> thumb))
      } else None
    }
    info("custom.thumbnail.size: " + url2thumb.size + " => " + items.size)
    if (items.size > 0) frontierClient.client.offer(asThrift(items toList)) onSuccess { t =>
      info("Send thumb size: " + items.size)
    } onFailure { t =>
      warn("error send thumb", t)
    }
  }

  private def asThrift(items: List[Item]): List[TItem] = {
    items.map { item =>
      val id = SignatureUtil.signature(item.url)
      val host = DomainNames.safeGetHost(item.url)
      val queue = if (host.contains("51chudui.com")) "51chudui.com" else host
      TItem(id, Item.toString(item), queue, Some(1000 * 3600 * 3))
    }
  }
}

object CustomMetaUpdater extends Loggable {
  val te = new com.buzzinate.nlp.util.TitleExtractor

  def updateTitle(client: Client, url2title: HashMap[String, String]): Unit = {
    val updatedocs = client.get(url2title.keys.toList.asJava).asScala.flatMap { case (url, doc) =>
      url2title.get(url).filter(t => doc.title != t).map { title =>
        doc.title = title
        doc.signature = SignatureUtil.signature(LargestTitle.parseLargest(te.extract(title)))
        doc
      }
    }.toList

    info("custom.title: " + updatedocs.map(x => x.url + " => " + x.title).mkString(", "))
    if (updatedocs.size > 0) client.bulkAdd(updatedocs.asJava)
  }

  def main(args: Array[String]): Unit = {
    val url2title = new HashMap[String, String]
    url2title += "http://politics.scrb.scol.com.cn/bmxx/content/2013-03/07/content_4819472.htm?node=4721" -> "test title"
    url2title += "http://photos.scrb.scol.com.cn/gqtp/content/2013-03/19/content_4866368_2.htm" -> "四川日报网-金鹅古街"
//    val client = new Client("192.168.1.136")
//    updateTitle(client, url2title)
//    client.close()
     val customMeta = new CustomMetaUpdater(null, Servers.htablePool, null, null)
     customMeta.flushTitle(url2title)
  }
}