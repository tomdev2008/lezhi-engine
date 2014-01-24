package com.buzzinate.trending;

import com.nicta.scoobi.Scoobi._
import com.buzzinate.lezhi.util.URLCanonicalizer
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import com.nicta.scoobi.core.DList
import com.buzzinate.dm.cassandra.CassandraDataSource
import com.nicta.scoobi.core.Grouping._
import com.buzzinate.dm.util.TopItems
import com.buzzinate.dm.cassandra.CassandraDataSink
import com.buzzinate.dm.cassandra.CassandraInputConverter
import com.buzzinate.dm.util.UrlPagination
import scala.collection.mutable.HashMap
import org.apache.commons.lang.StringUtils

object BuildTrending2 extends ScoobiApp {
  val base = new SimpleDateFormat("yyyy-MM-dd").parse("2012-08-01").getTime
  
  def run() {
    val clickstat = DList.fromSource(new CassandraDataSource("clicklog", "stat", List(), CassandraInputConverter.counter)).flatMap { case (key, columns) =>
      val url = URLCanonicalizer.getCanonicalURL(key)
      for ((siteprefix, click) <- columns) yield {
        url -> (siteprefix, click)
      }
    }
   
    val urltime = DList.fromSource(new CassandraDataSource("crawl", "metadata", List("lastModified"))).filter { case (key, columns) =>
      columns.contains("lastModified")
    }.map { case (key, columns) =>
      val lastModified = columns("lastModified").toLong
      val url = URLCanonicalizer.getCanonicalURL(key)
      url -> lastModified
    }
    
    val top = clickstat.join(urltime).map { case (rawurl, ((siteprefix, click), time)) =>
      val score = math.log(1 + click) + (time - base) / 45000000.0
      val url = if (rawurl.contains("gmw.cn")) StringUtils.substringBefore(rawurl, "?") else rawurl
      println("score: " + url + " -> " + score)
      (siteprefix, url.hashCode % 197) -> (url, score)
    }.groupByKey.flatMap { case ((siteprefix, _), urlscores) =>
      val topurls = TopItems.top(UrlPagination.distinctUrls(urlscores), 50) { case (url, score) => score }
      topurls map { case (url, score) =>
        siteprefix -> (url, score)
      }
    }.groupByKey.map { case (siteprefix, urlscores) =>
      val topurls = TopItems.top(UrlPagination.distinctUrls(urlscores), 50) { case (url, score) => score }
      val cols = for (i <- 0 until topurls.size) yield {
        int2str(i) -> topurls(i)._1
      }
      siteprefix -> cols.toList
    }
    
    persist(new DListPersister(top, CassandraDataSink("search", "top")))
  }

  def int2str(v: Int): String = {
    if (v < 10) "0" + v else v.toString
  }
}