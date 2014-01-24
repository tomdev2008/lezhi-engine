package com.buzzinate.dm.util

import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

object UrlPagination {
  val pageReg = Pattern.compile("\\d+(_\\d{0,3})\\.")
  
  def distinctUrls(urlscores: Iterable[(String, Double)]): List[(String, Double)] = {
    val url2score = new HashMap[String, Double]
    val normal2urlscores = new HashMap[String, ListBuffer[(String, Double)]].withDefault(x => new ListBuffer[(String, Double)])
    urlscores.toList.groupBy { case (url, _) => UrlPagination.normal(url) } map { case (_, uss) =>
      val maxScore = uss map { case (_, score) => score } max
      var minUrl = ""
      uss foreach { case (url, _) => 
        if (minUrl == "" || url < minUrl) minUrl = url
      }
      url2score += minUrl -> maxScore
    }
    url2score.toList
  }
  
  def distinctUrlsWithRawTitle(urlscoretitles: Iterable[(String, Double, String)]): List[(String, (Double, String))] = {
    val url2score = new HashMap[String, (Double, String)]
    val normal2urlscores = new HashMap[String, ListBuffer[(String, Double)]].withDefault(x => new ListBuffer[(String, Double)])
    urlscoretitles.toList.groupBy { case (url, _, rawTitle) => (UrlPagination.normal(url), rawTitle) } map { case (_, ust) =>
      val maxScore = ust map { case (_, score, _) => score } max
      var minUrl = ""
      var minRawTitle = ""
      ust foreach { case (url, _, rawTitle) => 
        if (minUrl == "" || url < minUrl) {minUrl = url
        	minRawTitle = rawTitle
        }
      }
      url2score += minUrl -> (maxScore, minRawTitle)
    }
    url2score.toList
  }
  
  // TODO: Fix ME 判断分页
  def normal(url: String) = {
    val m = pageReg.matcher(url)
    if (m.find() && m.groupCount() >= 1) {
      val pagestr = m.group(1)
      val idx = StringUtils.lastIndexOf(url, pagestr)
      StringUtils.substring(url, 0, idx) + StringUtils.substring(url, idx + pagestr.length)
    } else url
  }
  
  def refineTitle(url: String, title: String) = {
    val m = pageReg.matcher(url)
    val page = if (m.find() && m.groupCount() >= 1) {
      val pagestr = m.group(1)
      pagestr.substring(1)
    } else "1"
    List("(" + page + ")", "（" + page + "）") map { pagesuff =>
      if (title.endsWith(pagesuff)) title.substring(0, title.length - pagesuff.length)
      else title
    } minBy { t => t.length }
  }
  
  def main(args: Array[String]): Unit = {
    val rawurls = List("http://view.gmw.cn/2012-11/06/content_5592835.htm?1352173522", "http://view.gmw.cn/2012-11/06/content_5592835.htm", "http://zqb.cyol.com/html/2012-11/06/nw.D110000zgqnb_20121106_4-02.htm", "http://zqb.cyol.com/html/2012-11/06/nw.D110000zgqnb_20121106_4-02.htm?123")
    rawurls foreach { rawurl =>
      val url = if (rawurl.contains("gmw.cn")) StringUtils.substringBefore(rawurl, "?") else rawurl
      println(url)
    }
    println(distinctUrls(List("http://zqb.cyol.com/html/2012-11/06/nw.D110000zgqnb_20121106_1-02.htm" -> 1d, "http://zqb.cyol.com/html/2012-11/06/nw.D110000zgqnb_20121106_4-02.htm" -> 2d)))
    println(distinctUrls(List("http://e.gmw.cn/2012-06/16/content_4364055_2.htm" -> 1d, "http://e.gmw.cn/2012-06/16/content_4364055_1.htm" -> 2d)))
    
    val url = "http://e.gmw.cn/2012-06/16/content_4364055_2.htm"
    println(normal(url))
    println(refineTitle(url, "郑雅律生前遭猥亵照片曝光 韩星生存环境大揭密(2)"))
    println(refineTitle("http://fm.m4.cn/2012-10/1187024_2.shtml", "对学校午餐不满意？看看韩国的供餐吧！（2）"))
  }
}