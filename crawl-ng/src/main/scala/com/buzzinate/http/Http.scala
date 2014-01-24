package com.buzzinate.http

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager
import org.apache.commons.httpclient.params.HttpClientParams
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler
import java.util.concurrent.Executors
import org.jsoup.Jsoup
import org.apache.commons.httpclient.cookie.CookiePolicy

object Http {
   def buildAgent(maxTotalConns: Int, maxConnsPerRoute: Int): HttpClient = {
     val connManager = new MultiThreadedHttpConnectionManager
     val connParams = connManager.getParams
     connParams.setDefaultMaxConnectionsPerHost(maxConnsPerRoute)
     connParams.setMaxTotalConnections(maxTotalConns)
     
     val client = new HttpClient(new HttpClientParams, connManager)
     val params = client.getParams
     params.setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false))
     params.setParameter(HttpMethodParams.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES)
     params.setParameter(HttpMethodParams.USER_AGENT, "Yahoo! Slurp China")
     params.setSoTimeout(8000)
     params.setConnectionManagerTimeout(0)
     
     client
  }
   
  def build(maxTotalConns: Int, maxConnsPerRoute: Int): HttpClient = {
    val connManager = new MultiThreadedHttpConnectionManager
    val connParams = connManager.getParams
    connParams.setDefaultMaxConnectionsPerHost(maxConnsPerRoute)
    connParams.setMaxTotalConnections(maxTotalConns)
     
    val client = new HttpClient(new HttpClientParams, connManager)
    
    
    val params = client.getParams()
    params.setSoTimeout(20000)
    params.setConnectionManagerTimeout(30000)
    
    client
  }
  
  def main(args: Array[String]): Unit = {
    val client = buildAgent(100, 2)
    val scheduler = new PolicyHttpScheduler(client, Executors.newFixedThreadPool(64), 100, 100)
    List("http://www.docin.com/p-60527144.html", "http://www.docin.com/p-7176218.html", "http://www.docin.com/p-546960416.html", "http://t.cn/zT2vcUA", "http://changchun.51chudui.com/Class/Class.asp?DB18=%28100000-200000%29%u5143&ID=438") foreach { url =>
      scheduler.get(url, true) onSuccess { resp =>
        val htmlresp = resp.toHtml(url)
        println(htmlresp.lastRedirectedUri)
        println(Jsoup.parse(htmlresp.html, url).title())
      } onFailure { t =>
        t.printStackTrace()
      }
    }
  }
}