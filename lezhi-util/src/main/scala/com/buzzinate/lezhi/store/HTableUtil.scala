package com.buzzinate.lezhi.store

import org.apache.hadoop.hbase.client.HTablePool
import org.apache.hadoop.hbase.client.HTableInterface
import com.buzzinate.lezhi.util.Loggable
import org.apache.hadoop.hbase.HBaseConfiguration
import scala.collection.mutable.ListBuffer

object HTableUtil extends Loggable {
  def use[T](pool: HTablePool, table: String)(f: HTableInterface => T): Option[T] = {
    val htable = pool.getTable(table)
    try {
      Some(f(htable))
      //TODO: 异常时，怎样返回
    } finally {
      htable.close
    }
  }

  def createHTablePool(hbaseZookeeperQuorum: String, hTableReference: Int): HTablePool = {
    val conf = HBaseConfiguration.create()
    conf.set("hbase.zookeeper.quorum", hbaseZookeeperQuorum)
    new HTablePool(conf, hTableReference)
  }
  
  def topCols(count: Int): List[String] = {
    val cols = new ListBuffer[String]
    for(i <- 0 until count){
      if(i < 10) cols += "0" + i else cols += String.valueOf(i)
    }
    cols.toList
  }
}