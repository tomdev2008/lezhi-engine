package com.buzzinate.dm.crawl

import com.nicta.scoobi.Scoobi._
import com.buzzinate.lezhi.util.URLCanonicalizer
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import com.nicta.scoobi.core.DList
import com.buzzinate.dm.cassandra.CassandraDataSource
import com.nicta.scoobi.core.Grouping._
import com.buzzinate.dm.util.TopItems
import com.buzzinate.dm.cassandra.CassandraDataSink
import com.buzzinate.lezhi.util.DomainNames


object BuildUrls extends ScoobiApp {
    
  def run() {
    val hosturls = DList.fromSource(new CassandraDataSource("crawl", "metadata", List("statusCode"))).filter { row =>
      val (url, cols) = row
      cols.contains("statusCode")
    }.map { keycols =>
      val (url, _) = keycols
      val host = DomainNames.safeGetHost(url)
      (host, url.hashCode % 197) -> url
    }.groupByKey.map { row =>
      val ((host, h), urls) = row
      host -> urls.toList.map(url => url -> "")
    }
    
    persist(new DListPersister(hosturls, CassandraDataSink("crawl", "urls")))
  }
}