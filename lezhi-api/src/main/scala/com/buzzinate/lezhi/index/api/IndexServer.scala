package com.buzzinate.lezhi.index.api

import java.net.InetSocketAddress
import org.apache.thrift.protocol.TBinaryProtocol
import com.buzzinate.lezhi.elastic.RichClient
import com.buzzinate.lezhi.util.Constants._
import com.buzzinate.lezhi.store.DocStatus
import com.buzzinate.lezhi.elastic.ClientWrapper
import org.apache.hadoop.hbase.client.HTablePool
import org.apache.hadoop.hbase.HBaseConfiguration
import com.buzzinate.lezhi.store.DocStatus
import com.buzzinate.lezhi.thrift.ElasticIndexService
import com.buzzinate.lezhi.util.Config
import com.buzzinate.lezhi.util.Loggable
import com.twitter.finagle.builder.Server
import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.buzzinate.lezhi.store.HTableUtil

object IndexServer extends Loggable{
  def main(args: Array[String]): Unit = {
    val prop = Config.getConfig("config.properties")
    val client = new RichClient(prop.getString(ELASTIC_SEARCH_CLUSTER_KEY), prop.getList(ELASTIC_SEARCH_HOSTS_KEY)) with ClientWrapper
    val htablePool = HTableUtil.createHTablePool(prop.getString(HBASE_ZKQ_KEY), prop.getInt(HBASE_MAX_POOL_KEY, 100))
    try {
      val docStatus = new DocStatus(htablePool)
      val IndexFuture = new IndexServiceImpl(client, docStatus)
      val service = new ElasticIndexService.FinagledService(IndexFuture, new TBinaryProtocol.Factory)
      
      val server: Server = ServerBuilder()
      	.name("elastic index")
      	.bindTo(new InetSocketAddress(32124))
      	.codec(ThriftServerFramedCodec())
      	.build(service)
      info("start index Service, port:32124")
      
      Runtime.getRuntime().addShutdownHook(new Thread() {
        override def run(): Unit = {
          server.close()
          client.close
          htablePool.close()
          info("shutdown index Service, port:32124")
        }
      })
    } catch {
      case e: Exception => e.printStackTrace
    }
  }
}
