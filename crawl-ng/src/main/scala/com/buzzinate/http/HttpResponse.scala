package com.buzzinate.http

import collection.JavaConverters._
import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.HttpMethod
import java.util.zip.GZIPInputStream
import java.io.ByteArrayOutputStream
import org.apache.commons.io.IOUtils
import com.buzzinate.charset.MultiCharsetDetector
import com.buzzinate.crawl.core.detect.CharsetDetector
import com.buzzinate.crawl.core.detect.MetaCharsetDetector
import com.buzzinate.crawl.core.detect.MozillaCharsetDetector
import java.text.SimpleDateFormat
import java.util.Locale
import org.apache.commons.httpclient.HeaderGroup
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.text.ParseException
import org.apache.commons.lang.StringUtils
import java.awt.image.BufferedImage

case class HttpResponse(lastUrl: String, statusCode: Int, entity: Array[Byte], headers: Array[Header]) {
  def toHtml(url: String): HtmlResp = HttpResponse.toHtml(url, this)
  
  def toImage(): ImageResp = HttpResponse.toImage(this)
}

case class HtmlResp(lastRedirectedUri: Option[String], statusCode: Int, html: String, header: Map[String, Set[String]]) {
  def ok(): Boolean = statusCode >= 200 && statusCode <= 204
}

case class ImageResp(format: String, image: BufferedImage, time: Long)

object HttpResponse {
  val LAST_MOD_DF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
  
  def fromHttpMethod(method: HttpMethod): HttpResponse = {
    val isgzip = method.getResponseHeaders("Content-Encoding").exists(h => h.getValue == "gzip")
    val rawstream = method.getResponseBodyAsStream
    val stream = if (isgzip && rawstream != null) new GZIPInputStream(rawstream) else rawstream
    val entity = if (stream == null) null else {
      try {
        val os = new ByteArrayOutputStream
        IOUtils.copy(stream, os)
        os.toByteArray
      } finally {
        stream.close
      }
    }
    HttpResponse(method.getURI.toString, method.getStatusCode, entity, method.getResponseHeaders)
  }
  
  def toHtml(url: String, resp: HttpResponse): HtmlResp = {
    val lastRedirectedUri = if (url == resp.lastUrl) None else Some(resp.lastUrl)
    val data = resp.entity
    val headers = (Map.empty[String, Set[String]] /: resp.headers) { (m, h) =>
      val vs = m.getOrElse(h.getName, Set.empty) + h.getValue
      m + (h.getName -> vs)
    }
    
    if (data != null) {
      val mcd = new MultiCharsetDetector
        
      headers.getOrElse("Content-Type", Set.empty).headOption.foreach { contentType =>
        val charset = StringUtils.substringAfter(contentType, "charset=")
        if (StringUtils.isNotBlank(charset)) mcd.addCharset(charset)
      }
      mcd.addCharset(MozillaCharsetDetector.detect(data))
      for (metacharset <- MetaCharsetDetector.sniffCharacterEncoding(data).asScala) mcd.addCharset(metacharset)
      val cd = new CharsetDetector
      cd.setText(data)
      for (m <- cd.detectTop().asScala) {
        val coreCharset = m.getName
        val weight = m.getConfidence() / 100d
        mcd.addCharset(coreCharset, weight)
      }

      val realCharset = mcd.getCharset
      val html = new String(data, realCharset)
      HtmlResp(lastRedirectedUri, resp.statusCode, html, headers)
    } else HtmlResp(lastRedirectedUri, resp.statusCode, null, headers)
  }
  
  def toImage(resp: HttpResponse): ImageResp = {
    val headers = new HeaderGroup
    headers.setHeaders(resp.headers)
    val code = resp.statusCode
    val contentType = headers.getFirstHeader("Content-Type").getValue
    val format = contentType.substring("image/".length)
    val img = ImageIO.read(new ByteArrayInputStream(resp.entity))
    val lastModified = headers.getFirstHeader("Last-Modified")
    if (lastModified != null) ImageResp(format, img, parseLastModified(lastModified.getValue))
    else ImageResp(format, img, -1L)
  }
  
  def parseLastModified(lastModified: String): Long = {
    if (lastModified == null) return -1L
    try {
      LAST_MOD_DF.parse(lastModified).getTime()
	} catch {
	  case e: ParseException => { -1L }//Ignore
	}
  }
}