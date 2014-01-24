package com.buzzinate.lezhi.util
import java.util.regex.Pattern

import java.util.regex._

object UrlFilter {

  val FILTER_PARAMS = Array(
    Pattern.compile("^blz-.*$"), // lezhi track
    Pattern.compile("^bsh_.*$"), // bshare track
    Pattern.compile("^utm_.*$"), // google track
    Pattern.compile("^bdclkid$"), // baidu track
    Pattern.compile("^jtss.*$"), // jiathis track
    Pattern.compile("^request_locale$"),
    Pattern.compile("^lang$"),
    Pattern.compile("^lange$"),
    Pattern.compile("^langpair$"))

  def filter(orgUrl: String): String = {
    val hashPos = orgUrl.indexOf('#')
    val url =
      if (hashPos > 0) orgUrl.substring(0, hashPos)
      else orgUrl

    val quesPos = url.indexOf('?')
    if (quesPos < 0) return url

    val mainUrl = url.substring(0, quesPos)
    val params = url.substring(quesPos + 1).split("&")
    val filteredParams = params.filter { p =>
      val pname = p.split('=')(0)
      FILTER_PARAMS.forall { fp =>
        val m = fp.matcher(pname)
        !m.matches
      }
    }

    var resUrl: StringBuilder = new StringBuilder(mainUrl)
    if (!filteredParams.isEmpty) resUrl.append("?")
    filteredParams.sorted.foreach { p =>
      resUrl.append(p + "&")
    }
    if (resUrl.last == '&')
      resUrl.deleteCharAt(resUrl.size - 1)
    resUrl.toString
  }

  def main(args: Array[String]): Unit = {
    val url = "http://www.36kr.com/p/118424/#utm_source=krweeklyw81c&a=3"
    println(UrlFilter.filter(url))
  }
}