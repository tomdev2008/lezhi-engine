package com.buzzinate.dm.keywords

import org.apache.commons.lang.StringUtils

import com.buzzinate.dm.cassandra.CassandraDataSource
import com.nicta.scoobi.Scoobi.ComparableGrouping
import com.nicta.scoobi.Scoobi.ScoobiApp
import com.nicta.scoobi.Scoobi.StringFmt
import com.nicta.scoobi.Scoobi.Tuple2Fmt
import com.nicta.scoobi.Scoobi.persist
import com.nicta.scoobi.Scoobi.toTextFile
import com.nicta.scoobi.core.DList
import collection.JavaConverters._

object DetectErrorThumbnail extends ScoobiApp {
  
  def run() {
    val thumbnails = DList.fromSource(new CassandraDataSource("crawl", "metadata", List("thumbnail"))).flatMap { case (url, cols) =>
      val thumbnail = cols.getOrElse("thumbnail", "")
      if (StringUtils.isBlank(thumbnail)) Nil
      else List(thumbnail -> url)
    }.groupByKey.flatMap { case (thumbnail, urls) =>
      val list = urls.toList
      if (list.size >= 3) List(thumbnail -> list.mkString(", "))
      else Nil
    }
   
    persist(toTextFile(thumbnails, "/tmp/thumbnails", true))
  }
}