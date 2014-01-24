package com.buzzinate.lezhi.util

import java.net.URL

import scala.Array.canBuildFrom
import org.apache.commons.lang.StringUtils

class URLDiff(baseUrl: String) {

  val CLUSTER_SIZE = 3
  val MIN_DIS = 1
  val MIN_BATCHSIZE = 10

  val url = new URL(baseUrl)
  val basePaths = getPathParts(url)
  val baseQueries = getQueryParts(url)

  def diff(url: String): Double = {
    val thatUrl = new URL(url)
    val paths = getPathParts(thatUrl)
    val queries = getQueryParts(thatUrl)
    FastUtil.diffPath(basePaths, paths) + diffQuery(baseQueries, queries)
  }

  def diff(thatUrl: URL): Double = {
    val paths = getPathParts(thatUrl)
    val queries = getQueryParts(thatUrl)
    FastUtil.diffPath(basePaths, paths) + diffQuery(baseQueries, queries)
  }

  private def diffQuery(parts: Set[String], parts2: Set[String]): Int = {
    //在目标url中出现但在对比url中没出现的param的权重会高一些
    val partsDiff = (parts &~ parts2).size * 4
    val parts2Diff = parts2.&~(parts).size * 2
    partsDiff + parts2Diff
  }

  private def getPathParts(url: URL): Array[String] = {
    StringUtils.split(FastUtil.formatNum(url.getPath()), "/")
  }

  private def getQueryParts(url: URL): Set[String] = {
    val urlQuery = if (url.getQuery() == null) "" else url.getQuery()
    StringUtils.split(urlQuery, "&") map { pm => getParamName(pm) } toSet
  }

  private def getParamName(str: String): String = {
    StringUtils.split(str, "=").head
  }
}

object URLDiff {
  
  val qUrls = List("http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-17/content_7267088.html",
    "http://www.chinadaily.com.cn/micro-reading/mfeed/hotwords/20121018540.html",
    "http://www.chinadaily.com.cn/hqzx/")

  val urls = List("http://fashion.chinadaily.com.cn/",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-18/content_7279084.html",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-01/content_7398998.html",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-20/content_7294576.html",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-20/content_7294576.html?2341231",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-30/content_7376505.html",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-21/content_7297750.html#blz-insite",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-20/content_7294663.html",
    "http://www.chinadaily.com.cn/hqcj/2012-11/05/content_15875958.htm",
    "http://www.chinadaily.com.cn/hqgj/zbyt/",
    "http://cbbs.chinadaily.com.cn/portal.php?mod=more&type=pic&bid=29",
    "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-20/content_723276.html?2341231")

  def main(args: Array[String]): Unit = {
    urls foreach { url =>
      println(FastUtil.formatNum(url))
    }
    
    val ud = new URLDiff("http://blog.zzsmo.com/2011/12/bshareguaishou")
    println(ud.diff("http://blog.zzsmo.com/2010/06/smo-part1-why-smo?uid=32"))
    println(ud.diff("http://blog.zzsmo.com/2010/06/smo-part1-why-smo?uid=32&3221"))
    println(ud.diff("http://blog.zzsmo.com/category/bshare-hezuohuoban/"))
    println(ud.diff("http://blog.zzsmo.com/"))

    val udc = new URLDiff("http://www.chinadaily.com.cn/micro-reading/ent/2012-11-05/content_7427222.html")
    println(udc.diff("http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-24/content_7324499_2.html"))
    println(udc.diff("http://www.chinadaily.com.cn/dfpd/dfwhyl/2012-10-18/content_7272568.html"))
    println(udc.diff("http://www.chinadaily.com.cn/micro-reading/ent/2012-09-01/content_6885628.html"))
  }
}