package com.buzzinate.lezhi.crawl2

import scala.collection.Iterable
import com.buzzinate.crawl.UrlSource
import com.buzzinate.crawl.Item
import com.buzzinate.thrift.frontier.UrlFrontier
import com.buzzinate.crawl.UrlEntry
import com.buzzinate.lezhi.util.ItemConverter
import org.buzzinate.lezhi.api.Client
import collection.JavaConverters._
import org.elasticsearch.indices.IndexMissingException
import com.buzzinate.lezhi.crawl.LinkUtil
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.stream.Track
import com.buzzinate.stream.WithTrack
import scala.collection.mutable.HashSet
import com.buzzinate.lezhi.util.LockSupport
import java.util.concurrent.atomic.AtomicInteger
import com.buzzinate.lezhi.zk.Cluster

case class UrlItem(url: String, id: String, item: Item) extends UrlEntry

class FrontierSource(frontier: UrlFrontier.FinagledClient, client: Client, cluster: Cluster, checkExists: Boolean) extends UrlSource[UrlItem] with Loggable {
  val total = new AtomicInteger(0)
  val ids = new HashSet[String]
  val acklock = new java.util.concurrent.locks.ReentrantLock with LockSupport
  
  def ack(id: String): Unit = {
    total.decrementAndGet
    acklock.inlock {
      ids += id
      if (ids.size >= 100) {
        info("acks: " + ids)
        frontier.ack(ids.toSeq)
        ids.clear
      }
    }
  }
 
  case class UrlTrack(url: String, id: String) extends Track {
    def onError(t: Throwable): Unit = {
      warn(id + "/" + url + " with error: " + t.getMessage,t)
      ack(id)
    }
    def onFilter(): Unit = {
      debug(id + "/" + url + " is filtered")
      ack(id)
    }
    def onCommit(): Unit = {
      ack(id)
    }
}
  
  def submit(ues: Iterable[UrlItem]): Unit = {
    val items = ues.map(ue => ue.item)
    frontier.offer(ItemConverter.asThrift(items.toList))
  }

  def start(): Unit = {
    cluster.join
    while(!Thread.interrupted()){
      frontier.pop(cluster.id, 100) onSuccess { titems =>
        info(cluster.id + " pop " + titems.size )
        total.addAndGet(titems.size)
        val urlitems = titems map { titem =>
          val item = Item.fromString(titem.data) 
          UrlItem(item.url, titem.id, item) 
        }

        val existurls = try {
          if (checkExists) client.exists(urlitems.map(ui => ui.url).asJava).asScala.toSet
          else Set.empty[String]
        } catch {
          case e: IndexMissingException => Set.empty[String]
        }
        
        urlitems foreach { case UrlItem(url, id, item) =>
          val exists = existurls.contains(url)
          val needDrop = url.startsWith("http://www.l8sq.com/") && item.ndepth > 0 ||
        	url.contains("connect.php") ||
        	url.contains("home.php") ||
        	url.contains("mod=post") ||
        	url.contains("www.qq120.info") || url.contains("rentiyishu.43w.org") || url.contains("pc580.info") || url.contains("31zu.com") || url.contains("/play/?") ||
        	url.contains("search.asp") ||
        	url.contains("googleusercontent.com") || url.contains("baiducontent.com")
        	url.contains("http://www.yokamen.cnhttp") ||
        	LinkUtil.isIP(DomainNames.safeGetHost(url)) ||
    		  url.contains("c_urlredirect.asp") ||
          !(url.startsWith("http://") || url.startsWith("https://"))
          if (exists || needDrop) {
            debug("ignore: " + url)
            ack(id)
          } else offer(WithTrack(UrlItem(url, id, item), UrlTrack(url, id)))
        }
      }
      
      while (total.get >= 20000) {
        info("#### sleeping because total: " + total.get)
        Thread.sleep(200)
      }
      Thread.sleep(200)
    } 
  }
}