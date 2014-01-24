//package com.buzzinate.http
//
//import java.net.URI
//import java.text.SimpleDateFormat
//import java.util.Locale
//import java.util.zip.GZIPInputStream
//import com.buzzinate.charset.MultiCharsetDetector
//import java.io.ByteArrayOutputStream
//import org.apache.commons.io.IOUtils
//import com.buzzinate.crawl.core.detect.CharsetDetector
//import com.buzzinate.crawl.core.detect.MetaCharsetDetector
//import com.buzzinate.crawl.core.detect.MozillaCharsetDetector
//import collection.JavaConverters._
//import javax.imageio.ImageIO
//import java.awt.image.BufferedImage
//import java.text.ParseException
//
//
//case class BytesResp(lastRedirectedUri: Option[String], statusCode: Int, data: Array[Byte], contentType: ContentType, header: Map[String, Set[String]]) {
//  def toHtml(): HtmlResp = {
//    if (data != null) {
//      val mcd = new MultiCharsetDetector
//        
//      if (contentType != null && contentType.getCharset != null) mcd.addCharset(contentType.getCharset.toString)
//      mcd.addCharset(MozillaCharsetDetector.detect(data))
//      for (metacharset <- MetaCharsetDetector.sniffCharacterEncoding(data).asScala) mcd.addCharset(metacharset)
//      val cd = new CharsetDetector
//      cd.setText(data)
//      for (m <- cd.detectTop().asScala) {
//        val coreCharset = m.getName
//        val weight = m.getConfidence() / 100d
//        mcd.addCharset(coreCharset, weight)
//      }
//
//      val realCharset = mcd.getCharset
//      val html = new String(data, realCharset)
//      HtmlResp(lastRedirectedUri, statusCode, html, header)
//    } else HtmlResp(lastRedirectedUri, statusCode, null, header)
//  }
//}
//
//case class ImageResp(format: String, image: BufferedImage, time: Long)
//
//object HttpConverter {
//  val LAST_MOD_DF = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
//  
//  def toString(resp: HttpResponse): String = {
//    EntityUtils.toString(resp.getEntity)
//  }
//  
//  def toBytesResp(resp: HttpResponse): BytesResp = {
//    val code = resp.getStatusLine.getStatusCode
//    val headers = (Map.empty[String, Set[String]] /: resp.getAllHeaders) { (m, h) =>
//      val vs = m.getOrElse(h.getName, Set.empty) + h.getValue
//      m + (h.getName -> vs)
//    }
//    val entOpt = if (resp.getEntity == null) None else Some(resp.getEntity)
//    try {
//      entOpt.map { ent =>
//        val stm = (ent.getContent, ent.getContentEncoding) match {
//          case (stm, null) => stm
//          case (stm, enc) if enc.getValue == "gzip" => new GZIPInputStream(stm)
//          case (stm, _) => stm
//        }
//        
//        val os = new ByteArrayOutputStream
//        IOUtils.copy(stm, os)
//        
//        val lastUrl = headers.get(Http.FINAL_URL).map(vs => vs.head) 
//        BytesResp(lastUrl, code, os.toByteArray, ContentType.get(ent), headers)
//      } getOrElse BytesResp(None, code, Array.empty[Byte], null, headers)
//    } finally {
//      entOpt.foreach(ent => EntityUtils.consume(ent))
//    }
//  }

//  @Deprecated("performance issue")
//  def toHtml(resp: HttpResponse): HtmlResp = {
//    val code = resp.getStatusLine.getStatusCode
//    val headers = (Map.empty[String, Set[String]] /: resp.getAllHeaders) { (m, h) =>
//      val vs = m.getOrElse(h.getName, Set.empty) + h.getValue
//      m + (h.getName -> vs)
//    }
//    val entOpt = if (resp.getEntity == null) None else Some(resp.getEntity)
//    try {
//      entOpt.map { ent =>
//        val stm = (ent.getContent, ent.getContentEncoding) match {
//          case (stm, null) => stm
//          case (stm, enc) if enc.getValue == "gzip" => new GZIPInputStream(stm)
//          case (stm, _) => stm
//        }
//        
//        val mcd = new MultiCharsetDetector
//        
//        val os = new ByteArrayOutputStream
//        IOUtils.copy(stm, os)
//        val bs = os.toByteArray()
//        val ct = ContentType.get(ent)
//        if (ct != null) mcd.addCharset(ct.getCharset.toString)
//        mcd.addCharset(MozillaCharsetDetector.detect(bs))
//        for (metacharset <- MetaCharsetDetector.sniffCharacterEncoding(bs).asScala) mcd.addCharset(metacharset)
//        val cd = new CharsetDetector
//        cd.setText(bs)
//        for (m <- cd.detectTop().asScala) {
//          val coreCharset = m.getName
//          val weight = m.getConfidence() / 100d
//          mcd.addCharset(coreCharset, weight)
//        }
//
//        val realCharset = mcd.getCharset
//        val lastUrl = headers.get(Http.FINAL_URL).map(vs => vs.head) 
//        HtmlResp(lastUrl, code, new String(bs, realCharset), headers)
//      } getOrElse HtmlResp(None, code, null, headers)
//    } finally {
//      entOpt.foreach(ent => EntityUtils.consume(ent))
//    }
//  }
  
//  def toImage(resp: HttpResponse): ImageResp = {
//    val code = resp.getStatusLine.getStatusCode
//    try {
//      val contentType = resp.getFirstHeader("Content-Type").getValue
//      val format = contentType.substring("image/".length)
//      val img = ImageIO.read(resp.getEntity.getContent)
//      val lastModified = resp.getFirstHeader("Last-Modified")
//      if (lastModified != null) ImageResp(format, img, parseLastModified(lastModified.getValue))
//      else ImageResp(format, img, -1L)
//    } finally {
//      if (resp.getEntity != null) EntityUtils.consume(resp.getEntity)
//    }
//  }
//  
//  def parseLastModified(lastModified: String): Long = {
//    if (lastModified == null) return -1L
//    try {
//      LAST_MOD_DF.parse(lastModified).getTime()
//	} catch {
//	  case e: ParseException => { -1L }//Ignore
//	}
//  }
//}