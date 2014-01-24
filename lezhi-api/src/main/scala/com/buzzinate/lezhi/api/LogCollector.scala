package com.buzzinate.lezhi.api

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import com.buzzinate.lezhi.Servers
import java.util.concurrent.Executor
import java.nio.ByteBuffer
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.lezhi.thrift.RecommendType
import org.apache.flume.thrift.ThriftFlumeEvent
import com.alibaba.fastjson.JSON
import com.buzzinate.lezhi.util.SignatureUtil
import org.apache.commons.lang.StringUtils
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.behavior._
import com.buzzinate.lezhi.behavior.Doc
import com.buzzinate.lezhi.async.Batch
import com.buzzinate.lezhi.store.HTableUtil

trait Log

case class ViewLog(userid: String, url: String, siteprefix: String, title: String, keywords: String, timestamp: Long) extends Log {
  def toEvent(): ThriftFlumeEvent = {
    val map = Map("userid" -> userid, "url" -> url, "siteprefix" -> siteprefix, "title" -> title, "keywords" -> keywords, "timestamp" -> timestamp)
    val json = JSON.toJSONBytes(map.asJava)
    ThriftFlumeEvent(Map("category" -> "viewlog"), ByteBuffer.wrap(json))
  }
}

case class ClickLog(userid: String, fromurl: String, tourl: String, recType: RecommendType, timestamp: Long) extends Log {
  def toEvent(): ThriftFlumeEvent = {
    val map = Map("userid" -> userid, "fromurl" -> fromurl, "tourl" -> tourl, "timestamp" -> timestamp)
    val json = JSON.toJSONBytes(map.asJava)
    ThriftFlumeEvent(Map("category" -> "clicklog"), ByteBuffer.wrap(json))
  }
}

class LogCollector(threadPool: Executor) extends Batch[Log](threadPool, 100, 1000 * 60) with Loggable {

  val logUpdater = new LogUpdater

  def flush(logs: java.util.ArrayList[Log]): Unit = {
    val viewlogbuff = new ListBuffer[ViewLog]
    val clicklogbuff = new ListBuffer[ClickLog]
    logs.asScala.foreach { log =>
      log match {
        case vl: ViewLog => viewlogbuff += vl
        case cl: ClickLog => clicklogbuff += cl
      }
    }

    info("logsize: view=" + viewlogbuff.size + ", clicklog=" + clicklogbuff.size)

    logUpdater.update(viewlogbuff.result, clicklogbuff.result)
  }
}

class LogUpdater extends Loggable {
  val pool = Servers.htablePool
  val userProfile = new UserProfile(Servers.htablePool, 50)
  val docdb = new DocDB(Servers.htablePool)

  def update(viewlogs: List[ViewLog], clicklogs: List[ClickLog]): Unit = {
    if (viewlogs.size > 0) {
      LogCollector.updateProfile(userProfile, docdb, viewlogs)
      flumeViewLogs(viewlogs)
    }
    if (clicklogs.size > 0) flumeClickLogs(clicklogs)
  }
  
  def flumeViewLogs(viewlogs: List[ViewLog]): Unit = {
    val events = viewlogs.map { viewlog => viewlog.toEvent }
    Servers.flumeClient.appendBatch(events) onSuccess { status =>
      info("flume viewlogs #" + viewlogs.size + " => " + status)
    } onFailure { t =>
      error(t)
    }
  }
  
  def flumeClickLogs(clicklogs: List[ClickLog]): Unit = {
    val events = clicklogs.map { clicklog => clicklog.toEvent }
    Servers.flumeClient.appendBatch(events) onSuccess { status =>
      info("flume clicklogs #" + clicklogs.size + " => " + status)
    } onFailure { t =>
      error(t)
    }
  }
}

object LogCollector {
  def updateProfile(userProfile: UserProfile, docdb: DocDB, viewlogs: List[ViewLog]): Unit = {
    val userdocs = viewlogs.filterNot(_.siteprefix.contains("gmw.cn")) map { case ViewLog(userid, url, _, title, keywords, timestamp) =>
      val site = StringUtils.substringBefore(DomainNames.safeGetPLD(url), ":")
      val doc = Doc(site, SignatureUtil.signatureTitle(title), url, title, keywords, timestamp)
      (userid, doc)
    }

    val ves = userdocs map { case (userid, Doc(site, docid, _,_,_,timestamp)) =>
      ViewEntry(site, userid, docid, timestamp)
    }
    userProfile.add(ves)

    docdb.add(userdocs map { case (userid, doc) => doc })
  }

  def main(args: Array[String]): Unit = {
    val viewlogs = List(
      ViewLog("testuser", "http://bbs.chinanews.com/web/65/2012/1016/51251.shtml", "http://bbs.chinanews.com", "中新网社区 - 围观神秘女子 是谁制造了“总参一姐”(图)", "", System.currentTimeMillis),
      ViewLog("testuser", "http://bbs.chinanews.com/web/65/2012/1019/51738_5.shtml", "http://bbs.chinanews.com", "中新网社区 - “总参一姐”任婕被拘 大尺度半裸照片网上曝光(组图）(5)#nextpage", "", System.currentTimeMillis + 1000),
      ViewLog("testuser", "http://bbs.chinanews.com/web/blog/2012/0808/26996.shtml", "http://bbs.chinanews.com", "中新博客精选--董卿周涛暗自交恶内幕：为争\"一姐\"撕破脸皮(图）", "", System.currentTimeMillis + 2000),
      ViewLog("testuser", "http://bbs.chinanews.com/web/mil/hd/2012/05-29/66721.shtml", "http://bbs.chinanews.com", "国军新一军 抗战胜利前夕与洋姐狂欢-中新社区", "", System.currentTimeMillis + 3000)
    )

//    val esClient = new RichClient("lezhi", List("192.168.1.136"))

    val pool = HTableUtil.createHTablePool("192.168.1.93", 10)
//
    val up = new UserProfile(pool, 50)
    val docdb = new DocDB(pool)
    updateProfile(up, docdb, viewlogs)
    val ds = up.get("chinanews.com", "testuser")
    println(ds)
    ds foreach { d =>
      println(d + " => " + docdb.get("chinanews.com", List(d)))
    }
  }
}