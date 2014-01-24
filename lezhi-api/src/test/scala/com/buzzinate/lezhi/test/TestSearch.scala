package com.buzzinate.lezhi.test

import com.buzzinate.lezhi.util.DomainNames
import org.apache.commons.lang.StringUtils
import com.buzzinate.lezhi.Servers
import com.buzzinate.lezhi.elastic.SearchRecommend
import com.buzzinate.lezhi.store.HbaseTable

object TestSearch {

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: TestSearch <url> <count>")
      return
    }
    val url = args(0)
    val count = args(1).toInt
//    val url = "http://e.gmw.cn/2012-04/28/content_4059642.htm"
    val host = DomainNames.safeGetHost(url)
    val siteprefix = StringUtils.substringBefore(url, host) + host
//    val siteprefix = "http://e.gmw.cn/2012-11"
//    val client = new Client("192.168.1.136")
//    println(client.numDocs("http://e.gmw.cn"))
//    println(client.numDocs("http://it.gmw.cn"))
//    val meta = CascalUtil.getCols(Servers.htablePool, url, "crawl" \ "metadata", ColumnPredicate(List("rawTitle", "keywords")))
    val meta = new HbaseTable(Servers.htablePool, "crawl", "metadata").getRow(url, List("rawTitle", "keywords"))
    val rawTitle = meta.getOrElse("rawTitle", "")
    val keywords = meta.getOrElse("keywords", "")
    val recommend = new SearchRecommend
    
    val docs = recommend.recommend(url, None, rawTitle, siteprefix, count, keywords)
    for (doc <- docs) println(doc)
    println("after dedup by url & title: ") 
    val docs1 = SearchRecommend.dedupByUrl(docs, url){d => d.url}
    SearchRecommend.dedupByTitle(docs1, rawTitle){d => d.title} foreach println
  }
}