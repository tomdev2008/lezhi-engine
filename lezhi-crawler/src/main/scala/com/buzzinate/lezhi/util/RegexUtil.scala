package com.buzzinate.lezhi.util

import java.util.UUID
import scala.util.matching.Regex
import scala.collection.mutable.ListBuffer

/**
 * @author jeffrey created on 2012-9-11 下午2:45:04
 *
 */
case class HtmlText(url: String, htmlText: String)

object RegexUtil extends Loggable {
  val pattern = new Regex("http://lzstatic\\.bshare\\.cn/plugin/lz.*uuid=([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})")
  val NON_UUID = "non_uuid"
    
  def getUuid(htmls: Iterable[HtmlText]): Map[String, String] = {
    val missurls = new ListBuffer[String]
    val url2uuids = htmls map { case HtmlText(url, htmlText) =>
      val res = pattern.findFirstMatchIn(htmlText)
      try {
        if (res.getOrElse(NON_UUID) != NON_UUID) {
          (url, res.get.group(1))
        } else {
          missurls += url
          (url, NON_UUID)
        }
      } catch {
        case ex: Exception =>
          error("Failed to  get the matched uuid from url:" + url, ex);
          (url, NON_UUID)
      }
    }
    if (missurls.size > 0) info("Cannot get the matched uuid from url:" + missurls)
    Map() ++ url2uuids
  }

  def getUuid(url: String, htmlText: String): String = {
    getUuid(List(HtmlText(url, htmlText))).getOrElse(url, NON_UUID)
  }

  def main(args: Array[String]): Unit = {
    println(getUuid("""http://blog.zzsmo.com/2012/07/gxyushhpz/""", """<script type="text/javascript" charset="utf-8" src="http://lzstatic.bshare.cn/plugin/lzChinaZTmp.js#uuid=6ee942be-d897-4f60-9a70-7acc2035c7a0"></script>"""))
  }
}