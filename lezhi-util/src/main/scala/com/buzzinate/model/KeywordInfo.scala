package com.buzzinate.model

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import collection.JavaConverters._

case class KeywordInfo(word: String, freq: Int, field: Int)

object KeywordInfo {
  val CONTENT = 0
  val META = 1
  val TITLE = 2
  val META_TITLE = 3
  
  def toKV(ki: KeywordInfo): (String, String) = {
    val info = Map("freq"->ki.freq, "field"->ki.field)
    ki.word -> JSON.toJSONString(info.asJava, false)
  }
  
  def fromKV(kv: (String, String)): KeywordInfo = {
    val (word, info) = kv
    val map = JSON.parse(info).asInstanceOf[java.util.Map[String, Any]].asScala
    val freq = map.get("freq").get.asInstanceOf[Int]
    val field = map.get("field").get.asInstanceOf[Int]
    KeywordInfo(word, freq, field)
  }
  
  def main(args: Array[String]): Unit = {
    val ki = KeywordInfo("the matrix", 3, TITLE)
    println(ki)
    val kv = KeywordInfo.toKV(ki)
    println(kv)
    println(KeywordInfo.fromKV(kv))
  }
}