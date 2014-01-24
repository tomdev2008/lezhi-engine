package com.buzzinate.lezhi.crawl

import com.buzzinate.crawl.Item
import com.buzzinate.crawl.FrontierClient
import com.buzzinate.lezhi.util.ItemConverter

object TestCrawlSetup {
  
  def main(args: Array[String]): Unit = {
    val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")  
    
    val frontierClient = new FrontierClient(prop.getProperty("frontier.host", "localhost"))  
      
    
    val items = if(args.length == 0) List(
        Item("http://t.docin.com/p-618824133.html", "siteprefix" -> "http://t.docin.com/"),
//        Item("http://t.docin.com/t-279219.html", "siteprefix" -> "http://t.docin.com/"),
////        Item("http://t.docin.com/d-310673.html", "siteprefix" -> "http://t.docin.com/")
//        Item("http://world.huanqiu.com/regions/2013-03/3747405.html","siteprefix" -> "http://world.huanqiu.com" ),
//        Item("http://article.woshao.com/796cfd2a642d11e1a75c000c2959fd2a", "siteprefix" -> "http://article.woshao.com"),
//        Item("http://lz.bshare.cn/install", "siteprefix" -> "http://lz.bshare.cn/"),
//        Item("http://test.buzzinate.com/wordpress/?p=102", Map("siteprefix" -> "http://test.buzzinate.com")),
//        Item("http://test.buzzinate.com/wordpress/?p=102", Map("siteprefix" -> "http://test.buzzinate.com")),
//        Item("http://www.chinanews.com/gn/2012/06-19/3974887.shtml", "siteprefix" -> "http://www.chinanews.com/"),
//        Item("http://news.uuu9.com/2012/201206/256735.shtml", "siteprefix" -> "http://news.uuu9.com/"),
//        Item("http://luo.bo/25499/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/4868/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/28887/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/25736/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/2180/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/26636/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/26263/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/28630/", "siteprefix" -> "http://luo.bo/"),
//        Item("http://luo.bo/25736/", "siteprefix" -> "http://luo.bo/"),
        Item("http://luo.bo/6906/", "siteprefix" -> "http://luo.bo/"),
        Item("http://www.36kr.com/p/121670.html", "siteprefix" -> "http://www.36kr.com/"),
//        Item("http://www.36kr.com/p/121670.html?ref=KrTop10", "siteprefix" -> "http://www.36kr.com/"),
//        Item("http://www.36kr.com/p/119160.html", "siteprefix" -> "http://www.36kr.com/"),
////        Item("http://www.36kr.com/p/109843.html", "siteprefix" -> "http://www.36kr.com/")
//        Item("http://qa.buzzinate.com/ceshi/blog/?post=2","siteprefix" -> "http://qa.buzzinate.com"),
//        Item("http://qa.buzzinate.com/news/entertainment/%E4%B8%80%E4%B8%AA%E5%A5%B3%E4%BA%BA%E5%8F%AF%E4%B8%8D%E5%8F%AF%E4%BB%A5%E7%88%B1%E4%B8%A4%E4%B8%AA%E7%94%B7%E4%BA%BA%EF%BC%9F.html","siteprefix" -> "http://qa.buzzinate.com"),
        Item("http://blog.bshare.cn/2012/06/smozhanzhanglm/", "siteprefix" -> "http://blog.bshare.cn/"),
//        Item("http://happy.gmw.cn/content_6128927.htm", "siteprefix" -> "http://happy.gmw.cn/content"),
        Item("http://blog.zzsmo.com/2012/08/2012hnzzdahui/", Map("siteprefix" -> "http://blog.zzsmo.com", "urlprefix" -> "http://blog.zzsmo.com/"))
//        Item("http://http://test.buzzinate.com/wordpress/?p=3038", Map("siteprefix" -> "http://test.buzzinate.com"))
    ) else List(Item(args(1),Map("siteprefix" -> args(0))))
    
    frontierClient.client.offer(ItemConverter.asThrift(items)).apply
    println("end")
    frontierClient.service.close().apply
    
  }
}