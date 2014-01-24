package com.buzzinate.lezhi.behavior

import java.lang.{Long => JLong}

import scala.Option.option2Iterable

import org.apache.hadoop.hbase.client.HTablePool

import com.buzzinate.lezhi.store.HbaseTable

case class Doc(site: String, docid: Long, url: String, title: String, keywords: String, timestamp: Long)

class DocDB(htableool: HTablePool) {
  val hbaseStore = new HbaseTable(htableool, "behavior", "itemdb")

  def add(docs: List[Doc]): Unit = {
    val iddocs = docs map { d =>
      (makeRowKey(d.site, d.docid), d)
    }
    val ids = iddocs map { case (id, _) => id }
    val oldids = hbaseStore.getRows(ids, List("timestamp"))  filter { case (_, cols) => cols.size > 0 } map { case (id, _) => id } toSet
    
    val newrows = iddocs filterNot { case (id, _) =>
      oldids.contains(id)
    } map { case (id, d) =>
      (id, Map("url" -> d.url, "title" -> d.title, "keywords" -> d.keywords, "timestamp" -> JLong.toString(d.timestamp, Character.MAX_RADIX)))
    }

    hbaseStore.putStrRows(newrows.toTraversable)
  }

  def get(site: String, docids: List[Long]): Map[Long, Doc] = {
    val plen = (site + "-").length

    val ids = docids map { docid => makeRowKey(site, docid) }
    hbaseStore.getRows(ids,List())flatMap { case (row, cols) =>
      val docid = JLong.parseLong(row.substring(plen), Character.MAX_RADIX)
      for {
        url <- cols.get("url")
        title <- cols.get("title")
        keywords <- cols.get("keywords")
        timestamp <- cols.get("timestamp")
      } yield (docid, Doc(site, docid, url, title, keywords, JLong.parseLong(timestamp, Character.MAX_RADIX)))
    } toMap
  }

  def getTitles(site: String, docids: List[Long]): Map[Long, String] = {
    if (docids.isEmpty) return Map()

    val plen = (site + "-").length

    val ids = docids map { docid => makeRowKey(site, docid) }
    hbaseStore.getRows(ids, List("title")) flatMap { case (row, cols) =>
      val docid = JLong.parseLong(row.substring(plen), Character.MAX_RADIX)
      cols.get("title") map { title =>
        (docid, title)
      }
    } toMap
  }

  private def makeRowKey(site: String, docid: Long): String = site + "-" + JLong.toString(docid, Character.MAX_RADIX)
}