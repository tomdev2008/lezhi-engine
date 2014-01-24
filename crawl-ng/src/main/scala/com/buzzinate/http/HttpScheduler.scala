package com.buzzinate.http

import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.HostConfiguration
import com.twitter.util.Future
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.GetMethod

trait HttpScheduler {
  val client: HttpClient
  
  def get(url: String, followRedirects: Boolean = true, reffer: Option[String] = None): Future[HttpResponse] = {
    val get = new GetMethod(url)
    
    // handle 51chudui case, make sure use the same connection
    val hostconfig = client.getHostConfiguration().clone().asInstanceOf[HostConfiguration]
	  hostconfig.setHost(get.getURI)
	  val host = hostconfig.getHost()
	  if (host.contains("51chudui.com")) {
	    hostconfig.setHost("www.51chudui.com")
	    get.addRequestHeader("Host", host)
	  }

    if (host.contains("hexun.com")) {
      get.setRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.43 Safari/537.31")
    }
    
    get.setFollowRedirects(followRedirects)
    reffer.map { r => get.addRequestHeader("Referer", r) }
    
    submit(hostconfig, get)
  }
  
  def submit(method: HttpMethod): Future[HttpResponse] = {
    val hostconfig = client.getHostConfiguration().clone().asInstanceOf[HostConfiguration]
	hostconfig.setHost(method.getURI)
	submit(hostconfig, method)
  }
  
  def submit(hostconfig: HostConfiguration, method: HttpMethod): Future[HttpResponse]
}