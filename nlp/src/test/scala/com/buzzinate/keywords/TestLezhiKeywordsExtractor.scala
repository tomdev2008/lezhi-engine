package com.buzzinate.keywords

import collection.JavaConverters._
import com.buzzinate.http.PolicyHttpScheduler
import com.buzzinate.http.Http
import com.buzzinate.http.Http
import java.util.concurrent.Executors

object TestLezhiKeywordsExtractor {
  def main(args: Array[String]): Unit = {
    val urls = List(
        "http://www.gmw.cn/cg/2013-03/05/content_6896942.htm",
        "http://e.gmw.cn/2012-11/26/content_5806068.htm",
        "http://www.docin.com/p-96249765.html",
        "http://www.chexun.com/2012-02-15/100503456_1.html",
        "http://e.gmw.cn/2012-07/31/content_4677804_2.htm",
        "http://e.gmw.cn/2012-10/15/content_5366112_4.htm",
        "http://e.gmw.cn/2012-10/16/content_5381093_4.htm",
        "http://www.chinadaily.com.cn/hqcj/zgjj/2012-12-06/content_7697417.html",
        "http://www.iteye.com/topic/1128172",
        "http://www.yxad.com/sina/402318334.html",
        "http://www.yxad.com/sina/492712431.html",
        "http://www.yxad.com/sina/494061558.html",
        "http://forum.china.com.cn/thread-2392252-1-1.html",
        "http://bbs.voc.com.cn/topic-1434709-1-1.html",
        "http://www.yxad.com/sina/352546467.html",
        "http://www.yxad.com/sina/479520916.html",
        "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-13/content_7492510_12.html",
        "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-05/content_7431082.html",
        "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-02/content_7408792.html",
        "http://dota.uuu9.com/201206/88135.shtml",
        "http://dota.uuu9.com/201211/93663.shtml",
        "http://www.chinadaily.com.cn/dfpd/shehui/2012-09/11/content_15751025.htm",
        "http://www.chinadaily.com.cn/dfpd/shehui/2012-10/10/content_15805651.htm",
        "http://news.subaonet.com/sports/2012/0110/854834.shtml",
        "http://fm.m4.cn/2012-10/1186220.shtml",
        "http://news.subaonet.com/2012/0921/1002468.shtml",
        "http://yj.soufun.com/tiaozao/detail_1015510/",
        //"http://www.airm.cn/index.php/Seojs/contentSeojs?id=65", 
        "http://www.yangod.com/archives/27342",
        "http://article.woshao.com/9ad6dee6415a11e081e1000c2959fd2a/",
        "http://article.woshao.com/dd563b6cec0911e1a0aa000c291345e1/",
        "http://www.chinanews.com/mil/2012/06-15/3965152.shtml",
        "http://article.woshao.com/8a56c448b79a11e1a309000c29fa3b3a/",
        "http://article.woshao.com/de889074f2bd11e1a0aa000c291345e1",
        "http://article.woshao.com/caaea4f2bca611df8ddb000c295b2b8d/",
        "http://www.chinanews.com/tp/hd2011/2012/05-14/102539.shtml",
        "http://www.minigu.cn/shougongzhizuo/2012082735.html",
        "http://nlq.name/confused/182.html",
        "http://article.woshao.com/ecc5bd3ecc9211e19097000c29fa3b3a",
        "http://astro.women.sohu.com/20120821/n350910526_9.shtml",
        "http://news.cnnb.com.cn/system/2012/07/31/007402196.shtml",
        "http://article.woshao.com/796cfd2a642d11e1a75c000c2959fd2a",
        "http://news.cnnb.com.cn/system/2012/08/21/007428900.shtml",
        "http://news.cnnb.com.cn/system/2012/08/21/007428848.shtml",
        "http://news.cnnb.com.cn/system/2012/08/16/007423478.shtml",
        "http://article.woshao.com/d414a2ec101311e081e3000c295b2b8d/",
        "http://www.chinanews.com/ny/2012/06-27/3989397.shtml",
        "http://luo.bo/26539/",
        "http://finance.chinanews.com/auto/2012/04-13/3817408.shtml",
        "http://www.chinanews.com/yl/2012/01-29/3626835.shtml",
        "http://sports.163.com/09/0707/14/5DKJVFM000051C8V.html",
        "http://www.chinanews.com/tp/hd2011/2011/12-16/80086.shtml",
        "http://www.36kr.com/p/119160.html",
        "http://www.36kr.com/p/109843.html",
        "http://www.infoq.com/cn/news/2012/03/senseidb-1-0-0",
        "http://www.williamlong.info/archives/394.html",
        "http://www.williamlong.info/archives/1507.html",
        "http://www.williamlong.info/archives/2984.html",
        "http://www.williamlong.info/archives/3012.html",
        "http://www.williamlong.info/archives/3025.html",
        "http://www.williamlong.info/archives/3085.html",
        "http://www.williamlong.info/archives/3152.html",
        "http://www.iteye.com/news/24617",
        "http://news.sina.com.cn/s/p/2012-03-22/052424154750.shtml",
        "http://www.tianya.cn/publicforum/content/develop/1/844680.shtml",
        "http://www.infoq.com/cn/articles/google-dart",
        "http://blogread.cn/it/article.php?id=4948&f=sinat",
        "http://blogread.cn/it/article.php?id=5053&f=sinat",
        "http://wallstreetcn.com/node/9561",
        "http://www.infoq.com/articles/haywood-ddd-no",
    	"http://www.ifanr.com/78052",
        "http://blog.chinabyte.com/a/2656904.html",
        "http://blog.chinabyte.com/a/2717291.html",
        "http://www.infoq.com/cn/news/2012/03/zk-6-released",
        "http://liudie.bokee.com/5572787.html",
        
        "http://luo.bo/25499/",
        "http://luo.bo/4868/",
        "http://luo.bo/28887/",
        "http://luo.bo/25736/",
        "http://luo.bo/2180/",
        "http://luo.bo/26636/",
        "http://luo.bo/26263/",
        "http://luo.bo/28630/",
        "http://www.20ju.com/content/V220643.htm", 
        "http://lol.uuu9.com/lolgl/strategy_pages.aspx?raiderid=3606", 
        "http://comic.emland.net/cms.php?id=1017",
        "http://www.maitian520.com/", 
        "http://hunjia.55bbs.com/2011/1028/1731769.shtml", 
        "http://bbs.voc.com.cn/topic-4773760-1-1.html",
        "http://www.williamlong.info/archives/2923.html",
        "http://luo.bo/18517/"
      )
      
      val client = new PolicyHttpScheduler(Http.buildAgent(100, 2), Executors.newFixedThreadPool(64), 100, 100)
      for (url <- urls) {
        client.get(url) onSuccess { br =>
          try {
            val resp = br.toHtml(url)
            println("######")
            println(url + " ==> ")
            val keywords = LezhiKeywordsExtractor.extract(url, resp.html)
            println(keywords.filter(kw => kw.field == 3).map(kw => kw.word + "(" + kw.freq + ")"))
            println(keywords.filter(kw => kw.field == 2).map(kw => kw.word + "(" + kw.freq + ")"))
            println(keywords.filter(kw => kw.field == 1).map(kw => kw.word + "(" + kw.freq + ")"))
            println(keywords.filter(kw => kw.field == 0).map(kw => kw.word + "(" + kw.freq + ")"))
            println
          } catch {
            case t => System.err.println(url + " => " + t.getMessage)
          }
        }
      }
  }
}