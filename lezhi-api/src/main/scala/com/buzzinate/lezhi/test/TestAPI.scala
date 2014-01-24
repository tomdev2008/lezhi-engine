package com.buzzinate.lezhi.test

import scala.collection.mutable.ListBuffer
import com.buzzinate.lezhi.api.RecommendServiceImpl
import com.buzzinate.lezhi.thrift.PicType
import com.buzzinate.lezhi.thrift.RecommendParam
import com.buzzinate.lezhi.thrift.RecommendType
import com.buzzinate.lezhi.thrift.RecommendTypeParam
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.Servers
import com.buzzinate.lezhi.store.HbaseTable

object TestAPI {
  def main(args: Array[String]): Unit = {
    val service = new RecommendServiceImpl
    if (args.length < 2) {
      println("Usage: TestAPI <url> <count>")
      return
    }
    val url = args(0)
    val count = args(1).toInt
    val siteprefix = "http://" + DomainNames.safeGetHost(url)
    
    val rawTitle = new HbaseTable(Servers.htablePool, "crawl", "metadata").getRow(url, List("rawTitle")).values.toList
    val typeParams = ListBuffer[RecommendTypeParam]()
    val typeParam1 = RecommendTypeParam(1, RecommendType.Insite, PicType.Insite, 2)
    typeParams.append(typeParam1)

    val typeParam2 = RecommendTypeParam(1, RecommendType.Insite, PicType.Insite, 3)
    typeParams.append(typeParam2)

    val params = RecommendParam(url,typeParams.toList,Option(rawTitle(0)),Option(siteprefix),"1FFyKjHTD1qL0dhAhP4g", None, None, None,None )
   
    println(url + " => " + rawTitle)
    val r = service.recommend(params)
    r.get().results foreach { itemlist =>
      println(itemlist.typeParam.recommendType)
      itemlist.items foreach { doc =>
        println(doc)
      }
    }
    Servers.htablePool.close
    Servers.client.close
  }
}