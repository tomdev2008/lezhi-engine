package com.buzzinate.lezhi.elastic
import org.buzzinate.lezhi.api.Client
import java.util.{List => JList}
import collection.JavaConverters._
import org.buzzinate.lezhi.api.Doc
import org.elasticsearch.indices.IndexMissingException
import com.buzzinate.lezhi.util.Loggable

/**
 * 用于捕获ElasticSearch方法执行中抛出的异常
 */
trait ClientWrapper extends Client with Loggable{
   /**
    * 根据urls获取Doc
    */
	override def get(urls: JList[String]): java.util.Map[String, Doc]={
	    try{
	      val docs = super.get(urls)
	      docs
	    }catch{
	      case ex :IndexMissingException => 
	         info("[get(urls)] => Elasticsearch's index: " + ex.getMessage() )
	         new java.util.HashMap[String, Doc]()                            
	    }
    }
	
	/**
	 * 根据单个url获取Doc
	 */
	override def get(url: String) :Doc={
	   try{
	      val doc = super.get(url)
	      doc
	    }catch{
	      case ex :IndexMissingException => 
	         info("[get(url)] => Elasticsearch's index: " + ex.getMessage() )
	         null                         
	    }
	}
	
	/**
	 * 添加Docs
	 */
	override def bulkAdd(docs : JList[Doc]):Unit={
	   try{
	      super.bulkAdd(docs)
	    }catch{
	      case ex :IndexMissingException => 
	         info("[bulkAdd(docs)] => Elasticsearch's index: " + ex.getMessage() )
	    }
	}
}