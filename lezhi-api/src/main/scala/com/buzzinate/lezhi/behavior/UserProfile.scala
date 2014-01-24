package com.buzzinate.lezhi.behavior

import java.lang.{ Long=>JLong }
import com.buzzinate.lezhi.store.HbaseTable
import org.apache.hadoop.hbase.client.HTablePool

case class ViewEntry(site: String, userid: String, docid: Long, timestamp: Long)

class UserProfile(htableool: HTablePool, max: Int) {
  val hbaseStore = new HbaseTable(htableool, "behavior", "profile")
  
  def add(entries: List[ViewEntry]): Unit = {
    val rows = entries map { case ViewEntry(site, userid, docid, timestamp) =>
      val value = JLong.toString(docid, Character.MAX_RADIX)
      (userid,site, value, timestamp)
    }

    hbaseStore.putColTimes(rows)
  }

  def get(site: String, userid: String): Set[Long] = {
    hbaseStore.topN(userid, site, max) map { case (docid, timestamp) =>
      JLong parseLong(docid, Character.MAX_RADIX)
    } toSet
  }
}