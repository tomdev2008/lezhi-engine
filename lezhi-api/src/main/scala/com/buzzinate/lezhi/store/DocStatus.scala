package com.buzzinate.lezhi.store

import collection.JavaConverters._
import com.alibaba.fastjson.JSON
import com.buzzinate.lezhi.thrift.StatusEnum
import org.buzzinate.lezhi.util.DomainNames
import org.buzzinate.lezhi.util.SignatureUtil
import org.apache.hadoop.hbase.client.HTablePool

class DocStatus(htableool: HTablePool, max: Int = 100) {
   val hbaseStore = new HbaseTable(htableool, "search", "docstatus")

  def getStatus(site: String): Map[String, StatusEnum] = {
     hbaseStore.getRowByCount(site, max) map { case (_, json) =>
      val map = JSON.parse(json).asInstanceOf[java.util.Map[String, String]]
      val url = map.get("url")
      val status = map.get("status")
      (url, StatusEnum.valueOf(status).get)
    }
     Map()
  }

  def getStatus(site: String, urls: List[String]): Map[String, StatusEnum] = {
    if (urls.isEmpty) return Map()

    val names = urls.map(url => SignatureUtil.signature(url))
      hbaseStore.getRow(site, names) map { case (_, json) =>
      val map = JSON.parse(json).asInstanceOf[java.util.Map[String, String]]
      val url = map.get("url")
      val status = map.get("status")
      (url, StatusEnum.valueOf(status).get)
    }
  }
  
  def getStatus(sites: Iterable[String], urls:List[String]): scala.collection.mutable.Map[String, StatusEnum] = {
    if (urls.isEmpty) return scala.collection.mutable.Map()

    val names = urls.map(url => SignatureUtil.signature(url))
    val results =  hbaseStore.getRows(sites, names)
    val url2status = scala.collection.mutable.Map.empty[String, StatusEnum]
    
    for(result <- results){
    result._2.foreach { case (_, json) =>
      val map = JSON.parse(json).asInstanceOf[java.util.Map[String, String]]
      val url = map.get("url")
      val status = map.get("status")
       url2status += (url -> StatusEnum.valueOf(status).get)
     }
    }
   url2status
  }

  def put(url: String, status: StatusEnum): Unit = {
    val site = DomainNames.safeGetPLD(url)
    if (status == StatusEnum.Normal) {
      val id = SignatureUtil.signature(url)
    } else {
      val json = JSON.toJSONString(Map("url" -> url, "status" -> status.name).asJava, false)
      val id = SignatureUtil.signature(url)
      hbaseStore.putStr(site, Map(id -> json))
    }
  }
}