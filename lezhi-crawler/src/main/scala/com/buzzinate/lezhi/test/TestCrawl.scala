package com.buzzinate.lezhi.test

import com.buzzinate.keywords.LezhiKeywordsExtractor
import com.buzzinate.model.DelimitedKeywordText
import com.buzzinate.nlp.util.TitleExtractor
import com.buzzinate.lezhi.crawl.batch.KeywordInfoUtil
import org.jsoup.Jsoup
import org.buzzinate.lezhi.api.Doc
import org.buzzinate.lezhi.util.SignatureUtil
import org.buzzinate.lezhi.api.Client
import org.buzzinate.lezhi.util.LargestTitle
import java.util.Arrays
import com.buzzinate.lezhi.crawl.Vars
import collection.JavaConverters._
import com.buzzinate.http.PolicyHttpScheduler
import com.buzzinate.http.Http
import java.util.concurrent.Executors

object TestCrawl {
  val te = new TitleExtractor
  
  def main(args: Array[String]): Unit = {
    if (args.length < 1) {
      println("Usage: TestCrawl <url>")
      return
    }
    val http = new PolicyHttpScheduler(Http.buildAgent(100, 2), Executors.newFixedThreadPool(64), 100, 100)
    
    val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")
    val vars = new Vars(prop.asInstanceOf[java.util.Map[String, String]].asScala.toMap)
    val client = new Client(vars.elasticsearchHosts.asJava)
    
    val url = args(0)
    http.get(url) onSuccess { br =>
      val resp = br.toHtml(url)
      if (resp.ok) {
        val keywords = LezhiKeywordsExtractor.extract(url, resp.html)
        println("url: " + keywords)
        val keywordstr = DelimitedKeywordText.toText(KeywordInfoUtil.convert(keywords))
        val rawTitle = Jsoup.parse(Jsoup.parse(resp.html, url).title).text()
        val title = te.extract(rawTitle)
        val thumbnail = ""
        val doc = new Doc(url, rawTitle, SignatureUtil.signature(LargestTitle.parseLargest(title)), thumbnail, keywordstr, System.currentTimeMillis)
        client.bulkAdd(Arrays.asList(doc))
      }
      client.close
    }
    
  }
}