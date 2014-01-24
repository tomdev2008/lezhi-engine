package com.buzzinate.lezhi

import com.buzzinate.lezhi.elastic.RecDoc
import com.alibaba.fastjson.JSON
import org.apache.hadoop.hbase.client.HTablePool
import com.buzzinate.lezhi.store.HbaseTable

class Trending(htableool: HTablePool) {
  val hbaseStore = new HbaseTable(htableool, "search", "top")
  
  def getTrending(siteprefix: String, count: Int): List[RecDoc] = {
    hbaseStore.getRowByCount(siteprefix, count).toList.map { case (id, json) =>
      val map = JSON.parse(json).asInstanceOf[java.util.Map[String, Object]]
      val url = map.get("url").asInstanceOf[String]
      val title = map.get("title").asInstanceOf[String]
      val score = map.get("score").asInstanceOf[Number].doubleValue
      (id.toInt, RecDoc(url, title, "", System.currentTimeMillis,0L, score.toFloat))
    } sortBy(x => x._1) map { case (_, doc) => doc }
  }
}

object Trending {
  def main(args: Array[String]): Unit = {
    val t = new Trending(Servers.htablePool)
    t.getTrending("http://bbs.chinanews.com", 10) foreach { d =>
      println(d)
    }
  }
}