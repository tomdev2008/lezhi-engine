package com.buzzinate.lezhi.test

import com.buzzinate.lezhi.Servers
import com.buzzinate.lezhi.redis.RedisCache
import com.buzzinate.lezhi.render.{RenderDoc, Render}
import com.buzzinate.lezhi.thrift.RecommendType

object TestRedisCache {
  def main(args: Array[String]): Unit = {
    val render = new Render(new RedisCache(Servers.jedisPool), Servers.htablePool, "crawl")
    val docs = List(RenderDoc("http://qa.buzzinate.com/news/technology/groupon%e8%91%a3%e4%ba%8b%e4%bc%9a%e5%86%b3%e5%ae%9a%e6%a2%85%e6%a3%ae%e7%bb%a7%e7%bb%ad%e5%87%ba%e4%bb%bbceo-%e8%82%a1%e7%a5%a8%e4%bb%b7%e6%a0%bc%e5%bd%93%e5%a4%a9%e4%b8%8b%e9%99%8d4.html", "", null, None, 2, 2))
    val res = render.matchInsitePics("test", docs)
    println(res)
  }
}