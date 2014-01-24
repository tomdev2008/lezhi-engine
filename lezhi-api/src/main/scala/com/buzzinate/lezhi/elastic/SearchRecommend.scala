package com.buzzinate.lezhi.elastic

import scala.collection.JavaConverters._
import scala.collection.mutable.HashSet
import scala.collection.mutable.ListBuffer
import org.apache.commons.lang.StringUtils
import org.buzzinate.lezhi.util.LargestTitle
import org.buzzinate.lezhi.util.SignatureUtil
import org.elasticsearch.indices.IndexMissingException
import com.buzzinate.crawl.Item
import com.buzzinate.json.RecItem
import com.buzzinate.lezhi.async.Async
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.util.KeywordUtil
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.lezhi.Servers
import com.buzzinate.model.DelimitedKeywordText
import com.buzzinate.model.KeywordInfo
import com.buzzinate.nlp.util.DictUtil
import com.buzzinate.nlp.util.TextUtil
import com.buzzinate.lezhi.util.TitleExtractor
import com.buzzinate.lezhi.util.UrlPagination
import org.buzzinate.lezhi.api.HitDoc

case class RecDoc(url: String, title: String, thumbnail: String, lastModified: Long, click: Long, score: Float)

object RecDoc {
  def asJava(docs: List[RecDoc]): List[RecItem] = {
    docs map {d => new RecItem(d.url, d.title, d.thumbnail, d.lastModified, d.click, d.score) }
  }
  
  def asScala(docs: List[RecItem]): List[RecDoc] = {
    docs map {d => RecDoc(d.url, d.title, d.thumbnail, d.lastModified, d.click, d.score)}
  }
}

class SearchRecommend extends Loggable {
  val async = new Async(Servers.asyncPool)
   
  def recommend(url: String, canonicalUrl: Option[String], title: String, siteprefix: String, count: Int, metaKeywords: String): List[RecDoc] = {
    val start = System.currentTimeMillis
    val signature = SignatureUtil.signature(LargestTitle.parseLargest(SearchRecommend.te.extract(title)))
    try {
      val urls = List(url) ++ canonicalUrl ++ SearchRecommend.tryRemoveParam(url)
      val url2doc = Servers.client.get(urls.distinct.asJava).asScala
      val hitdocs = url2doc.find { case (_, doc) => doc.title == title || doc.url == url }.map { case (realurl, doc) =>
        if (url != realurl) info("Please double check: " + url + " => " + realurl)
        val idfkeyword = DelimitedKeywordText.toIdfText(doc.keyword) { word => DictUtil.splitIdf(word)}
        request(url, siteprefix, doc.signature, idfkeyword, doc.lastModified, count)
        //Servers.client.query(siteprefix, doc.signature, idfkeyword, doc.lastModified, SearchRecommend.ensureRange(count * 2, 20, 50))
      }.getOrElse {
        info(url + " does not exists, inform crawl")
        Servers.sendQueue(Item(url, "siteprefix" -> siteprefix))
        
        val titlekeywords = DelimitedKeywordText.toIdfText(SearchRecommend.tryTitleKeywords(title, metaKeywords)) { word => DictUtil.splitIdf(word) }
        request(url, siteprefix, signature, titlekeywords, System.currentTimeMillis, count)
//        Servers.client.query(siteprefix, signature, titlekeywords, System.currentTimeMillis, SearchRecommend.ensureRange(count * 2, 20, 50))
      }
    
      info(url + " recommend cost(ms): " + (System.currentTimeMillis - start))
    
      val rawdocs = SearchRecommend.dedupBy(hitdocs, signature){doc => doc.signature}
      val urldocs = SearchRecommend.dedupByUrl(rawdocs, url) { doc => doc.url }
      val docs = SearchRecommend.dedupByTitle(urldocs, title) { doc => doc.title }
      
      docs map { doc =>
        RecDoc(doc.url, doc.title, doc.thumbnail, doc.lastModified, 0L, doc.score)
      }      
    } catch {
      case e: IndexMissingException => {
        info("new host " + DomainNames.safeGetHost(url) + ", " + url + " does not exists, inform crawl")
        Servers.sendQueue(Item(url, "siteprefix" -> siteprefix))
        Nil
      }
    }
  }

  // TODO: refine the logic
  private def request(url: String, siteprefix: String, signature: String, idfkeyword: String, lastModified: Long, count: Int): List[HitDoc] = {
    val docs = if (url.startsWith("http://t.docin.com/p-")) {
      val result = new java.util.ArrayList[HitDoc]
      result.addAll(Servers.client.query("http://t.docin.com/d-", signature, idfkeyword, lastModified, 2))
      result.addAll(Servers.client.query("http://t.docin.com/t-", signature, idfkeyword, lastModified, 2))
      result.addAll(Servers.client.query("http://t.docin.com/p-", signature, idfkeyword, lastModified, SearchRecommend.ensureRange(count * 2 - 4, 20, 50)))
      result.asScala.toList
    } else if (url.startsWith("http://www.docin.com/p-")) {
      val result = new java.util.ArrayList[HitDoc]
      result.addAll(Servers.client.query("http://www.docin.com/d-", signature, idfkeyword, lastModified, 2))
      result.addAll(Servers.client.query("http://www.docin.com/t-", signature, idfkeyword, lastModified, 2))
      result.addAll(Servers.client.query("http://www.docin.com/p-", signature, idfkeyword, lastModified, SearchRecommend.ensureRange(count * 2 - 4, 20, 50)))
      result.asScala.toList
    } else Servers.client.query(siteprefix, signature, idfkeyword, lastModified, SearchRecommend.ensureRange(count * 2, 20, 50)).asScala.toList
    
    if (siteprefix.startsWith("http://t.docin.com")) {
      docs map { d =>
        if (d.url.startsWith("http://t.docin.com/p-")) {
          d.url = StringUtils.replace(d.url, ".html", "-854.html")
        }
        d
      }
    } else if (siteprefix.startsWith("http://www.docin.com")) {
      docs map { d =>
        if (d.url.startsWith("http://www.docin.com/p-")) {
          d.url = StringUtils.replace(d.url, ".html", "-854.html")
        }
        d
      }
    } else docs
  }
  
  def checkCrawl(url: String, canonicalUrl: Option[String], title: String, siteprefix: String): Unit = {
    async.asnyc(url) {
      try {
        val urls = List(url) ++ canonicalUrl ++ SearchRecommend.tryRemoveParam(url)
        val url2doc = Servers.client.get(urls.distinct.asJava).asScala
      
        url2doc.find { case (_, doc) => doc.title == title || doc.url == url } match {
          case Some(doc) => {}
          case None => {
            info(url + " does not exists, inform crawl")
            Servers.sendQueue(Item(url, "siteprefix" -> siteprefix))
          }
        }
      } catch {
        case e: IndexMissingException => {
          info("new host " + DomainNames.safeGetHost(url) + ", " + url + " does not exists, inform crawl")
          Servers.sendQueue(Item(url, "siteprefix" -> siteprefix))
        }
      }
    }
  }
}

object SearchRecommend {  
  val te = new com.buzzinate.nlp.util.TitleExtractor
  
  def dedupBy[T, B](list: List[T], thisobj: B)(mf: T => B): List[T] = {
    val values = new HashSet[B]
    values += thisobj
    val result = new ListBuffer[T]
    list foreach { x =>
      val v = mf(x)
      if (!values.contains(v)) {
        result += x
        values += v
      }
    }
    result.result
  }
  
  def dedupByUrl[T](list: List[T], thisurl: String)(mf: T => String): List[T] = {
    val pagesize = list.filter(x => UrlPagination.hasPage(mf(x))).size
    if (pagesize >= math.max(3, list.size / 5)) {
      val values = new HashSet[String]
      values += UrlPagination.normal(thisurl)
      val result = new ListBuffer[T]
      list foreach { x =>
        val v = UrlPagination.normal(mf(x))
        if (!values.contains(v)) {
          result += x
          values += v
        }
      }
      result.result
    } else list
  }
  
  def dedupByTitle[T](list: List[T], thistitle: String)(mf: T => String): List[T] = {
    val te = TitleExtractor.prepare(thistitle::list.map(x => mf(x)))
    val values = new HashSet[String]
    values += te.extract(thistitle)
    val result = new ListBuffer[T]
    list foreach { x =>
      val v = te.extract(mf(x))
      if (!values.contains(v)) {
        result += x
        values += v
      }
    }
    result.result
  }
  
  private def ensureRange(x: Int, min: Int, max: Int) = if (x < min) min else if (x > max) max else x
  
  private def tryTitleKeywords(rawTitle: String, metaKeywords: String): List[KeywordInfo] = {
    val filledTitle = TextUtil.fillText(te.extract(rawTitle))
    val keywords = splitMetaKeywords(metaKeywords.split("[,，]").toList).map { mk => 
      TextUtil.stemAll(StringUtils.trim(mk)).toLowerCase
    }.map { w =>
      val field = if (filledTitle.contains(TextUtil.fillWord(w))) KeywordInfo.META_TITLE else KeywordInfo.META
      KeywordInfo(w, 1, field)
    }
    KeywordUtil.extractKeywords(rawTitle, keywords)    
  }
  
  private def splitMetaKeywords(metaKeywords: List[String]): List[String] = {
    val words = new ListBuffer[String]
    for (metaKeyword <- metaKeywords) {
      val sb = new StringBuffer
      for (ch <- metaKeyword) {
        if (Character.isLetterOrDigit(ch)) sb.append(ch)
        else {
          val word = sb.toString.trim
          if (word.size > 1) words += word
          sb.setLength(0)
        }
      }
      val word = sb.toString.trim
      if (word.size > 1) words += word
    }
    words.result
  }
  
  private def tryRemoveParam(url: String): Option[String] = {
    val lastSlashIdx = url.lastIndexOf('/')
    val markidx = url.indexOf('?', lastSlashIdx)
    if (markidx != -1 && markidx > lastSlashIdx) {
      val dotidx = url.lastIndexOf('.', markidx)
      if (dotidx != -1 && dotidx > lastSlashIdx && dotidx+6 < markidx) return Some(url.substring(0, markidx))
    }
    return None
  }
}