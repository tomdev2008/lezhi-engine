package com.buzzinate.lezhi.index.api

import java.util.ArrayList
import com.buzzinate.lezhi.elastic.RichClient
import com.buzzinate.lezhi.store.DocStatus
import com.twitter.util.FuturePool
import scala.collection.mutable.ListBuffer
import com.buzzinate.lezhi.thrift.ElasticIndexService
import com.twitter.util.Future
import com.buzzinate.lezhi.thrift.SearchResult
import com.buzzinate.lezhi.thrift.Metadata
import java.util.concurrent.Executors


class IndexServiceImpl(client: RichClient, docStatus: DocStatus) extends ElasticIndexService.FutureIface  {
	val futurePool: FuturePool = FuturePool(Executors.newFixedThreadPool(128))
	val indexService = new IndexServiceHandler(client, docStatus)
  

	/**
	 * 根据title模糊匹配包含该title的页面，并分页返回页面对应的url结果集
	 */
	override def searchUrls(sitePrefixes: Seq[String] = Seq[String](), title: String, start: Int, size: Int): Future[SearchResult] = {
     	futurePool.apply(indexService.searchUrls(sitePrefixes, title, start, size))
	}
  
	/**
	 * 查找对应urls的内容管理索引
	 */
	override def getByUrls(urls: Seq[String] = Seq[String]()): Future[Seq[Metadata]] = {
     	futurePool.apply(indexService.getByUrls(urls))
	}
  
   	/**
   	 * 根据网站的domain或者网站下的sitePrefix列表获取内容管理索引
   	 */
	override def matchAll(sitePrefixes: Seq[String] = Seq[String](), start: Int, size: Int): Future[SearchResult] = {
    	futurePool.apply(indexService.matchAll(sitePrefixes, start, size))
	}
  

	/**
	 * 根据urls批量删除elastic search上的索引
	 */
	override def deleteIndexes(urls: Seq[String] = Seq[String]()):  Future[Unit] = {
		futurePool.apply(indexService.deleteIndexes(urls))
	}
  
	/**
	 * 修改Metadate信息
	 */
	override def updateMetadata(metadata: Metadata):  Future[Unit] = {
		futurePool.apply(indexService.updateMetadata(metadata))
	}
}