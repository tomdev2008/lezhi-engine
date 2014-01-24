package com.buzzinate.flash

import org.apache.commons.lang.StringUtils
import scala.xml._
import java.net.URLDecoder
import com.twitter.util.Future
import com.buzzinate.http.HttpScheduler
import com.buzzinate.http.HttpResponse
import org.apache.commons.httpclient.HeaderGroup
import com.buzzinate.http.Http
import com.buzzinate.http.DefaultHttpScheduler
import java.util.concurrent.Executors

object FlashThumbnail {  
  def getThumbnail(client: HttpScheduler, swf: String) = {
    swf match {
      case url if (url.contains("youku.com")) => getYoukuThumbnail(client, swf)
      case url if (url.contains("6.cn")) => get6cnThumbnail(client, swf)
      case url if (url.contains("56.com")) => get56Thumbnail(client, swf)
      case url if (url.contains("tudou.com")) => getTudouThumbnail(client, swf)
      case _ => Future.value(null)
    }
  }
  
  def getYoukuThumbnail(client: HttpScheduler, swf: String): Future[String] = {
    val vids = StringUtils.substringBetween(swf, "/sid/", "/v.swf")
    val jsonurl = "http://v.youku.com/player/getPlayList/VideoIDS/" + vids
    client.get(jsonurl, false, Some(swf)).map { resp =>
      val json = HttpResponse.toHtml(jsonurl, resp).html
      StringUtils.replace(StringUtils.substringBetween(json, "\"logo\":\"", "\""), "\\/", "/")
    }
  }
  
  def get6cnThumbnail(client: HttpScheduler, swf: String): Future[String] = {
    val vid = StringUtils.substringBetween(swf, "/p/", ".swf")
    val xmlurl = "http://6.cn/v72.php?vid=" + vid
    client.get(xmlurl, false, Some(swf)).map { resp =>
      val xml = HttpResponse.toHtml(xmlurl, resp).html
      XML.loadString(xml) \ "VIDEO" find { node => node.toString.contains(vid) } map { node =>
        StringUtils.substringBetween(node.toString, "<pic>", "</pic>")
      } getOrElse(null)
    }
  }
  
  def get56Thumbnail(client: HttpScheduler, swf: String): Future[String] = {
    val vid = StringUtils.substringBetween(swf, "com/v_", "swf")
    val jsonurl = "http://vxml.56.com/json/" + vid + "/?src=out"
    client.get(jsonurl, false, Some(swf)).map { resp =>
      val json = HttpResponse.toHtml(jsonurl, resp).html
      StringUtils.replace(StringUtils.substringBetween(json, "\"bimg\":\"", "\""), "\\/", "/")
    }
  }
  
  def getTudouThumbnail(client: HttpScheduler, swf: String): Future[String] = {
    client.get(swf, false, Some(swf)).map { resp =>
      val headers = new HeaderGroup
      headers.setHeaders(resp.headers)
      val loc = headers.getFirstHeader("Location")
      if (loc != null) {
        val pic = StringUtils.substringBetween(loc.getValue, "snap_pic=", "&")
        URLDecoder.decode(pic, "UTF-8")
      } else null
    }
  }
  
  def main(args: Array[String]): Unit = {
    val client = new DefaultHttpScheduler(Http.buildAgent(10, 2), Executors.newFixedThreadPool(12))
    println(getYoukuThumbnail(client, "http://player.youku.com/player.php/sid/XNDY5MDM5OTU2/v.swf").get)
    println(get6cnThumbnail(client, "http://6.cn/p/p0QgJ9v_0YfxfRLVVRwysA.swf").get)
    println(get56Thumbnail(client, "http://player.56.com/v_NTk2OTU2MjI.swf").get)
    println(getTudouThumbnail(client, "http://www.tudou.com/v/5MVpdq2CkvE/&resourceId=0_04_02_99&autoPlay=true/v.swf").get)
  }
}