package com.buzzinate.dm.archive

import com.nicta.scoobi.Scoobi._
import com.buzzinate.lezhi.util.URLCanonicalizer
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import com.nicta.scoobi.core.DList
import com.buzzinate.dm.cassandra.CassandraDataSource
import com.alibaba.fastjson.JSON
import collection.JavaConverters._
import org.apache.hadoop.io.Text
import com.buzzinate.dm.util.DateUtil
import com.buzzinate.dm.cassandra.MaxTimestampCassandraInputConverter

object Archive extends ScoobiApp {
  override def upload = true
    
  def run() {
    val today = DateUtil.truncateDate(System.currentTimeMillis)
    
    val since = (today - DateUtil.ONE_DAY) * 1000
    val to = today * 1000
    
     val rawhtmls = DList.fromSource(new CassandraDataSource("crawl", "content", List("raw"), MaxTimestampCassandraInputConverter.string, 512)).filter { case (url, columns, ts) =>
      columns.contains("raw") && (ts >= since && ts < to)
    }.map { case (url, columns, ts) =>
      println(url + " timestamp: " + ts)
      val html = columns("raw")
      new Text(url) -> new Text(html)
    }
    
    persist(toSequenceFile[Text, Text](rawhtmls, "/crawl/content/" + DateUtil.format(today), true))
  }
}