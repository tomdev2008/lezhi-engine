package com.buzzinate.lezhi.api

import com.buzzinate.lezhi.thrift._
import org.apache.thrift.protocol.TBinaryProtocol
import com.buzzinate.lezhi.Servers
import com.twitter.finagle.builder.ServerBuilder
import java.net.InetSocketAddress
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.buzzinate.lezhi.util.Loggable

object RecommendServer extends Loggable {
    def main(args: Array[String]): Unit = {
        try {
            val recommendFuture = new RecommendServiceImpl
            val service = new RecommendServices.FinagledService(recommendFuture, new TBinaryProtocol.Factory)
            ServerBuilder()
                .name("recommend")
                .bindTo(new InetSocketAddress(32123))
                .codec(ThriftServerFramedCodec())
                .build(service)
            info("start lezhi recommend service, port:32123")
            Runtime.getRuntime().addShutdownHook(new Thread() {
                override def run(): Unit = {
                    service.close()
                    Servers.htablePool.close
                    Servers.client.close
                    info("shutdown lezhi recommend service,port:32123")
                }
            })
        } catch {
            case e: Exception => error("Could not initialize service", e)
        }
    }
}