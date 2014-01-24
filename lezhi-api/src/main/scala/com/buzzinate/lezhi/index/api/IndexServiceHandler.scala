package com.buzzinate.lezhi.index.api
import java.util.concurrent.Executors
import java.util.{List => JList}
import collection.JavaConverters._
import com.buzzinate.lezhi.thrift._
import com.buzzinate.lezhi.elastic.RichClient
import com.buzzinate.lezhi.util.{Loggable, KeywordUtil, PhraseSplit, Phrase}
import com.buzzinate.nlp.util.DictUtil
import org.apache.commons.lang.StringUtils
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.store.DocStatus
import java.util.ArrayList
import com.buzzinate.lezhi.elastic.RichClient
import com.buzzinate.lezhi.store.DocStatus
import com.twitter.util.FuturePool
import scala.collection.mutable.ListBuffer
import com.buzzinate.lezhi.thrift.ElasticIndexService
import com.buzzinate.lezhi.Servers

class IndexServiceHandler(client: RichClient, docStatus: DocStatus) extends Loggable{
	preload

	/**
	 * sitePrefixes集合可能属于多个domain（因为有可能同一uuid在多个网站上安装lezhi plugin）
	 */
	def searchUrls(sitePrefixes: Seq[String] = Seq[String](), title: String, start: Int, size: Int): SearchResult = {
	    val startTime = System.currentTimeMillis()
	    val words = Phrase.possibleWords(PhraseSplit.splitTitle(title)).asScala
	    val keyword = words.map(w => StringUtils.replace(w, " ", "_") + "|2,1," + DictUtil.splitIdf(w)).mkString(" ")
    
	    val (totalHit, docs, domainList) = client.queryTitle(sitePrefixes.toList, keyword, start, size)
	    info("[query] " + sitePrefixes + " with " + title + " => total: " + totalHit)

        val url2status = docStatus.getStatus(domainList.toIterable, docs.map(d => d.url))
	
	    val metaDataList = ListBuffer[Metadata]()
	    for (doc <- docs) {
	      // TODO: load keywords
	      val status = url2status.getOrElse(doc.url, StatusEnum.Normal)
	      val meta = Metadata(doc.url, Option(doc.thumbnail),Option(doc.title), Option(""), None, Option(status))
	      metaDataList += meta
	    }
	    val endTime = System.currentTimeMillis() - startTime
	    info("[query] " + sitePrefixes + " with " + title + " => cost(ms): " + endTime)
	    SearchResult(totalHit.toInt, metaDataList.toList)
	}
	
	def getByUrls(urls: Seq[String]): Seq[Metadata] = {
		if (urls.isEmpty) List[Metadata]()
		else {
			val docs = client.get(urls.asJava).values.asScala.toList
			val host = DomainNames.safeGetHost(urls(0))
			val url2status = docStatus.getStatus(host, docs.map(d => d.url))

			val list = docs.map { d =>
			  val status = url2status.getOrElse(d.url, StatusEnum.Normal)
			  // TODO: load keywords
			  Metadata(d.url, Option(d.thumbnail),Option(d.title), Option(""), None, Option(status))
			}
			list
		}
	}
	
	def matchAll(sitePrefixes: Seq[String] = Seq[String](), start: Int, size: Int): SearchResult = {
	    val startTime = System.currentTimeMillis()
	    val (totalHit, docs, domainList) = client.matchAll(sitePrefixes.toList, start, size)
	    info("[match all] " + sitePrefixes + " => total: " + totalHit)
	     
		 //从cassandra中获取url对应的status
	    val url2status = docStatus.getStatus(domainList, docs.map(d => d.url))

		val metaDataList = ListBuffer[Metadata]()
		for (doc <- docs) {
			// TODO: load keywords
			val meta = Metadata(doc.url, Option(doc.thumbnail),Option(doc.title), Option(""), None, Option(url2status.getOrElse(doc.url, StatusEnum.Normal)))
			metaDataList += meta
		}
	    val endTime = System.currentTimeMillis() - startTime
	    info("[match all] " + sitePrefixes + " with " + sitePrefixes + " =>cost(ms): " + endTime)
		SearchResult(totalHit.toInt, metaDataList.toList)
	}
	
    def deleteIndexes(urls: Seq[String] = Seq[String]()):  Unit = {
		info("delete from index: " + urls)
    	client.delete(urls.toList)
	}
    
    def updateMetadata(metadata: Metadata):  Unit = {
		if (metadata.title.isDefined || metadata.thumbnail.isDefined) {
			val doc = client.get(metadata.url)
			if (metadata.title.isDefined) doc.title = metadata.title.toString
			if (metadata.thumbnail.isDefined) doc.thumbnail = metadata.thumbnail.toString
			client.bulkAdd(List(doc).asJava)
		}

		if (metadata.status.isDefined) docStatus.put(metadata.url, metadata.status.get)
	}
	
	private def preload(): Unit = {
	    info("preload ...")
	    KeywordUtil.refineKeywords("乐知推荐")
//	    info("Elasticsearch state: " + client.state)
    }
}

object IndexServiceHandler {
	def main(args: Array[String]): Unit = {
		val client = new RichClient("lezhi", List("192.168.1.136")) 
		val pool = Servers.htablePool
		val docStatus = new DocStatus(pool,100)
		val impl = new IndexServiceHandler(client, docStatus)

		val metadata = Metadata("http://www.taihainet.com/news/fujian/yghx/2013-04-04/1047358_2.html",None,None,None,None,Option(StatusEnum.Prior))
//		impl.updateMetadata(meta)
		docStatus.put(metadata.url, metadata.status.get)
		println(docStatus.getStatus(metadata.url));

		println("searchUrls: " + impl.searchUrls(List("http://www.taihainet.com/news","http://news.subaonet.com"), " 蹭车蹭食宿欲走遍全国 苏州 新闻网", 0, 20))
//		println("getByUrls: " + impl.getByUrls(List("http://www.taihainet.com/news/fujian/yghx/2013-04-04/1047358_2.html")))
//        println("matchAll: " + impl.matchAll(List("http://www.taihainet.com/news","http://news.subaonet.com"), 0, 20))
	}
}
