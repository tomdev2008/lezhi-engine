package com.buzzinate.lezhi.api

import com.buzzinate.lezhi.store.DocStatus
import com.google.common.cache.CacheBuilder
import java.util.concurrent.{Executors, TimeUnit}
import com.buzzinate.lezhi.thrift.StatusEnum
import com.buzzinate.lezhi.elastic.RecDoc
import com.buzzinate.lezhi.util.DomainNames
import org.buzzinate.lezhi.util.SignatureUtil
import scala.collection.mutable.ListBuffer
import org.apache.hadoop.hbase.client.HTablePool
import org.apache.hadoop.hbase.HBaseConfiguration

class DocStatusFilter(docStatus: DocStatus) {
  val thread = Executors.newSingleThreadExecutor
  val site2docstatus = CacheBuilder.newBuilder.maximumSize(1000).concurrencyLevel(5).expireAfterWrite(1, TimeUnit.HOURS).build[String, Map[String, StatusEnum]]

  def filter(siteprefix: String, docs: List[RecDoc]): List[RecDoc] = {
    val site = DomainNames.safeGetPLD(siteprefix)
    val docstatus = site2docstatus.getIfPresent(site)
    if (docstatus == null) {
      thread.submit(new Runnable {
        override def run(): Unit = {
          val docstatus = docStatus.getStatus(site).map { case (url, status) =>
            (SignatureUtil.signature(url), status)
          }
          site2docstatus.put(site, docstatus)
        }
      })

      docs
    } else {
      val priorDocs = new ListBuffer[RecDoc]
      val normalDocs = new ListBuffer[RecDoc]
      for (doc <- docs) {
        val id = SignatureUtil.signature(doc.url)
        val status = docstatus.getOrElse(id, StatusEnum.Normal)
        if (status == StatusEnum.Prior) priorDocs += doc
        if (status == StatusEnum.Normal) normalDocs += doc
      }

      priorDocs.result ++ normalDocs.result
    }
  }
}

object DocStatusFilter {
  def main(args: Array[String]): Unit = {
    val docs = List(
      RecDoc("http://www.taihainet.com/news/fujian/yghx/2013-04-06/1048288.html", "288", "", 0L, 0L, 2.0f),
      RecDoc("http://www.taihainet.com/news/fujian/yghx/2013-04-04/1047358_2.html", "358", "", 0L, 0L, 1.0f)
    )

    val conf = HBaseConfiguration.create()
    val pool = new HTablePool()
    
    val docStatusFilter = new DocStatusFilter(new DocStatus(pool))
    docStatusFilter.filter("http://www.taihainet.com", docs) foreach { d =>
      println(d)
    }
    docStatusFilter.filter("http://www.taihainet.com", docs) foreach { d =>
      println(d)
    }
  }
}