package com.buzzinate.lezhi.crawl2

case class CountTime(num: Long, time: Long)

class DocCounting(maxNum: Long) {
  val host2count = new java.util.concurrent.ConcurrentHashMap[String, CountTime]
  
  def countOrElse(siteprefix: String, fun: => Long): Long = {
    // TODO: thread safe
    var count = host2count.get(siteprefix)
    if (count == null || count.num < maxNum && count.time + 1000 * 3600 > System.currentTimeMillis) {
      count = CountTime(fun, System.currentTimeMillis)
      host2count.put(siteprefix, count)
    }
    count.num
  }
}