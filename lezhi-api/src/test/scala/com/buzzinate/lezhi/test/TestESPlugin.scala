package com.buzzinate.lezhi.test

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.Term
import org.apache.lucene.queries.BooleanFilter
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.search.CachingWrapperFilter
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.PrefixFilter
import org.apache.lucene.store.SimpleFSDirectory
import org.buzzinate.lezhi.cache.ElasticDocFreqCache
import org.buzzinate.lezhi.filter.FuzzyDuplicateFilter
import org.buzzinate.lezhi.query.LezhiQuery
import org.buzzinate.lezhi.query.TermInfo
import org.buzzinate.lezhi.query.TermQueryDetail
import org.elasticsearch.common.cache.Cache
import org.elasticsearch.common.cache.CacheBuilder
import java.io.File
import java.util.concurrent.TimeUnit
import org.buzzinate.lezhi.util.DomainNames
import org.buzzinate.lezhi.util.SignatureUtil
import java.net.URL
import org.apache.commons.io.IOUtils
import com.alibaba.fastjson.JSON
import org.buzzinate.lezhi.api.Doc
import org.apache.commons.lang.StringUtils
import com.buzzinate.model.DelimitedKeywordText
import com.buzzinate.nlp.util.DictUtil

object TestESPlugin {
  def main(args: Array[String]): Unit = {
    val reader = DirectoryReader.open(new SimpleFSDirectory(new File("D:/data/cqnews")))
    val searcher = new IndexSearcher(reader)
		
    val cache: Cache[String, java.lang.Integer] = CacheBuilder.newBuilder()
			.maximumSize(10000 * 100)
			.expireAfterWrite(10, TimeUnit.MINUTES).expireAfterAccess(1, TimeUnit.MINUTES)
			.build()
		
	val url = "http://news.cqnews.net/html/2010-11/17/content_5332195.htm"
	val siteprefix = "http://news.cqnews.net"
	val docurl = "http://58.83.175.241:9200/content_" + DomainNames.safeGetPLD(url) + "/doc/" + SignatureUtil.signature(url)
	
	val is = new URL(docurl).openStream()
	val json = IOUtils.toString(is)
	is.close
	
	val source = StringUtils.substringBeforeLast(StringUtils.substringAfter(json, "_source\" :"), "}")
	val doc = JSON.parseObject(source, classOf[Doc])
	println(source)
	
	val filters = new BooleanFilter()
    filters.add(new PrefixFilter(new Term("url", siteprefix)), Occur.MUST)
	filters.add(new FuzzyDuplicateFilter("signature"), Occur.MUST)
	val filter = new CachingWrapperFilter(filters)
    val keywordidf = DelimitedKeywordText.toIdfText(doc.keyword) { word => DictUtil.splitIdf(word)}
   	val tis = TermInfo.parse(keywordidf)
    val query = new LezhiQuery(new ElasticDocFreqCache(cache, siteprefix), "keyword", TermQueryDetail.DEFAULT_FIELD_BOOST, tis, doc.signature, doc.lastModified, 5)
    searcher.search(query, filter, 20)

    val result = searcher.search(query, filter, 20)
    println("total: " + result.totalHits)
    for (doc <- result.scoreDocs) {
      val d = searcher.doc(doc.doc)
      println(doc.score + " => " + d.get("title") + " / " + d.get("url") + " => " + d.get("signature"))
//      println(searcher.explain(query, doc.doc))
    }

   	reader.close()
  }
}