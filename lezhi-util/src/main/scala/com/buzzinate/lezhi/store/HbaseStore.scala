package com.buzzinate.lezhi.store

import org.apache.hadoop.hbase.client.HTablePool
import com.buzzinate.lezhi.util.Loggable
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Get
import collection.JavaConverters._
import org.apache.hadoop.hbase.KeyValue

class HbaseTable(pool: HTablePool, table: String, cf: String) extends Loggable {
  val cfBytes = Bytes.toBytes(cf)
  
  def topN(row: String, qualifier: String, top: Int): List[(String, Long)] = {
    val get = new Get(Bytes.toBytes(row))
    get.setMaxVersions(top)
    get.addFamily(cfBytes)
    get.addColumn(cfBytes, Bytes.toBytes(qualifier))
    val result = HTableUtil.use(pool, table) { htable => htable.get(get) }
    if(result.isDefined && result.get.list() != null){
        result.get.list().asScala.map { kv =>
        	val value = Bytes.toString(kv.getValue())
        	(value, kv.getTimestamp() / 1000)
        }.toList
    } else List()
  }

  def putColTimes(rowtimes: Traversable[(String, String, String, Long)]): Unit = {
    val putList = rowtimes.map { case (row, qualifier, value, timestamp) =>
      val put = new Put(Bytes.toBytes(row))
      put.add(cfBytes, Bytes.toBytes(qualifier), timestamp, Bytes.toBytes(value))
      put
    }.toList
    HTableUtil.use(pool, table) { htable => htable.put(putList.asJava) }
  }

  def getRow(row: String, cols: List[String] = List()): Map[String, String] = {
    val get = new Get(Bytes.toBytes(row))
    get.addFamily(cfBytes)
    if (!cols.isEmpty) {
      cols foreach { col =>
        get.addColumn(cfBytes, Bytes.toBytes(col))
      }
    }
    val result = HTableUtil.use(pool, table) { htable => htable.get(get) }
    if(result.isDefined && result.get.list() != null){
       result.get.list().asScala.map { kv: KeyValue =>
        (Bytes.toString(kv.getQualifier()), Bytes.toString(kv.getValue()))
       }.toMap
    } else Map()
  }
  
  
  def getRowByCount(row: String, nColumn: Int): Map[String, String] = {
    val get = new Get(Bytes.toBytes(row))
    get.addFamily(cfBytes)
    // TODO: use ColumnCountGetFilter
    val cols = HTableUtil.topCols(nColumn)
    cols foreach { col =>
      get.addColumn(cfBytes, Bytes.toBytes(col))
    }
    val result = HTableUtil.use(pool, table) { htable => htable.get(get) }
    if(result.isDefined && result.get.list != null){
    	result.get.list.asScala.map { kv: KeyValue =>
    		(Bytes.toString(kv.getQualifier()), Bytes.toString(kv.getValue()))
    	}.toMap
     } else Map()
  }
  

  def getRows(rows: Iterable[String], cols: List[String] = List()): Map[String, Map[String, String]] = {
    val gets = rows map { row =>
      val get = new Get(Bytes.toBytes(row))
      get.addFamily(cfBytes)
      if (!cols.isEmpty) {
        cols foreach { col =>
          get.addColumn(cfBytes, Bytes.toBytes(col))
        }
      }
      get 
    }
    val results = HTableUtil.use(pool, table) { htable => htable.get(gets.toList.asJava) }
    if(results.isDefined){
        Map() ++ results.get.flatMap { result =>
           if(result.list != null){
        	   val value = Map() ++ result.list.asScala.map { kv: KeyValue =>
        	   		(Bytes.toString(kv.getQualifier()), Bytes.toString(kv.getValue()))
             }
             if(value.isEmpty) None
             else Some(Bytes.toString(result.getRow()) -> value)
           } else None
        } 
    }else Map()
  }

  def putRows(keyvalues: Traversable[(String, Map[String, Array[Byte]])]): Unit = {
    val putList = keyvalues.map { case (row, vs) =>
        val put = new Put(Bytes.toBytes(row))
        vs foreach { case (qualifier, value) =>
            put.add(cfBytes, Bytes.toBytes(qualifier), value)
        }
        put
    }.toList
    HTableUtil.use(pool, table) { htable => htable.put(putList.asJava) }
  }

  def putStrRows(keyvalues: Traversable[(String, Map[String, String])]): Unit = {
    val putList = keyvalues.map { case (row, vs) =>
        val put = new Put(Bytes.toBytes(row))
        vs foreach { case (qualifier, value) =>
            put.add(cfBytes, Bytes.toBytes(qualifier), Bytes.toBytes(value))
        }
        put
    }.toList

    HTableUtil.use(pool, table) { htable => htable.put(putList.asJava) }
  }

  def put(row: String, vs: Map[String, Array[Byte]]): Unit = {
    val put = new Put(Bytes.toBytes(row))
    vs foreach { case (qualifier, value) =>
        put.add(cfBytes, Bytes.toBytes(qualifier), value)
    }
    HTableUtil.use(pool, table) { htable => htable.put(put) }
  }

  def putStr(row: String, vs: Map[String, String]): Unit = {
    val put = new Put(Bytes.toBytes(row))
    vs foreach {
      case (qualifier, value) =>
        put.add(cfBytes, Bytes.toBytes(qualifier), Bytes.toBytes(value))
    }
    HTableUtil.use(pool, table) { htable => htable.put(put) }
  }
}

object HbaseStore extends Loggable {
  def main(args: Array[String]) {
    val htablePool = HTableUtil.createHTablePool("192.168.1.93,192.168.1.94,192.168.1.95",100)
    val metadata = new HbaseTable(htablePool, "crawl", "metadata")
    val searchTop = new HbaseTable(htablePool, "search", "top")
//     metadata.putStr("row1", Map("title" -> "标题1", "url" -> "http://www.bshare.cn/1"), "metadata")
//        metadata.putStr("row1", Map("title" -> "标题2", "url" -> "http://www.bshare.cn/2"), "metadata")
//           metadata.putStr("row1", Map("title" -> "标题3", "url" -> "http://www.bshare.cn/3"), "metadata")
//      metadata.putStrRows(List(
//          ("row1", Map("title" -> "标题2", "url" -> "http://www.bshare.cn/2")),
//          ("row1", Map("title" -> "标题3", "url" -> "http://www.bshare.cn/3")),
//          ("row1", Map("title" -> "标题3", "url" -> "http://www.bshare.cn/4"))))
//    println(metadata.getRow("row1"))
//    println(metadata.getRow("row1", List("title")))
//    println(metadata.getRows(List("row1", "row2")))
//    println(metadata.topN("row1", "title", 2, "metadata"))
//    metadata.putStrRows(List(("row1", Map("title" -> "标题2", "url" -> "http://www.bshare.cn/5"))))
    println(searchTop.getRowByCount("http://bbs.voc.com.cn", 3))
//    metadata.getRow("row1") foreach println
//    metadata.topN("row1", "url", 10) foreach println
    //	println(tableStore.topCols(10))
  }
}