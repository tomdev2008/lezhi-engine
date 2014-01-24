package org.apache.flume.thrift

import com.twitter.finagle.Service
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.builder.ClientBuilder
import java.net.InetSocketAddress
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.thrift.protocol.TBinaryProtocol
import collection.JavaConverters._
import com.alibaba.fastjson.JSON
import java.nio.ByteBuffer
import scala.collection.mutable.ListBuffer

object TestThrift {
  def main(args: Array[String]): Unit = {
    val service: Service[ThriftClientRequest, Array[Byte]] = ClientBuilder()
    .hosts(new InetSocketAddress("192.168.1.234", 41414))
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(20)
    .build()

    val client = new ThriftSourceProtocol.FinagledClient(service, new TBinaryProtocol.Factory())
    
    val events = new ListBuffer[ThriftFlumeEvent]
    for (i <- 0 until 10000) {
      events += viewlog(i, "Hello Flume!")
      events += clicklog(i, "click flume")
      if (events.size > 100) {
        client.appendBatch(events.result) onSuccess { r =>
          println(r)
        }
        events.clear
      }
    }
    if (events.size > 0) {
      client.appendBatch(events.result) onSuccess { r =>
        println(r)
      } 
    }
  }
  
  def viewlog(i: Int, data: String): ThriftFlumeEvent = {
    val userid = "user-" + (i % 100)
    val url = "url-" + i
    val title = "title-" + i
    val keywords = "keywords-" + i
    val map = Map("userid" -> userid, "url" -> url, "siteprefix" -> "sp", "title" -> title, "keywords" -> keywords, "timestamp" -> System.currentTimeMillis)
    val json = JSON.toJSONBytes(map.asJava)
    ThriftFlumeEvent(Map("category" -> "viewlog"), ByteBuffer.wrap(json))
  }
  
  def clicklog(i: Int, data: String): ThriftFlumeEvent = {
    val userid = "user-" + (i % 100)
    val fromurl = "url-from-" + i
    val tourl = "url-to-" + i
    val map = Map("userid" -> userid, "fromurl" -> fromurl, "tourl" -> tourl, "timestamp" -> System.currentTimeMillis)
    val json = JSON.toJSONBytes(map.asJava)
    ThriftFlumeEvent(Map("category" -> "clicklog"), ByteBuffer.wrap(json))
  }
}