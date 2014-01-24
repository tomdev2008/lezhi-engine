package com.buzzinate.lezhi.util

import java.util.regex.Pattern
import org.apache.commons.lang.StringUtils

object UrlPagination {
  val pageReg = Pattern.compile("\\d+(_\\d{0,3})")
  
  // TODO: Fix ME 判断分页
  def normal(url: String) = {
    val start = math.max(0, url.lastIndexOf("/"))
    val m = pageReg.matcher(url.substring(start))
    if (m.find() && m.groupCount() >= 1) StringUtils.substring(url, 0, start + m.start(1)) + StringUtils.substring(url, start + m.end(1))
    else url
  }
  
  def hasPage(url: String): Boolean = {
    val start = math.max(0, url.lastIndexOf("/"))
    val m = pageReg.matcher(url.substring(start))
    m.find() && m.groupCount() >= 1
  }
  
  def getPageNum(url: String): Option[Int] = {
    val start = math.max(0, url.lastIndexOf("/"))
    val m = pageReg.matcher(url.substring(start))
    if (m.find() && m.groupCount() >= 1) {
      val pagestr = m.group(1)
      Some(pagestr.substring(1).toInt)
    } else None
  }
  
  def refineTitle(url: String, title: String) = {
    val start = math.max(0, url.lastIndexOf("/"))
    val m = pageReg.matcher(url.substring(start))
    val page = if (m.find() && m.groupCount() >= 1) {
      val pagestr = m.group(1)
      pagestr.substring(1)
    } else "1"
    List("(" + page + ")", "（" + page + "）", "[" + page + "]") map { pagesuff =>
      if (title.endsWith(pagesuff)) title.substring(0, title.length - pagesuff.length)
      else title
    } minBy { t => t.length }
  }
  
  def main(args: Array[String]): Unit = {
    val url = "http://e.gmw.cn/2012-06/16/content_4364055_2.htm"
    println(normal(url))
    println(hasPage("http://mobile.it168.com/a2013/0308/1460/000001460216_2.shtml"))
    println(refineTitle(url, "郑雅律生前遭猥亵照片曝光 韩星生存环境大揭密(2)"))
    println(refineTitle("http://fm.m4.cn/2012-10/1187024_2.shtml", "对学校午餐不满意？看看韩国的供餐吧！（2）"))
    println(refineTitle("http://www.chinadaily.com.cn/hqzx/2012-12/17/content_16022153_2.htm", "日本自民党重夺政权 安倍将任新首相[2]"))
  }
}