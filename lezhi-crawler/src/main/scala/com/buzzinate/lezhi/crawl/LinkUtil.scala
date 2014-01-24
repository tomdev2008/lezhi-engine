package com.buzzinate.lezhi.crawl

import scala.collection.mutable.HashMap
import org.apache.commons.lang.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import com.buzzinate.lezhi.util.URLCanonicalizer
import com.buzzinate.lezhi.util.UrlFilter
import com.buzzinate.util.TextUtil
import java.util.regex.Pattern

object LinkUtil {
  val ipreg = Pattern.compile("([0-9]|\\.|:)*")
  
  def extractLinks(url: String, doc: Document, urlfilter: String => Boolean): HashMap[String, String] = {
    val hrefs = new HashMap[String, String]
    val es = doc.body().getElementsByTag("a")
    for (i <- 0 until es.size()) {
      val e = es.get(i)
      val href = UrlFilter.filter(URLCanonicalizer.getCanonicalURL(e.attr("href"), url))

      var isimg = false
      val img = e.select("img").first
      if (img != null) {
        val src = UrlFilter.filter(URLCanonicalizer.getCanonicalURL(img.attr("src"), url))
        if (href == src) isimg = true
        else {
          val path = StringUtils.substringBeforeLast(src, "/")
          val format = StringUtils.substringAfterLast(src, ".")
          val filename = StringUtils.substring(src, path.length + 1, src.length - path.length - format.length - 1)
          val lcs = TextUtil.findLcs(filename, href)
          if (href.startsWith(path) && href.endsWith(format) && lcs.length * 3 > filename.length) isimg = true
        }
      }
      if (!isimg && isValidUrl(href) && urlfilter(href)) hrefs += href -> e.text
    }
    hrefs.remove(url)
    hrefs
  }

  def verifyPrefix(url: String, prefix: String): Boolean = url.startsWith(prefix)

  private def isValidUrl(href: String): Boolean = {
    href.startsWith("http://") && href.indexOf("http:/", 1) == -1 && href.indexOf("http%3A%2F%2F", 1) == -1
  }

  def shortenUrlPrefix(urlprefix: String) = {
    var i = urlprefix.length - 1
    var digitslash = true
    while (i >= 0 && digitslash) {
      val ch = urlprefix.charAt(i)
      digitslash = Character.isDigit(ch) || ch == '/' || ch == '-'
      i -= 1
    }
    urlprefix.substring(0, i + 2)
  }
  
  def isIP(host: String): Boolean = {
    ipreg.matcher(host).matches()
  }

  def main(args: Array[String]): Unit = {
    println(isIP("www.134.com"))
    println(isIP("lz.bshare.cn"))
    println(isIP("123.134.23"))
    println(isIP("123.134.23:8080"))
    val url = "http://www.minglu.info/"
    val doc = Jsoup.connect(url).get
    for (href <- extractLinks(url, doc, x => verifyPrefix(x, "http://www.minglu.info/"))) println(href)
  }
}