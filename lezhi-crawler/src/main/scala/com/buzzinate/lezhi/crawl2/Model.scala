package com.buzzinate.lezhi.crawl2

case class WebContent(realurl: String, canonicalUrl: Option[String], rawTitle: String, title: String, html: String, metaKeywords: String, statusCode: Int, lastModified: Long) {
  def isOK(): Boolean = statusCode >= 200 && statusCode < 300
}

case class Thumbnail(imgsrc: String, data: Array[Byte], format: String)

object Thumbnail {
  val Null = Thumbnail(null, null, null)
}