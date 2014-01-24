package com.buzzinate.dm.keywords

import com.alibaba.fastjson.JSON
import collection.JavaConverters._

case class Row(key: String, columns: Map[String, String])

object Row {
  def toJson(row: Row): String = {
    val map = row.columns ++ Map("$rowkey" -> row.key)
    JSON.toJSONString(map.asJava, false)
  }
  
  def fromJson(json: String): Row = {
    val map = JSON.parseObject(json).asInstanceOf[java.util.Map[String, String]]
    val key = map.remove("$rowkey")
    Row(key, map.asScala.toMap)
  }
  
  def main(args: Array[String]): Unit = {
    val r = Row("url1", Map("keyword1" -> "keyword1 info", "keyword2" -> "keyword2 info"))
    val json = toJson(r)
    println(json)
    val row = fromJson(json)
    println(row)
  }
}