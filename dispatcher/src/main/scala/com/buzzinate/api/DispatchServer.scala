package com.buzzinate.api

import com.buzzinate.util.{Loggable, Config}
import com.buzzinate.es.ESClient
import com.buzzinate.cassandra.Cassandra
import com.buzzinate.dispatcher.Dispatch
import org.apache.thrift.protocol.TBinaryProtocol
import com.twitter.finagle.builder.{ServerBuilder, Server}
import java.net.InetSocketAddress
import com.twitter.finagle.thrift.ThriftServerFramedCodec

object DispatchServer extends Loggable {
  def main(args: Array[String]): Unit = {
    val config = Config.getConfig("config.properties")
    val port = config.getInt("dispatch.port", 41414)

    val es = new ESClient("lezhi", config.getList("elastic.hosts"))
    val cass = new Cassandra("lezhi", config.getString("cassandra.hosts"))

    val dispatchImpl = new DispatchImpl(es, cass)

    val service = new Dispatch.FinagledService(dispatchImpl, new TBinaryProtocol.Factory())
    val server: Server = ServerBuilder()
      .name("dispatcher")
      .bindTo(new InetSocketAddress(port))
      .codec(ThriftServerFramedCodec())
      //      .logger(logger())
      .build(service)

    info("Dispatcher started, port: " + port)
    Runtime.getRuntime().addShutdownHook(new Thread() {
      override def run(): Unit = {
        service.close()
        es.close
        cass.close
        info("shutdown dispatcher ...")
      }
    })
  }
}