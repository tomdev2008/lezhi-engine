package com.buzzinate.crawl

import java.net.InetSocketAddress
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.buzzinate.thrift.frontier.UrlFrontier
import org.apache.thrift.protocol.TBinaryProtocol
import com.twitter.finagle.Service
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.commons.lang.StringUtils
import com.buzzinate.thrift.frontier.{Item => TItem}

class FrontierClient(host: String, port: Int = 31313) {
  val service: Service[ThriftClientRequest, Array[Byte]] = ClientBuilder()
    .hosts(new InetSocketAddress(host, port))
    .codec(ThriftClientFramedCodec())
    .hostConnectionLimit(20)
    .build()

  val client = new UrlFrontier.FinagledClient(service, new TBinaryProtocol.Factory())
  
  def makeClient = new UrlFrontier.FinagledClient(service, new TBinaryProtocol.Factory())
  
  def offer(id: String, queue: String, data: String, checkInterval: Int) = {
    client.offer(Seq(TItem(id, data, queue, Some(checkInterval)))).apply
  }
  
  def acks(ids: List[String]) = client.ack(ids.toSeq)
  
  def ack(id: String) = client.ack(Seq(id))

  def close() = service.close()
}

object FrontierClient {
}