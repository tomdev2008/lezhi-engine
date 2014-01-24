package com.buzzinate.dm.util

import scala.collection.mutable.HashMap
import com.alibaba.fastjson.JSON
import collection.JavaConverters._

case class RecDoc(url: String, score: Double, recTime: Long, reason: String = null)

object RecommendResult {

  def main(args: Array[String]): Unit = {
    val info = """{"count":10,"numDocs":19,"recType":["INSITE"],"time":1347264833881,"reason":null}"""
    val infomap = JSON.parse(info).asInstanceOf[java.util.Map[String, Any]].asScala
    println(infomap)
  }

  def fromMap(map: Map[String, String]): List[RecDoc] = {
    if (map.size == 0) List[RecDoc]()
    else {
      val infomap = JSON.parse(map("info")).asInstanceOf[java.util.Map[String, Any]].asScala
      val recTime = infomap("time").asInstanceOf[Long]
      
      val rds = JSON.parse(map("INSITE")).asInstanceOf[java.util.List[String]].asScala
      val res = for(rd <- rds) yield {
        val dm = JSON.parse(rd).asInstanceOf[java.util.Map[String, Any]].asScala
        RecDoc(dm("url").asInstanceOf[String], dm("score").asInstanceOf[java.math.BigDecimal].doubleValue, recTime, dm.get("reason").map(r => r.asInstanceOf[String]).getOrElse(null))
      }
      res.toList
    }
  }

}