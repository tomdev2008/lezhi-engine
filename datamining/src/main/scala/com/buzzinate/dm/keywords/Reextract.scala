package com.buzzinate.dm.keywords

import com.nicta.scoobi.Scoobi._
import com.buzzinate.lezhi.util.URLCanonicalizer
import java.util.regex.Pattern
import com.nicta.scoobi.core.DList
import com.buzzinate.dm.cassandra.CassandraDataSource
import com.buzzinate.dm.util.TopItems
import com.buzzinate.dm.cassandra.CassandraDataSink
import scala.collection.mutable.ListBuffer
import collection.JavaConverters._
import com.buzzinate.nlp.util.TextUtil
import com.buzzinate.dm.util.TextSnippet
import com.buzzinate.lezhi.util.DomainNames
import org.apache.commons.lang.StringUtils
import com.nicta.scoobi.core.Grouping
import com.nicta.scoobi.application.ScoobiConfiguration
import scala.collection.mutable.HashMap
import com.buzzinate.keywords.util.HashMapUtil
import scala.collection.mutable.HashSet
import com.buzzinate.model.KeywordInfo
import com.buzzinate.model.DocInfo
import java.io.DataOutput
import java.io.DataInput
import org.apache.cassandra.utils.ByteBufferUtil
import com.buzzinate.keywords.LezhiKeywordsExtractor
import com.buzzinate.nlp.util.TitleExtractor
import org.jsoup.Jsoup
import com.buzzinate.keywords.util.ExtractUtil
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.Text

object Reextract extends ScoobiApp {
  
  def run() {
    val pathes = configuration.fileSystem.listStatus(new Path("/crawl/content")).map{ fs => fs.getPath.toUri.getPath }.toList
    val newWords = fromSequenceFile[Text, Text](pathes).map { case (wurl, whtml) =>
      val url = wurl.toString
      val html = whtml.toString
      val doc = Jsoup.parse(html, url)
      val canonicalUrl = ExtractUtil.extractCanonicalUrl(doc, url)
      val title = Global.extractTitle(doc.title)
      val keywords = LezhiKeywordsExtractor.extract(url, html)
      println(canonicalUrl + " => " + title)
//      canonicalUrl + ": " + title + " => \n" + keywords.mkString(", ")
      val row = Row(canonicalUrl, Map() ++ keywords.map(kw => KeywordInfo.toKV(KeywordInfo(kw.word, kw.freq, kw.field))))
      Row.toJson(row)
    }
    
    persist(toTextFile(newWords, "/keywords/new", true))
  }
}