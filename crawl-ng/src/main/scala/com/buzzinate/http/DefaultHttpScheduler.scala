package com.buzzinate.http

import org.apache.commons.httpclient.HostConfiguration
import org.apache.commons.httpclient.HttpMethod
import com.twitter.util.Future
import com.twitter.util.FuturePool
import org.apache.commons.httpclient.HttpClient
import java.util.concurrent.ExecutorService

class DefaultHttpScheduler(val client: HttpClient, threadPool: ExecutorService) extends HttpScheduler {
  val futurePool = FuturePool(threadPool)
  
  override def submit(hostconfig: HostConfiguration, method: HttpMethod): Future[HttpResponse] = {
    futurePool {
      try {
        client.executeMethod(hostconfig, method)
        HttpResponse.fromHttpMethod(method)
      } finally {
        method.releaseConnection
      }
    }
  }
}