package com.buzzinate.crawl

import com.alibaba.fastjson.JSON
import collection.JavaConverters._

case class Item(url: String, ndepth: Int, nretry: Int, meta: Map[String, String])

object Item {
  def apply(url: String, meta: Map[String, String]) = new Item(url, 0, 0, meta)
  def apply(url: String, pair: (String, String)) = new Item(url, 0, 0, Map(pair))
  
  def toString(item: Item): String = {
    val map = Map("url"->item.url, "ndepth"->item.ndepth, "nretry"->item.nretry, "meta"->item.meta.asJava)
    JSON.toJSONString(map.asJava, false)
  }
  
  def fromString(str: String): Item = {
    val map = JSON.parse(str).asInstanceOf[java.util.Map[String, Any]].asScala
    val url = map.get("url").get.asInstanceOf[String]
    val ndepth = map.get("ndepth").get.asInstanceOf[Int]
    val nretry = map.get("nretry").get.asInstanceOf[Int]
    val meta = map.get("meta").get.asInstanceOf[java.util.Map[String, String]].asScala.toMap
    Item(url, ndepth, nretry, meta)
  }
  
  def fromString(str: String,id: String): Item = {
    val map = JSON.parse(str).asInstanceOf[java.util.Map[String, Any]].asScala
    val url = map.get("url").get.asInstanceOf[String]
    val ndepth = map.get("ndepth").get.asInstanceOf[Int]
    val nretry = map.get("nretry").get.asInstanceOf[Int]
    val meta = map.get("meta").get.asInstanceOf[java.util.Map[String, String]].asScala.toMap   
    Item(url, ndepth, nretry, meta)
  }
  def main(args: Array[String]): Unit = {
    val meta = Map("type" -> "insite", "url" -> "http://luo.bo/tag/美女/")
    val str = Item.toString(Item("url", 1, 1, meta))
    println(str)
    println(Item.fromString(str))
  }
}