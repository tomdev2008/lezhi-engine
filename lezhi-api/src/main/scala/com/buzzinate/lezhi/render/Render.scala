package com.buzzinate.lezhi.render

import collection.JavaConverters._
import com.buzzinate.lezhi.thrift.{RecommendTypeParam, RecommendType, PicType}
import org.apache.commons.lang.StringUtils
import com.buzzinate.lezhi.util._
import java.text.SimpleDateFormat
import com.buzzinate.lezhi.elastic.RecDoc
import scala.collection.mutable.{HashMap, HashSet}
import com.buzzinate.json.RenderThumbnail
import com.buzzinate.lezhi.Servers
import org.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.redis.RedisCache
import com.buzzinate.model.DelimitedKeywordText
import com.buzzinate.nlp.util.DictUtil
import org.apache.hadoop.hbase.client.HTablePool
import com.buzzinate.lezhi.store.HbaseTable

class Render(cache: RedisCache, htableool: HTablePool, crawlKeyspace: String) extends Loggable {
  val base = new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-01").getTime
  val hbaseStore = new HbaseTable(htableool, "crawl", "metadata")
    
  def rende(url: String, title: String, siteprefix: String, recTypes: List[RecommendTypeParam], topDocs: Map[RecommendType, List[RecDoc]]): List[(RecommendTypeParam,List[RenderDoc])] = {
    val titledocs = rendeTitle(topDocs, title) map { case (recType, docs) =>
      recType -> docs.filterNot(d => d.url == url)
    }

    val type2idx = new HashMap[RecommendType, Int] with HashMapUtil.IntHashMap[RecommendType]
    recTypes.map { recType =>
      val idx = type2idx.getOrElse(recType.recommendType, 0)
      val alldocs = titledocs.getOrElse(recType.recommendType, Nil)
      val docs = alldocs.slice(idx, idx + recType.count)
      type2idx.adjustOrPut(recType.recommendType, docs.size, idx + docs.size)
      (recType, matchPics(url, docs, siteprefix, recType.matchPic))
    }
  }

  def fillThumbnail(docs: List[RecDoc]): List[RecDoc] = {
    val url2thumb = getThumbs(docs.map(d => d.url))
    docs map { doc =>
      val thumb = url2thumb.getOrElse(doc.url, doc.thumbnail)
      RecDoc(doc.url, doc.title, thumb, doc.lastModified, doc.click, doc.score)
    }
  }
    
  def rendeTitle(topDocs: Map[RecommendType, List[RecDoc]], rawTitle: String): Map[RecommendType, List[RenderDoc]] = {
    val rawtitles = topDocs.values.flatten.map(d => cleanHash(d.title)).toList
    val te = TitleExtractor.prepare(cleanHash(rawTitle) :: rawtitles.toList)
   
    topDocs map { case (recType, docs) => 
      val renderDocs = docs map { doc =>
	    val title = UrlPagination.refineTitle(doc.url, te.extract(cleanHash(doc.title)))
	    val hotScore = math.log(1 + doc.click) + (doc.lastModified - base) / 45000000.0
	    val picType = if (StringUtils.isBlank(doc.thumbnail)) None else Some(PicType.Inpage) 
	    RenderDoc(doc.url, title, doc.thumbnail, picType, doc.score.toDouble, hotScore)
      }
      recType -> renderDocs
    }
  }

  @inline
  private def cleanHash(title: String): String = {
    // remove #blz_insite or #nextPage
    val idx = title.indexOf("#")
    if (idx >= 0) {
      val after = title.substring(idx)
      if (after.startsWith("#blz") || after.startsWith("#next")) title.substring(0, idx)
      else title
    } else title
  }
  
  def matchPics(url: String, docs: List[RenderDoc], siteprefix: String, matchPic: PicType): List[RenderDoc] = {
    matchPic match {
      case PicType.Inpage => {
        docs map { doc =>
          if (doc.picType.isDefined) doc
          else RenderDoc(doc.url, doc.title, "", Some(PicType.Provided), doc.score, doc.hotScore)
        }
      }
      case PicType.Insite => {
        matchDefaultPics(matchInsitePics(url, docs))
      }
      case PicType.Provided => {
        debug("## Match default pic: " + url)
        matchDefaultPics(docs)
      }
      case PicType.Text => docs
    }
  }

  def matchInsitePics(url: String, docs: List[RenderDoc]): List[RenderDoc] = {
    val usedThumbnails = new HashSet[String]
    docs filter { doc => doc.picType.isDefined } foreach { doc => usedThumbnails += doc.thumbnail }

    val nopicdocs = docs filter { doc => doc.picType.isEmpty } map { doc => doc.url }

    val doc2pics = nopicdocs map { docurl =>
      val recPics = cache.getListOrElseAsnyc(Constants.keyRecImg(docurl), 3600 * 480) {
        val doc = Servers.client.get(docurl)
        if (doc == null) List()
        else {
          val idfkeyword = DelimitedKeywordText.toIdfText(doc.keyword) { word => DictUtil.splitIdf(word)}
          val recpics = Servers.client.queryThumbnail(DomainNames.safeGetPLD(docurl), idfkeyword, 100).asScala.take(5).toList map { hd =>
            new RenderThumbnail(hd.url, hd.thumbnail, hd.score)
          }
          info("Thumbnail.search: " + docurl + " => " + recpics.size)
          recpics
        }
      }
      info("Insite.Match " + docurl + " => " + recPics.map(x => x.thumbnail))
      docurl -> recPics
    }

    val doc2vacPics = doc2pics map { case (docurl, pics) =>
      docurl -> pics.filterNot { pic => usedThumbnails.contains(pic.thumbnail) || pic.url == url }
    }

    // 采用匈牙利最大权匹配
    debug("Vacancy thumbnails: " + doc2vacPics)
    val start = System.currentTimeMillis
    val doc2pic = ThumbnailMatcher.matchThumbnails(doc2vacPics toMap)
    debug("Match insite cost: " + (System.currentTimeMillis - start))

    docs map { doc =>
      if (doc.picType.isDefined) doc
      else {
        doc2pic.get(doc.url) map { pic =>
          RenderDoc(doc.url, doc.title, pic, Some(PicType.Insite), doc.score, doc.hotScore)
        } getOrElse doc
      }
    }
  }
  
  private def matchDefaultPics(docs: List[RenderDoc]): List[RenderDoc] = {
    // assign a thumbnail for the item without thumbnail itself
    val nopicdocs = Map() ++ docs.filter { doc => doc.picType.isEmpty }.map { doc => doc.url -> doc.title }

    val start = System.currentTimeMillis
    val doc2defaultpics = ThumbnailMatcher.matchDefaultThumbnails(nopicdocs)
    debug("match default cost: " + (System.currentTimeMillis - start))

    docs map { doc =>
      if (doc.picType.isDefined) doc
      else RenderDoc(doc.url, doc.title, doc2defaultpics.getOrElse(doc.url, ""), Some(PicType.Provided), doc.score, doc.hotScore)
    }
  }

  def getThumbs(urls: List[String]): Map[String, String] = {
     val rows = hbaseStore.getRows(urls, List("thumbnail"))
    rows.flatMap { case (url, cols) =>
      cols.get("thumbnail").map { thumb =>
        url -> thumb
      }
    }
  }
}

object Render{
   
  def main(args: Array[String]) {
	  val render = new Render(null, Servers.htablePool, null)
	  println(render.getThumbs(List("http://politics.scrb.scol.com.cn/bmxx/content/2013-03/07/content_4819472.htm?node=4721")))
  }
}