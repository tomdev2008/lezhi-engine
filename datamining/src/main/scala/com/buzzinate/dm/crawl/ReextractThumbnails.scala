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
import org.apache.hadoop.fs.Path
import com.nicta.scoobi.io.FileSystems
import org.apache.hadoop.io.Text
import org.jsoup.Jsoup
import com.buzzinate.keywords.util.ExtractUtil
import com.buzzinate.keywords.util.ThumbnailExtractor

object ReextractThumbnails extends ScoobiApp {
    
  def run() {
    val pathes = configuration.fileSystem.listStatus(new Path("/crawl/content")).map{ fs => fs.getPath.toUri.getPath }.toList
    val thumbnails = fromSequenceFile[Text, Text](pathes.filterNot(url => url.endsWith("all"))).flatMap { case (urltext, htmltext) =>
      val url = urltext.toString
      val html = htmltext.toString
      try {
        if (url.contains("chinanews.com")) {
          val doc = Jsoup.parse(html, url)
          val title = ExtractUtil.extractTitle(doc.body, doc.title)
          ThumbnailExtractor.extractThumbnail(doc, title, url).map { thumbnail =>
            thumbnail -> url
          }
        } else None
      } catch {
        case e: Throwable => {
          println(e.getMessage + " , url= " + url)
          None
        }
      }
    }.groupByKey.map { case (thumbnail, urls) =>
      val url = urls.head
      url + "\t" + thumbnail
    }
    
    persist(toTextFile(thumbnails, "/tmp/thumbnails", true))
  }
}