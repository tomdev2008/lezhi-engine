package com.buzzinate.lezhi.tools

import collection.JavaConverters._
import org.jsoup.Jsoup
import com.buzzinate.http.{Http, PolicyHttpScheduler}
import java.util.concurrent.Executors
import com.buzzinate.lezhi.crawl.Vars
import com.buzzinate.crawl.{Item, FrontierClient}
import com.buzzinate.lezhi.util.ItemConverter

object FeedUrls {
  val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")
  val vars = new Vars(prop.asInstanceOf[java.util.Map[String, String]].asScala.toMap)
  val frontierClient = new FrontierClient(prop.getProperty("frontier.host", "localhost"))

  val http = new PolicyHttpScheduler(Http.buildAgent(100, 2), Executors.newFixedThreadPool(64), 100, 100)

  def main(args: Array[String]): Unit = {
    if (args.length < 1) return println("usage: FeedUrls <start url>")
    val startUrl = args(0)
    val siteprefix = parseSitePrefix(startUrl)

    println("Feed " + siteprefix + " start from " + startUrl)
    val items = suburls(startUrl, siteprefix) map { url =>
      Item(url, Map("siteprefix" -> siteprefix))
    }

    frontierClient.client.offer(ItemConverter.asThrift(items.toList))
  }

  def suburls(startUrl: String, prefix: String): List[String] = {
    val resp = http.get(startUrl).get.toHtml(startUrl)
    if (resp.ok) {
      val doc = Jsoup.parse(resp.html, startUrl)
      doc.getElementsByTag("a").asScala.map { e =>
        e.absUrl("href")
      }.toList filter { url =>
        url.startsWith(prefix)
      }
    } else List()
  }

  @inline
  def parseSitePrefix(url: String): String = {
    val idx = url.indexOf('/', "http://".length + 1)
    url.substring(0, idx)
  }
}