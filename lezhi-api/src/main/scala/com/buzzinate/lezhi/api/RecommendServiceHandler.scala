package com.buzzinate.lezhi.api

import scala.collection.mutable.HashMap
import com.buzzinate.crawl.Item
import com.buzzinate.lezhi.behavior.ItemCFRecommend
import com.buzzinate.lezhi.elastic.RecDoc
import com.buzzinate.lezhi.elastic.SearchRecommend
import com.buzzinate.lezhi.redis.RedisCache
import com.buzzinate.lezhi.render.Render
import com.buzzinate.lezhi.render.RenderDoc
import com.buzzinate.lezhi.store.DocStatus
import com.buzzinate.lezhi.thrift._
import com.buzzinate.lezhi.util._
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.lezhi.Servers
import com.buzzinate.lezhi.Trending
import org.jsoup.Jsoup
import org.apache.commons.lang.StringUtils

class RecommendServiceHandler extends Loggable {
  val cache = new RedisCache(Servers.jedisPool)

  val recommend = new SearchRecommend
  val cfrecommend = new ItemCFRecommend(Servers.jedisPool, Servers.htablePool)
  val trend = new Trending(Servers.htablePool)

  val render = new Render(cache, Servers.htablePool, "crawl")
  val cacheDelay = Servers.prop.getInt(Constants.RESULT_CACHE_SECONDS, Constants.ONE_HOUR_SECS * 2)

  val logCollector = new LogCollector(Servers.batchPool)
  val customMeta = new CustomMetaUpdater(Servers.batchPool, Servers.htablePool, Servers.client, Servers.thumbnailFrontierClient)

  val docStatusFilter = new DocStatusFilter(new DocStatus(Servers.htablePool))
  preload

  def recommend(param: RecommendParam): RecommendResult = {
    val userid = param.userid
    val url = URLCanonicalizer.getCanonicalURL(param.url)
    val recTypes = param.types
    val title = param.title.map(title => Jsoup.parse(title).text()).getOrElse("")
    val siteprefix = param.siteprefix.getOrElse("")
    val metaKeywords = param.keywords.getOrElse("")

    val type2count = new HashMap[RecommendType, Int] with HashMapUtil.IntHashMap[RecommendType]
    param.types foreach { rt =>
      type2count.adjustOrPut(rt.recommendType, rt.count, rt.count)
    }

    param.customTitle.foreach { customTitle =>
      if (StringUtils.isNotBlank(customTitle)) customMeta ! CustomTitle(url, customTitle)
    }
    param.customThumbnail.foreach { customThumbnail =>
      if (StringUtils.isNotBlank(customThumbnail)) customMeta ! CustomThumbnail(url, customThumbnail)
    }
    logCollector ! ViewLog(userid, url, siteprefix, title, metaKeywords, System.currentTimeMillis)

    try {
      val startTime = System.currentTimeMillis

      var recs = Map[RecommendType, List[RecDoc]]()
      type2count.get(RecommendType.Trending).foreach { count =>
        val docs = cache.getListOrElse(Constants.keyTop(siteprefix), cacheDelay) {
          RecDoc.asJava(render.fillThumbnail(trend.getTrending(siteprefix, count)))
        }
        recs += RecommendType.Trending -> RecDoc.asScala(docs)

        if (!type2count.contains(RecommendType.Insite)) {
          recommend.checkCrawl(url, None, title, siteprefix)
        }
      }

      val site = DomainNames.safeGetPLD(url)
      if (siteprefix.startsWith("http://bbs.chinanews.com")) {
        type2count.get(RecommendType.Insite).foreach { count =>
          recs += RecommendType.Insite -> cfrecommend.recommend(site, title, count, metaKeywords)
        }

        type2count.get(RecommendType.Personalized).foreach { count =>
          recs += RecommendType.Personalized -> cfrecommend.recommend(site, userid, count)
        }
      } else {
        type2count.get(RecommendType.Insite).foreach { count =>
          val docs = cache.getListOrElse(Constants.keyInsite(url), cacheDelay) {
            val recommendResult = recommend.recommend(url, None, title, siteprefix, count, metaKeywords)
            RecDoc.asJava(recommendResult)
          }
          recs += RecommendType.Insite -> RecDoc.asScala(docs)
        }

        type2count.get(RecommendType.Itemcf).foreach { count =>
          recs += RecommendType.Itemcf -> cfrecommend.recommend(site, title, count, metaKeywords)
        }
      }

      recs = recs map { case (recType, docs) =>
        (recType, docStatusFilter.filter(siteprefix, docs))
      }

      val renderResult = render.rende(url, title, siteprefix, recTypes.toList, recs)
      val result = RenderDoc.convert(renderResult)

      val params = "userid=" + userid + ",siteprefix=" + siteprefix + ",recTypes=" + recTypes + ",metaKeywords=" + metaKeywords
      info("Recommend " + url + " (" + params + ",cost=" + (System.currentTimeMillis - startTime) + ", recsize=" + recSize(renderResult) + ") " + title)
      val recItemList = result.results map { case RecommendItemList(typeParam, items) =>
          val recItems = items.map { case RecommendItem(url, title, thumbnail, score, hotScore) =>
              val title2 = if (StringUtils.isNotBlank(title)) Jsoup.parse(title).text() else title
              val thumbnail2 = if (thumbnail.isDefined && Servers.imageBlacklist.contains(thumbnail.get)) None else thumbnail
              RecommendItem(url, title2, thumbnail2, score, hotScore)
          }
          RecommendItemList(typeParam, recItems)
      }
      // TODO: 处理图片纠正功能
      RecommendResult(recItemList, "")
    } catch {
      case e => {
        error("Faild to recommend " + url, e)
        RenderDoc.empty(recTypes.toList)
      }
    }
  }

  def recSize(result: List[(RecommendTypeParam, List[RenderDoc])]): String = {
    result.map { case (rtp, docs) => rtp.order + "(" + docs.size + ")" } mkString (",")
  }

  def click(param: ClickParam): Unit = {
    val fromurl = URLCanonicalizer.getCanonicalURL(param.fromurl)
    val tourl = URLCanonicalizer.getCanonicalURL(param.tourl)
    val recType = param.`type`
    val userid = param.userid
    info(userid + " click: " + userid + ", " + fromurl + " ===> " + tourl)
    logCollector ! ClickLog(userid, fromurl, tourl, recType.get, System.currentTimeMillis)
  }

  def recrawl(url: String): Unit = {
    Servers.sendQueue(Item(url, "siteprefix" -> url))
  }

  def correctImg(url: String, rightImg: String, userAgent: Option[String]): Unit = {
    // send queue
    debug("correctImg:" + url + "=>" + rightImg + ",userAgent:" + userAgent)
  }

  private def preload(): Unit = {
    info("cache delay in seconds: " + cacheDelay)
    info("preload ...")
    KeywordUtil.refineKeywords("乐知推荐")
    //		info("Elasticsearch state: " + Servers.client.state)
  }
}