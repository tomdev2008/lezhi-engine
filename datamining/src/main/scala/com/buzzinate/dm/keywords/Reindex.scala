package com.buzzinate.dm.keywords

import com.nicta.scoobi.Scoobi._
import com.buzzinate.lezhi.util.URLCanonicalizer
import java.util.regex.Pattern
import java.text.SimpleDateFormat
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
import com.buzzinate.model.RowKeyUtil

object Reindex extends ScoobiApp {
  
  implicit def BytesFmt = new WireFormat[java.nio.ByteBuffer] {
    def toWire(x: java.nio.ByteBuffer, out: DataOutput) = {
      val bs = ByteBufferUtil.getArray(x)
      out.writeInt(bs.length)
      out.write(x.array)
    }
    def fromWire(in: DataInput): java.nio.ByteBuffer = {
      val len = in.readInt
      val bs = new Array[Byte](len)
      in.readFully(bs)
      java.nio.ByteBuffer.wrap(bs)
    }
  }
  
  def run() {
    val words = DList.fromSource(new CassandraDataSource("search", "keywords")).map { case (url, cols) =>
      url -> cols.toList
    }
    
    // calc docfreq
    val docfreq = words.flatMap { case (url, ws) =>
      val host = DomainNames.safeGetHost(url)
      for ((word, _) <- (ws ++ List("TOTAL" -> ""))) yield {
        (host, word) -> 1L
      }
    }.groupByKey.map { case ((host, word), fs) =>
      RowKeyUtil.word2DocFreqRow(host, word) -> (word, fs.sum)
    }.groupByKey.map { case (host, dfs) =>
      val docfreqs = dfs.toList
      host -> docfreqs
    }
    persist(new DListPersister(docfreq, CassandraDataSink.counter("search", "docfreq")))
    
    // build inverse
    val inverse = DList.fromSource(new CassandraDataSource("crawl", "metadata", List("lastModified"))).flatMap { case (url, cols) =>
      cols.get("lastModified").map(lm => url -> lm.toLong)
    }.join(words).flatMap { row =>
      val (url, (time, ws)) = row
      ws.map { case (w, wi) =>
        (w, url.hashCode % 137) -> (url, time, wi, ws.size)
      }
    }.groupByKey.map { case ((w, _), dis) =>
      val ds = for ((url, time, wi, numWords) <- dis.toList) yield {
        val host = DomainNames.safeGetHost(url)
        val ki = KeywordInfo.fromKV(w, wi)
        val (k, v) = DocInfo.toKV(DocInfo(url, time, ki.freq, ki.field, numWords))
        (host, k, v)
      }
      w -> ds
    }
    persist(new DListPersister(inverse, CassandraDataSink.superColumn("search", "inverse")))
  }
}