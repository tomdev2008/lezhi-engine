package com.buzzinate.keywords

import java.util.Date
import org.jsoup.Jsoup
import com.buzzinate.keywords.util.ExtractUtil
import com.buzzinate.keywords.util.ThumbnailExtractor
import com.buzzinate.http.PolicyHttpScheduler
import com.buzzinate.http.Http
import java.util.concurrent.Executors

object TestThumbnail {
  def main(args: Array[String]): Unit = {
    val urls = List(
        
      "http://mrwanju.abang.com/od/omei/a/peter-rabbit1.htm",
      "http://mrwanju.abang.com/od/omei/a/disney5.htm",
      "http://mrwanju.abang.com/od/rihan/ig/hellokitty27/kitty01.-0wZ.htm#blz-insite",
      "http://mrwanju.abang.com/od/rihan/ig/comic/seedlacus.htm#blz-trending",
      "http://mrwanju.abang.com/od/rihan/ig/penne/nyokkipenne2.htm#blz-trending",
      "http://mrwanju.abang.com/od/omei/a/hamburger_p2.htm#blz-trending",
      
      "http://in.nen.com.cn/system/2013/03/14/010270537.shtml",
      "http://www.it-bound.com/xinwenzixun/yejiedongtai/18208.html",
      "http://www.it-bound.com/yidongtianxia/pingguo/18084.html#blz-insite",

      "http://www.baob520.net/a/qingganqingsu/20121224/275.html",
      "http://www.baob520.net/a/qingganqingsu/20121224/275_2.html",

      "http://www.vancl.bz/post/210.html",
      "http://www.vancl.bz/post/129.html#blz-insite",
      "http://www.vancl.bz/post/7.html#blz-insite",
      "http://www.vancl.bz/post/164.html#blz-insite",

      "http://www.junshilu.com/bencandy.php?aid=335&fid=50&page=4#blz-insite",
      "http://www.junshilu.com/bencandy.php?fid=4&id=72#blz-insite",

      "http://www.51xupu.com/guangbo/info/299.html",
      "http://www.51xupu.com/guangbo/info/216.html",

      "http://www.yhqyj.com/Detail.asp?id=1452",
      "http://www.yhqyj.com/Detail.asp?id=1487#blz-insite",
      "http://www.yhqyj.com/Detail.asp?id=1486#blz-insite",
      "http://www.yhqyj.com/Detail.asp?id=1488#blz-insite",

      "http://www.u0756.com/index.php?m-cms-q-view-id-4038.html",
      "http://www.u0756.com/mode.php?id=3343&m=cms&q=view#blz-insite",
      "http://www.u0756.com/index.php?m-cms-q-view-id-3359.html#blz-insite",

      "http://lusongsong.com/reed/605.html",
      "http://lusongsong.com/reed/604.html",
      "http://lusongsong.com/reed/602.html",

      "http://www.jiadianpj.com/baike/show-114.html#blz-insite",
      "http://www.jiadianpj.com/news/show-8620.html",
      "http://www.jiadianpj.com/baike/show-353.html#blz-insite",

      "http://www.shangjie.biz/shangye/ss/2012/1112/311095.html#blz-insite",
      "http://www.shangjie.biz/news/cj/2012/0924/290810.html#blz-insite",
      "http://www.shangjie.biz/news/yw/2012/0921/290360.html#blz-insite",
      "http://www.shangjie.biz/wenhua/jy/2013/0118/336025.html",
      "http://www.shangjie.biz/shangye/ss/2012/0802/277026.html#blz-insite",
      "http://www.shangjie.biz/news/cj/2012/0920/290045.html#blz-insite",

      "http://www.hinews.cn/news/system/2013/01/22/015377523.shtml",
      "http://www.hinews.cn/news/system/2013/01/21/015374842.shtml",

      "http://www.huuxuu.com/park/2013/0118/1535.html",
      "http://www.huuxuu.com/park/2013/0118/1534.html#blz-insite",
      "http://www.huuxuu.com/park/2013/0117/1510.html#blz-insite",

      "http://book.k618.cn/qzfd/201210/t20121006_2486708.html",
      "http://book.k618.cn/zxyd/yd/201212/t20121211_2701507.html#blz-insite",
      "http://book.k618.cn/zxyd/yd/201301/t20130109_2790653.html#blz-insite",
      "http://book.k618.cn/zxyd/yd/201211/t20121130_2667862.html#blz-insite",

      "http://politics.scdaily.cn/szyw/content/2013-01/22/content_4630879.htm?node=3605",
      "http://politics.scdaily.cn/szyw/content/2013-01/18/content_4619694.htm#blz-insite",
      "http://politics.scdaily.cn/szyw/content/2013-01/18/content_4619461.htm?node=3605#blz-insite",

      "http://news.ynet.com/1183/2012/12/27/503@395395.htm#blz-insite",
      "http://news.ynet.com/1183/2013/01/06/344@398387.htm#blz-insite",
      "http://news.ynet.com/1183/2013/01/04/343@397514.htm#blz-insite",

      "http://shanghai.51chudui.com/Class/fandian/20131/340484.html#blz-insite",
      "http://shanghai.51chudui.com/Class/chaoshi/20115/81813.html#blz-insite",
      "http://shanghai.51chudui.com/Class/fandian/20112/58106.html#blz-insite",
      "http://shanghai.51chudui.com/Class/kongpu/20131/325863.html",

      "http://www.bjd.com.cn/10cj/201211/27/t20121127_3444254.html#blz-insite",
      "http://www.bjd.com.cn/10cj/201211/28/t20121128_3444462.html#blz-insite",
      "http://www.bjd.com.cn/jryw/201301/22/t20130122_3476914.html",

      "http://www.iheima.com/html/2012/socialmouths_1107/3959.html#blz-insite",
      "http://www.iheima.com/html/2013/cxzc_0117/5081.html",

      "http://mobile.it168.com/a2013/0111/1445/000001445942_5.shtml#blz-insite",
      "http://mobile.it168.com/a2013/0115/1446/000001446831.shtml#blz-insite",
      "http://mobile.it168.com/a2013/0111/1445/000001445942.shtml",

      "http://www.hc699.com/article-544-1.html#blz-trending",
      "http://www.hc699.com/ask-detail-347.html#blz-insite",
      "http://www.hc699.com/plugin.php?aid=607&id=ask%3Adetail#blz-insite",
      "http://www.hc699.com/ask-detail-1201.html#blz-insite",
      "http://www.hc699.com/article-439-1.html",

      "http://dota.uuu9.com/201101/69963_4.shtml",
      "http://dota.uuu9.com/200811/49613.shtml",
      "http://dota.uuu9.com/201207/88939.shtml",
      "http://dota.uuu9.com/201210/93504.shtml",
      "http://dota.uuu9.com/201211/93613.shtml",

      "http://news.k618.cn/xy_37043/201211/t20121118_2625197.html#blz-insite",
      "http://news.k618.cn/ty_37053/201301/t20130121_2824073.html#blz-trending",
      "http://news.k618.cn/ztx/201212/t20121219_2726223_1.html#blz-insite",
      "http://news.k618.cn/ztx/201301/t20130122_2826704.html",
      "http://news.yninfo.com/yn/kmxw/201108/t20110828_1702774.htm#blz-insite",
      "http://news.yninfo.com/yn/kmxw/201111/t20111117_1726598.htm#blz-insite",
      "http://news.yninfo.com/yn/jjxw/201202/t20120226_1759079.htm#blz-insite",
      "http://www.21ccom.net/articles/qqsw/qyyj/article_2012122673734_2.html#blz-insite",
      "http://www.21ccom.net/plus/view.php?aid=23993#blz-insite",
      "http://www.21ccom.net/articles/zgyj/ggcx/article_2013010374141.html#blz-insite",
      "http://www.21ccom.net/articles/zgyj/ggcx/article_2013012075419.html#blz-insite",
      "http://opinion.m4.cn/2012-12/1192579.shtml#blz-insite",
      "http://opinion.m4.cn/2012-11/1190348.shtml#blz-insite",
      "http://opinion.m4.cn/2012-11/1192246.shtml#blz-insite",
      "http://www.chexun.com/2012-10-25/101440319.html#blz-insite",
      "http://health.chinadaily.com.cn/2013-01/18/content_16136893_2.htm",
      "http://health.chinadaily.com.cn/2012-11/20/content_15945161_2.htm",
      "http://health.chinadaily.com.cn/2012-11/23/content_15952988_8.htm",
      "http://health.chinadaily.com.cn/2012-11/23/content_15953655.htm#blz-insite",
      "http://health.chinadaily.com.cn/2013-01/18/content_16136893_2.htm",
      "http://health.chinadaily.com.cn/2013-01/18/content_16136893_3.htm#blz-insite",
      "http://xx.yzz.cn/leiren/201301/571698.shtml",
      "http://xx.yzz.cn/leiren/201301/571538.shtml",
      "http://bbs.chinanews.com/web/tp/hd/2011/10-20/23626.shtml",
      "http://bbs.chinanews.com/web/tp/hd/2012/10-17/99983.shtml",
      "http://mil.m4.cn/2012-12/1193908.shtml",
      "http://test.buzzinate.com/wordpress/?p=6894",
      "http://test.buzzinate.com/wordpress/?p=5858",
      "http://opinion.m4.cn/2012-11/1191788.shtml",
      "http://opinion.m4.cn/2012-09/1185627.shtml",
      "http://www.chinadaily.com.cn/hqgj/jryw/2012-11-17/content_7530538.html",
      "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-18/content_7279084.html",
      "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-18/content_7272911.html",
      "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-02/content_7408792.html",

      "http://www.fanjian8.com/show/903.html",
      "http://www.fanjian8.com/show/9662.html",
      "http://www.fanjian8.com/show/5949.html",
      "http://e.gmw.cn/2012-10/23/content_5460869_10.htm",
      "http://www.fanjian8.com/show/14797.html",
      "http://www.chinadaily.com.cn/micro-reading/dzh/2012-10-12/content_7223986.html",
      "http://www.fanjian8.com/show/9860.html",
      "http://www.fanjian8.com/show/7373.html",
      "http://www.fanjian8.com/show/12391.html",
      "http://www.36kr.com/p/158016.html",
      "http://www.chinadaily.com.cn/dfpd/shehui/2012-10/10/content_15805651.htm",
      "http://www.fanjian8.com/show/11832.html",
      "http://www.yangod.com/archives/6027/wanda",
      "http://news.cnnb.com.cn/system/2012/08/28/007436125.shtml",
      "http://news.cnnb.com.cn/system/2012/08/28/007436135.shtml",
      "http://news.uuu9.com/2011/201111/231039.shtml",
      "http://www.36kr.com/p/37102.html",
      "http://www.meizico.com/kittysex",
      "http://www.meizico.com/xiatian",
      "http://luo.bo/28285/",
      "http://luo.bo/28208/",
      "http://www.infoq.com/cn/news/2012/03/senseidb-1-0-0",
      "http://www.williamlong.info/archives/1507.html",
      "http://www.williamlong.info/archives/2984.html",
      "http://www.williamlong.info/archives/3025.html",
      "http://www.williamlong.info/archives/394.html",
      "http://www.iteye.com/news/24617",
      "http://news.sina.com.cn/s/p/2012-03-22/052424154750.shtml",
      "http://www.infoq.com/cn/articles/google-dart",
      "http://blogread.cn/it/article.php?id=4948&f=sinat",
      "http://blogread.cn/it/article.php?id=5053&f=sinat",
      "http://wallstreetcn.com/node/9561",
      "http://en.wikibooks.org/wiki/Haskell/Applicative_Functors",
      "http://www.ifanr.com/78052",
      "http://blog.chinabyte.com/a/2656904.html",
      "http://finance.chinanews.com/cj/2012/08-21/4120640.shtml",
      "http://www.infoq.com/cn/news/2012/03/zk-6-released",
      "http://www.williamlong.info/archives/2923.html",
      "http://xx.yzz.cn/leiren/201301/571698.shtml",
      "http://xx.yzz.cn/leiren/201301/571538.shtml",
      "http://xx.yzz.cn/leiren/201301/572476_27.shtml",
      "http://photo.gmw.cn/2013-01/16/content_6390552.htm",
      "http://culture.gmw.cn/2012-12/06/content_5930598.htm",
      "http://mil.gmw.cn/2012-12/28/content_6178312.htm#blz-insite",
      "http://mil.gmw.cn/2012-09/23/content_5176012.htm",
      "http://mil.gmw.cn/2012-10/28/content_5504816.htm#blz-insite",
      "http://mil.gmw.cn/2012-02/14/content_3568680.htm#blz-insite",
      "http://mil.gmw.cn/2011-08/15/content_2457220.htm#blz-insite",
      "http://www.chinadaily.com.cn/micro-reading/politics/2013-01-15/content_8031962.html")

    val client = new PolicyHttpScheduler(Http.buildAgent(100, 2), Executors.newFixedThreadPool(64), 100, 100)
    for (url <- urls) {
      client.get(url) onSuccess { br =>
        val resp = br.toHtml(url)
        val doc = Jsoup.parse(resp.html, url)
       
        val title = ExtractUtil.extractTitle(doc.body, doc.title)
        //        val blocks = ExtractUtil.extractBlocks(doc, title)
        ThumbnailExtractor.extractThumbnail(client, doc, title, url) match { case optionImg =>
          val img = if (optionImg == None) null else optionImg.get
          println(url)
          println(url + " => " + img)
          println
        }
      }
    }
  }
}