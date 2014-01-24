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

object BuildTrending extends ScoobiApp {
  val base = new SimpleDateFormat("yyyy-MM-dd").parse("2012-08-01").getTime()
  
  override def upload = true
    
  def run() {
    val clicklog = DList.fromSource(new CassandraDataSource("clicklog", "view"))
    val trending = clicklog.map { case (key, columns) =>
      val url = URLCanonicalizer.getCanonicalURL(columns("url"))
      val siteprefix = columns("siteprefix")
      val userid = columns("userid")
      val time = columns("timestamp").toLong
      (url, siteprefix) -> time
    }.groupByKey.map { case ((url, siteprefix), times) =>
      val (cnt, minTime) = times.foldLeft(0 -> Long.MaxValue) { (sizemin, t) =>
        val (size, minT) = sizemin
        (size + 1) -> (minT min t)
      }
     
      val score = math.log(1 + cnt) + (minTime - base) / 45000000.0
      siteprefix -> (url, score)
    }.groupByKey.map { case (siteprefix, urlscores) =>
      val topurls = TopItems.top(urlscores.toList, 50) { case (url, score) => score }
      println("top: " + siteprefix + " ==> " + topurls)
      val cols = for (i <- 0 until topurls.size) yield {
        i.toString -> topurls(i)._1
      }
      siteprefix -> cols.toList
    }
    
    persist(new DListPersister(trending, CassandraDataSink("search", "top")))
  }
}