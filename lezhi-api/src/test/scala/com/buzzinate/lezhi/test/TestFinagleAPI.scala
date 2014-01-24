package com.buzzinate.lezhi.test
import java.net.InetSocketAddress
import scala.collection.mutable.ListBuffer
import org.apache.thrift.protocol.TBinaryProtocol
import com.buzzinate.lezhi.thrift.ElasticIndexService
import com.buzzinate.lezhi.thrift.PicType
import com.buzzinate.lezhi.thrift.RecommendParam
import com.buzzinate.lezhi.thrift.RecommendServices
import com.buzzinate.lezhi.thrift.RecommendType
import com.buzzinate.lezhi.thrift.RecommendTypeParam
import com.buzzinate.lezhi.util.Config
import com.buzzinate.lezhi.util.Constants
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.Servers
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.Service
import javax.annotation.Generated
import com.buzzinate.lezhi.store.HbaseTable

object TestFinagleAPI {
	def main(args: Array[String]) :Unit ={
//	    TestFinagleAPI.testRecommendApi
	    TestFinagleAPI.testElasticIndexApi
	}

	def testRecommendApi():Unit={
      val port = 32123
      
	   val service: Service[ThriftClientRequest, Array[Byte]] = ClientBuilder()
	    .hosts(new InetSocketAddress(port))
	    .codec(ThriftClientFramedCodec())
	    .hostConnectionLimit(20)
	    .build()
	
	  val client = new RecommendServices.FinagledClient(service, new TBinaryProtocol.Factory(),"recommendService")
	  val url = "http://qa.buzzinate.com/news/entertainment/%e4%b8%80%e4%b8%aa%e5%a5%b3%e4%ba%ba%e5%8f%af%e4%b8%8d%e5%8f%af%e4%bb%a5%e7%88%b1%e4%b8%a4%e4%b8%aa%e7%94%b7%e4%ba%ba%ef%bc%9f.html"
	  val siteprefix = "http://" + DomainNames.safeGetHost(url)
      val rawTitle = new HbaseTable(Servers.htablePool, "crawl", "metadata").getRow(url, List("rawTitle")).values.toList
	  
      val typeParams = ListBuffer[RecommendTypeParam]()
      val typeParam1 = RecommendTypeParam(1, RecommendType.Insite, PicType.Insite, 2)
      typeParams.append(typeParam1)

      val typeParam2 = RecommendTypeParam(1, RecommendType.Insite, PicType.Insite, 3)
      typeParams.append(typeParam2)

      val params = RecommendParam(url,typeParams.toList,Option(rawTitle(0)),Option(siteprefix),"1FFyKjHTD1qL0dhAhP4g", None, None, None,None )
		client.recommend(params) onSuccess( recResults =>
	    recResults.results foreach { itemlist =>
//	      println(itemlist.typeParam.recommendType)
	      itemlist.items foreach { doc =>
	        println(doc)
	      }
       }
	  ) onFailure( e =>
	    e.printStackTrace()
	  ) ensure{
		     service.close()
	   }
	
//	 client.correctImg("1", "2", Option("3"))  ensure{
//	     service.close()
//	   }
	}
	
	def testElasticIndexApi():Unit ={
//	   val config = Config.getConfig("test.properties")
       val port = 32124
      
	   val service: Service[ThriftClientRequest, Array[Byte]] = ClientBuilder()
	    .hosts(new InetSocketAddress(port))
	    .codec(ThriftClientFramedCodec())
	    .hostConnectionLimit(20)
	    .build()
	
	    val client = new ElasticIndexService.FinagledClient(service, new TBinaryProtocol.Factory(),"indexService")
	    val result = client.searchUrls(List("http://www.taihainet.com/news","http://news.subaonet.com"), " 蹭车蹭食宿欲走遍全国 苏州 新闻网", 0, 20).get()
	    val result2 = client.getByUrls(List("http://www.taihainet.com/news/fujian/yghx/2013-04-04/1047358_2.html")).get()
	    val result3 = client.matchAll(List("http://www.taihainet.com/news","http://news.test.com"), 0, 20).get()
	    println(result)
	    println(result2)
	    println(result3)
	    service.close()
	}
}

