package com.buzzinate.dm.adaptive

import com.nicta.scoobi.Scoobi._
import collection.JavaConverters._
import com.nicta.scoobi.application.ScoobiApp
import com.nicta.scoobi.core.DList
import com.buzzinate.dm.cassandra.CassandraDataSource
import com.buzzinate.dm.util.RecommendResult

object BuildTopClickInfo extends ScoobiApp {
  def run() {
   
    val metadatas = DList.fromSource(new CassandraDataSource("crawl", "recurls"))
    val s2t = DList.fromSource(new CassandraDataSource("search", "top")).flatMap { row =>
      val (site, cols) = row
      val topUrls = cols.values.toList
      for (url <- topUrls) yield {
        url -> site
      }
    }

    val recs = DList.fromSource(new CassandraDataSource("cache", "recommend", List("INSITE", "info"))).filter {
      case (key, columns) =>
        columns.contains("INSITE")
    }.map {
      case (url, cols) =>

        val recs = RecommendResult.fromMap(cols)
        url -> recs.map(rec => (rec.url, rec.recTime))
    }

    val p2s = s2t.join(recs).flatMap {
      case (fromurl, (site, tourlsInfo)) =>
        for (tourlInfo <- tourlsInfo) yield {
          val (tourl, recTime) = tourlInfo
          (fromurl, tourl) -> (site, recTime)
        }
    }

    persist(toDelimitedTextFile(p2s, "/tmp/test/topRec", ","))

    val u2s = p2s.flatMap {
      case ((fromurl, tourl), (site, recTime)) =>
        for (url <- List(fromurl, tourl)) yield {
          url -> site
        }
    }.groupByKey

    val u2l = DList.fromSource(new CassandraDataSource("crawl", "metadata", List("lastModified"))).filter {
      case (key, columns) =>
        columns.contains("lastModified")
    }.map {
      case (url, cols) =>
        url -> cols.getOrElse("lastModified", "0")
    }
    
    val topu2l = u2s.join(u2l).map { case (url, (sites,lastModified)) =>
      url -> lastModified
    }
    
    persist(toDelimitedTextFile(topu2l, "/tmp/test/topLastmodified", ","))

    val click = DList.fromSource(new CassandraDataSource("clicklog", "click", List("fromurl", "tourl", "timestamp"))).map {
      case (uuid, cols) =>
        val (fromurl, tourl, timestamp) = (cols.getOrElse("fromurl", ""), cols.getOrElse("tourl", ""), cols.getOrElse("timestamp", ""))
        (fromurl, tourl) -> timestamp
    }

    val topClick = p2s.join(click).groupByKey.map {
      case ((fromurl, tourl), list) =>
       
        (fromurl, tourl, list.size)
    }

    persist(toDelimitedTextFile(topClick, "/tmp/test/topClick", ","))

  }
}