package com.buzzinate.lezhi.elastic

import java.lang.{Long => JLong}
import org.buzzinate.lezhi.api.{Query, Client}
import collection.JavaConverters._
import org.buzzinate.lezhi.plugin.LezhiQueryParser
import org.elasticsearch.action.search.SearchType
import com.alibaba.fastjson.JSON
import org.apache.commons.lang.StringUtils
import org.elasticsearch.index.query._
import com.buzzinate.lezhi.util.{SignatureUtil, DomainNames}
import org.elasticsearch.action.get.MultiGetRequest
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.indices.IndexMissingException
import com.buzzinate.lezhi.util.Loggable
import scala.collection.mutable.ListBuffer
import org.elasticsearch.action.support.IgnoreIndices
import com.buzzinate.lezhi.util.PhraseSplit

case class PhraseDoc(url: String, title: String, keywords: String, timestamp: Long) {
  def id(): String = JLong.toHexString(SignatureUtil.signatureTitle(title))

  def toJson(): String = {
    val snippet = StringUtils.join(PhraseSplit.splitTitle(title), " | ")
    val keyword = StringUtils.join(PhraseSplit.splitKeyword(keywords), " ")
    JSON.toJSONString(Map("id"->id, "url"->url, "title"->title, "snippet"->snippet, "keyword"->keyword, "lastModified"->timestamp).asJava, false)
  }
}

class RichClient(clusterName: String, hosts: List[String]) extends Client(clusterName, hosts.asJava) with Loggable{
  def matchAll(sitePrefixes: List[String], start: Int, size: Int): (Long, List[RecDoc], List[String]) = {
    if(sitePrefixes.size > 0){
	    val query = buildQuery(sitePrefixes)
	    val domainList = sitePrefixes.map(siteprefix => DomainNames.safeGetPLD(siteprefix))
	    val indices = domainList.map(domain => "content_" + domain) 
	    try{
	      val resp = client.prepareSearch(indices.seq: _*)
	    		  .setIgnoreIndices(IgnoreIndices.MISSING)
	    		  .setTypes("doc")
	    		  .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)//改DFS_QUERY_AND_FETCH为DFS_QUERY_THEN_FETCH
	              .setQuery(query)
	              .addSort("lastModified", SortOrder.DESC)
	              .addFields("url", "title", "thumbnail", "lastModified")
	              .setFrom(start).setSize(size)
	              .execute.actionGet
	      val docs = resp.getHits().asScala.toList.map { hit =>
		      val url = hit.field("url").getValue[String]
		      val title = hit.field("title").getValue[String]
		      val thumbnail = hit.field("thumbnail").getValue[String]
		      RecDoc(url,title, thumbnail, 0L, 1, hit.score)
	      }
	     (resp.getHits().totalHits, docs, domainList)
	    }catch{
	      case ex :IndexMissingException =>  info("[matchAll] => Elasticsearch's index: " + ex.getMessage() )
	      (0, List(), domainList)
	    }
    }else (0, List(), List())
  }

  private def buildQuery(siteprefixes: List[String]): QueryBuilder = {
    if (siteprefixes.size == 0) new MatchAllQueryBuilder
    else {
      val query = new BoolQueryBuilder
      for (siteprefix <- siteprefixes) {
        query.should(new PrefixQueryBuilder("url", siteprefix))
      }
      query
    }
  }

  private def buildFilter(siteprefixes: List[String]): FilterBuilder = {
    if (siteprefixes.size == 0) new MatchAllFilterBuilder
    else {
      val filter = new BoolFilterBuilder
      for (siteprefix <- siteprefixes) {
        filter.should(new PrefixFilterBuilder("url", siteprefix))
      }
      filter
    }
  }
  
  /**
   * 
   */
  def queryTitle(sitePrefixes: List[String], keyword: String, start: Int, size: Int): (Long, List[RecDoc], List[String]) = {
    if(sitePrefixes.size > 0){
      //根据sitePrefixes获取domain
     val domainList = sitePrefixes.map(siteprefix => DomainNames.safeGetPLD(siteprefix))
     val indices = domainList.map(domain => "content_" + domain) 
    
	 val filter = buildFilter(sitePrefixes)
	 val q = new Query(null, keyword, System.currentTimeMillis)
	 val querymap = new java.util.HashMap[String, Query]
	 querymap.put(LezhiQueryParser.NAME, q)
	
	 try{
	      val resp = client.prepareSearch(indices.seq: _*)
	         .setIgnoreIndices(IgnoreIndices.MISSING)
	         .setTypes("doc")
	         .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)//改DFS_QUERY_AND_FETCH为DFS_QUERY_THEN_FETCH
	         .setFilter(filter)//增加filter,基于sitePrefixes进行过滤
	         .setQuery(JSON.toJSONString(querymap, false))
	         .addFields("url", "title", "thumbnail")
	         .setFrom(start).setSize(size)
	         .execute.actionGet
	      val docs = resp.getHits.asScala.toList.map { hit =>
		      val url = hit.field("url").getValue[String]
		      val title = hit.field("title").getValue[String]
		      val thumbnail = hit.field("thumbnail").getValue[String]
		      RecDoc(url,title, thumbnail, 0L, 1, hit.score)
	      }
	      (resp.getHits.totalHits, docs, domainList)
	 }catch{
	      case ex :IndexMissingException =>  info("[queryTitle] => Elasticsearch's index: " + ex.getMessage() )
	       (0, List(), domainList)
	 }
    }else (0, List(), List())
  }

  def existsPhrases(siteids: Set[(String, String)]): Set[(String, String)] = {
    if (siteids.size > 0) {
      val mget = client.prepareMultiGet
      for ((site, id) <- siteids) {
        mget.add(new MultiGetRequest.Item("phrase_" + site, "phrase", id).fields("id"))
      }

      mget.execute().actionGet().asScala.flatMap { resp =>
        if (resp.getResponse.isExists) {
          val site = resp.getIndex.substring("phrase_".length)
          val id = resp.getResponse().getField("id").getValue.asInstanceOf[String]
          Some((site, id))
        } else None
      }.toSet
    } else Set.empty[(String, String)]

  }

  def delete(urls: List[String]): Unit = {
    val bulkReq = client.prepareBulk
    urls foreach { url =>
      val site = DomainNames.safeGetPLD(url)
      bulkReq.add(client.prepareDelete("content_" + site, "doc", SignatureUtil.signature(url)))
    }
    bulkReq.execute().actionGet
  }
}

object RichClient {
  def main(args: Array[String]): Unit = {
    val client = new RichClient("lezhi", List("192.168.1.136"))
    println(client.matchAll(List("http://www.taihainet.com/news","http://www.admin5.com/article"), 1, 40))
    client.close
  }
}
