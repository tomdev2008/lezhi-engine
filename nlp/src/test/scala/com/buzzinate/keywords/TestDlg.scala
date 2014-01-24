package com.buzzinate.keywords

import com.buzzinate.nlp.util.TextUtil
import com.buzzinate.nlp.segment.TextProcess
import java.util.regex.Pattern
import com.buzzinate.keywords.content.DlgExtractor
import org.jsoup.Jsoup
import com.buzzinate.keywords.util.ExtractUtil
import com.buzzinate.keywords.util.SnippetBlock
import com.buzzinate.nlp.util.TitleExtractor
import com.buzzinate.http.PolicyHttpScheduler
import com.buzzinate.http.Http
import java.util.concurrent.Executors

object TestDlg {
  val urlReg = Pattern.compile("http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", Pattern.CASE_INSENSITIVE)
   
  def main(args: Array[String]): Unit = {
//    val url = "http://www.yxad.com/sina/494061558.html"
//    val url = "http://news.cnnb.com.cn/system/2012/08/21/007428900.shtml"
//    val url = "http://e.gmw.cn/2012-10/15/content_5366112_4.htm"
//    val url = "http://e.gmw.cn/2012-10/16/content_5381093_4.htm"
//    val url = "http://www.infoq.com/cn/articles/google-dart"
//    val url = "http://blogread.cn/it/article.php?id=4948&f=sinat"
//    val url = "http://www.yxad.com/sina/479520916.html"
//    val url = "http://www.yxad.com/sina/487715636.html"
//    val url = "http://ai.9ta.org/play/?53907-0-0.html"
//    val url = "http://www.36kr.com/p/109843.html"
//    val url = "http://www.20ju.com/content/V220643.htm"
//    val url = "http://astro.women.sohu.com/20120821/n350910526_9.shtml"
//     val url = "http://happy.gmw.cn/archives/46031"
//    val url = "http://www.fanjian8.com/show/14797.html"
//    val url = "http://bbs.voc.com.cn/topic-4773760-1-1.html"
//    val url = "http://astro.women.sohu.com/20120821/n350910526_9.shtml"
//     val url = "http://blog.zzsmo.com/2012/06/bshareandsina/"
//    val url = "http://www.hualongxiang.com/longchengcheyou/10094713"
//     val url = "http://blog.zzsmo.com/2012/08/2012hnzzdahui/"
//    val url = "http://blog.zzsmo.com/2012/07/gxyushhpz/"
//    val url = "http://opinion.m4.cn/wb/2012-11/1190973.shtml"
//    val url =  "http://www.chinadaily.com.cn/hqsj/hqlw/2012-11-28/content_7619275_2.html"
//    val url = "http://opinion.m4.cn/wb/2012-11/1190973.shtml"
//    val url = "http://www.hualongxiang.com/xueche/10089595"
//    val url = "http://www.tianya.cn/publicforum/content/develop/1/844680.shtml"
    val url = "http://forum.china.com.cn/thread-2789820-1-1.html"
//    val url = "http://luo.bo/18517/"
//    val url = "http://dota.uuu9.com/201211/93663.shtml"
//    val url = "http://forum.china.com.cn/thread-2392252-1-1.html"
//      val url = "http://www.36kr.com/choozon/"
//    val url = "http://www.infoq.com/articles/haywood-ddd-no"
//    val url = "http://view.gmw.cn/2012-11/05/content_5581014.htm"
//    val url = "http://www.infoq.com/cn/news/2012/11/go-error-handle"
//    val url = "http://www.infoq.com/cn/articles/spring-integration-in-action-book-review"
//    val url = "http://www.infoq.com/cn/articles/dolphin-browser-cloud-way"
//    val url = "http://www.cnbeta.com/articles/215751.htm"
//    val url = "http://www.iteye.com/topic/1127094"
//    val url = "http://www.csdn.net/article/2012-11-27/2812241-next-up-exascale-computers" // issue
//    val url = "http://www.csdn.net/article/2012-11-27/2812205-Hadoop-HBase"
//    val url = "http://e.gmw.cn/2012-10/16/content_5381093_4.htm"
//    val url = "http://it.gmw.cn/2012-11/26/content_5795071.htm"
      
    val client = new PolicyHttpScheduler(Http.buildAgent(100, 2), Executors.newFixedThreadPool(64), 100, 100)
    
    client.get(url) onSuccess { br =>
      val resp = br.toHtml(url)
//    for (i <- 0 until 1000000) {
      val doc = Jsoup.parse(resp.html, url)
      ExtractUtil.cleanup(doc.body)
//    doc.select(":containsOwn(\u00a0)").remove
      val title = ExtractUtil.extractTitle(doc.body, doc.title)
      println(title)
      val blocks = ExtractUtil.extractBlocks(doc, title) map { block =>
        SnippetBlock(block.snippets map { snippet => TextProcess.normalize(urlReg.matcher(snippet).replaceAll(""))}, block.score, block.isArticle, block.imgs)
      }
    
//    val blocks = List(SnippetBlock(weibo.split("\n").toList, 5d, true, List()))
//    val title = ""
 
//    val snippets = ExtractUtil.extractSnippets(Jsoup.parse(html1, url1))
//    val a = HtmlExtractor.extract(html, url)
//    val snippets = List(a.title) ++ a.snippets ++ a.metaKeywords
//    val snippets = List("金山毒霸2013悟空版：更小、更快、更便捷", "金山毒霸2013是金山公司开发的杀毒软件，可以查杀和预防电脑上的木马病毒，保护计算机安全。金山毒霸2013正式版依托独家99秒快速鉴定能力，为你的电脑建立了一条完整的防线，每一个进入的电脑文件都经过了 ......")
//    val snippets = List("Interview and Book Excerpt: Dan Haywood's Domain-Driven Design Using Naked Objects", "Domain-Driven Design Using Naked Objects book, by Dan Haywood, covers the Java application development using Domain-Driven Design techniques and the open-source Java framework Naked Objects (which is now part of the Apache Isis incubator project). In the book, Dan discusses how Java developers can develop and test domain applications by focusing on the business domain model and let the framework take care of the infrastructure related code and configuration elements.")
      DlgExtractor.extract(TextProcess.normalize(title), TextProcess.normalize(doc.title), blocks, 5).sortBy{ case (word, freq) => -freq} foreach { case (word, freq) =>
        println(word + " => " + freq)
      }
//    }      
    }
  }
  
  val weibo = """买了船票可以退吗？
【危地马拉马雅领导人澄清：12月21日不是世界末日】危地马拉印第安马雅人的3名领导人昨天澄清说，12月21日发生的纪元的变更将不是世界的末日。“散布世界末日的不是马雅人，是那些研究印第安人民的人，他们歪曲了那些信息。”（人民网）  
 // 高效的MySQL分页   
高科技 // Facebook的朋友推荐系统   
不错的介绍 // 搜索引擎技术   
11年阿。。。
01年在网上认识了一个mm，和她聊天挺有意思的，后来不知怎么地就失去联系了……最近和老爸聊天，才知道她当时给我写了一封信，老爸怕影响我学习就默默扣下来没跟我说。11年后看到这封信感慨万千，就手写了一封回信给她。不知道万能的 能不能让她看到？
#创业挑战赛总决赛#个性化阅读器“乐知”里面的一个频道叫“猜你喜欢”，只要使用新浪 的账号登录，它就能推荐给你喜欢的资讯，而其他的阅读器多以“问题”或者“订阅”的方式来判断读者的兴趣从而进行推荐。而乐知需要你做的仅仅是登录，这就是乐知的神奇之处。
不错 
#创业挑战赛总决赛#一直使用乐知，还不错，猜你喜欢频道很棒。。。。。
数据结构也是个酒鬼 // 爱喝啤酒的程序员是如何学习数据结构的   
难懂。。。 // 复杂网络2012年度盘点：博弈+传播+控制   
#比你更懂你#NoSQL数据库的分布式算法：本文译自 Distributed Algorithms in NoSQL Databases 系统的可扩展性是推动NoSQL运动发展的的主要理由，...  
#比你更懂你#月PV破15亿：Tumblr架构揭密  
#比你更懂你#Ted Yu是Apache HBase的PMC成员，目前在PMC中只有23名成员。Apache项目按照贡献度“论资排辈”，只有作出足够的核心贡献才能进入PMC。日前，CSDN对其进行了采访，畅谈了自己的成长故事。  
#比你更懂你#Cloudera Impala™ provides fast, interactive SQL queries directly on your Apache Hadoop data stored in HDFS or HBase. This post will explain how to …  
15 billion page views #比你更懂你#  
#比你更懂你#对于我们来讲，非常重要一点，我们认为到今天非常高兴看到，所有的商家已经完全超过以前阶段的认知。认为这次无非就是搞一个大促，一个促销，我就搞一点货放到网上就行了。其实所有的商家都明白，双十一是一天，但绝  
用么？//  国内终于也逐步的有靠谱的云服务出现了，创业公司的一大福音啊。 //  和其他小型创业团队交流过这个问题，作为机房小客户，自己做运维托管的风险不比云服务小，麻烦 。几家大公司云的技术响应太糟糕。所以不妨试一下Ucloud，七牛这样的，纯技术基因的创业云服务。
我们之前用的是盛大云，不到一个月， 就被气疯了，发了篇千字文大骂一顿之后，云主机搬去  ，云存储与CDN搬去 。前前后后磨合了大约半年，云服务质量的不断提升是可信赖的，技术响应速度更是无可挑剔。把云端放在创业产品上自然有风险，但总得有人来吃这只螃蟹。
 Scala是为优秀程序员准备的
#比你更懂你#  
#比你更懂你#  
 // 大妈啥时候整点machine learning//  牛
北京大妈的牛逼不是语言能形容的！！！  
#比你更懂你#【导语】“大数据不是炒作，也不是泡沫。Hadoop在未来将继续追随谷歌的脚步。”Hadoop的创造者兼Apache Hadoop项目创始人Doug Cutting近日表示。    
#比你更懂你# How TinEye image search engine works   
#比你更懂你#What I wrote before about Cloudera Impala was quite incomplete. After a followup call, I now feel I have a better handle on the whole thing. First, some  
#比你更懂你# Driving the Databus        
#比你更懂你#Cloudera offers enterprises a powerful new data platform built on the popular Apache Hadoop open-source software package.  
  
【11月初易有好运的星座】NO.1 天蝎座（全方位）；NO.2 摩羯座（爱情运、财运）；NO.3 狮子座（财运、恋爱运） ；NO.4 处女座（事业运）；NO.5 天秤座（旅行运、财运）
某 天，佛罗里达州朱庇特镇的数学家 Zachary Harris 收到了一封奇怪的邮件。这封邮件来自 Google 的招聘人员，问他是否对网站可靠性工程师的职位感兴趣。“很显然你对 Linux 和编程有激情”，信件中这样写道，”我希望知道，  
基于redis构建系统的经验和教训   
如何提高mysql insert性能
来源：tigernorth  
来源：tigernorth  
LLVM之爷 发了一封邮件，希望他帮忙推荐一些编译方向的学生到Google做实习生，或者是直接到Google工作。其中，列举了一些Google编译组打算让实习生或者全职工程师们开展工作的一些方向。  
神作阿，拜读！//    levelDb剖析。
【#SAE技术分享#】LevelDb是Google 公司Jeff Dean和Sanjay Ghemawat这两位大神级别的工程师发起的开源项目，是能够处理十亿级别规模Key-Value型数据持久性存储的C++ 程序库。本文从整体架构、log文件、SSTable文件、MemTable、写入与删除记录、读取记录等多个方面来分析LevelDb。 
文章标题：Oracle未雨绸缪:首曝JDK十年发展路线图。中国IT实验室JAVA频道是一个专业的JAVA技术平台，着眼于业界尖端技术，提供及时全面的JAVA技术和资讯文章，为广大的JAVA爱好者提供一个技术学习共享的资源库。  
【商品图片】——一个商品有5个图片，商品描述里面有 图片，你猜淘宝有多少张图片要存储？100亿以上。这么多图片要是在你的硬盘里面，你怎么去查找其中的一张？要是你的同学想拷贝你的图片，你需要他准备多少  
  
哇 
世界上最mini的、最便宜的Hadoop服务器：我成功地把安卓智能电视棒改造成Hadoop服务器了，比信用卡还小，可能是世界上最mini的Hadoop服务器，更可能是最便宜的Hadoop服务器了。 开工之前，我们...   （使用新浪长 工具发布  ）
对于一个大型网站来说，负载均衡是永恒的话题。随着硬件技术的迅猛发展，越来越多的负载均衡硬件设备涌现出来，如F5 BIG-IP、Citrix NetScaler、Radware等等，虽然可以解决问题，但其高昂的价格却往往令人望而却步，因此负载均衡软件仍然是大部分公司的不二之选。  
前面的内容请看：Java、Scala和Go语言多线程并发对比测试。  相关的代码下载： 测试结果和结论统计1～N个自然数里面有多少个质数，并记录所花费时间。相同的N，时间越少性能越好。AMD 双核 2.8G ,4G内存 winxp               java+conc java+A  
前段时间有高人写了一篇《面对一个全新的环境,作为一个Oracle DBA,首先应该了解什么》，本文借花献佛，总结了一些思路，如何面对一个全新的Mysql环境。1、先要了解当前的Mysql数据库的版本和平台以及字符集等相关信息mysql> status--------------mysql Ver   
It's a no-brainer: well performing websites enjoy higher visitor engagement, retention and conversion. Given how fickle users can be, plus the fact that mobile  
1、栈和队列的区别是啥？ 吃多了拉就是队列；吃多了吐就是栈 2、世界上最遥远的距离不是生与死，而是你亲手制造的BUG就在你眼前，你却怎么都找不到她。。。 3、《c++程序设计语言》比《c程序设计语言》厚了几倍。。。果然有了对象就麻烦很多。。。 4、怎么使用面向...  
对s个串编号，1，2。。。n，kmp找到所有s串在T中所有起始和终止位置并标记下来，然后找一个长串包括从1到n  
 低成本高性能云数据架构探索  
从图中可以看出，其内部也分为两个部分，前面是一个个KV记录，其顺序是根据Key值由小到大排列的，在Block尾部则是一些“重启点”（Restart Point）,其实是一些指针，指出Block内  
例如，为了更轻松地识别不同的人，他把“人”作为了一种基本的数据类型，并让新编程语言能够读懂这种类型，就像其他语言能读懂字符串或整数一样。随后，他为这些创意设置了简单的句法（syntax），这些句法使用  
但从其他方面来看： Eucalyptus由于出现最早，同时与AWS签订相关API兼容协议，在面向AWS生态环境的私有云市场处于领先地位； CloudStack在经过大量商业客户公有云的部署后，其功能已  
CC 节点，并通过一个消息机制和各个组件进行交互，其优势就是能构建巨大的云平台环境，从目前掌握的资料而言，一些公共开源云环境均采用这种模式，但结构复杂，运维和管理相对困难。  
专业面向开发者的中文技术问答社区  
新浪 既拥有Twitter般的内容，有拥有Facebook般的形式，Twitter+Facebook，按照国外分析师的观点，这不是要逆天了吗？​  
  
谢文：大数据概念混乱 未来或将卷入混战 实现数据的标准化、开放化和通用化，关键在于如何冲破个人隐私、商业利益、行业垄断的限制，实现数据的低成本、高效率、大规模的聚集和整合。  
//    
32个非常重要的算法，你全都知道吗？不全知道没关系，我们也是，今后的一段日子，请关注我们，正好我们一块学习，我们会对每个算法整理出比较好的资料，然后和大家分享，交流。
号外！Cloudera发布了实时查询开源项目Impala！多款产品实测表明，比原来基于MapReduce的Hive SQL查询速度提升3～90倍。Impala是Google Dremel的模仿，但在SQL功能上青出于蓝胜于蓝。  
Good Freely Available Textbooks on Machine Learning  
Outbrain是一家内容推荐引擎公司，该公司为博客主和新闻网站，提供了一个插件，装上该插件后，每篇博客文章和新闻的最后将出现一个评分界面，读者可以对文章内容进行评分。与此同时，读者也将看到一些个性化的推荐。  
选择跟程序员约会的10个理由  唠叨/选择跟程序员约会的10个理由.html
亲，还有什么办法能提升系统的性能？一定还有招数可以用，这就是缓存和 CDN（内容分发网络）。你可以想象，九千万的访问量，有多少是在商品详情页面？访问这个页面的时候，数据全都是只读的（全部从数据库里面读  
搜狗输入法智慧版 - 新增情景感知，文思泉涌，妙笔生花，搜狗卷轴四大功能亮点，你也一起来下载吧，畅享更完美的打字输入体验。享受输入，从搜狗开始！    
//  //    
#百度技术沙龙#大家期待已久的百度赵岷的分享<推荐引擎实践：策略篇_赵岷.pdf>，讲稿下载地址：  内容涵盖：在推荐引擎的策略设计中，需要考虑哪些因素？用户需求和产品设计对算法的选择有什么影响？推荐需要什么样的数据？如何评估推荐效果？
伟大工程师从未说过的9句话  
帮助文档首页/云数据库  %
积淀智慧造就卓越 打造一客一市场  
Google Open Sources Supersonic Query Engine  
帮助创业者了解创业融资策略，两位年轻人推出创业指导搜索What Would PG（Paul Graham） Do?  
LinkedIn改版个人主页：简化编辑工具  
Google Throws Open Doors to Its Top-Secret Data Center  
分布式计算长文  
//   //   
关注lambdaRank时看到这个网页 ，实现了MART，RankNet， RankBoost，AdaRank，Coordinate Ascent，LambdaMART，大家可以看看。
如何设计一款高性能的队列通知应用  
探索推荐引擎内部的秘密，第 3 部分: 深入推荐引擎相关算法  
哈哈，bug太多
抱歉，此 不适宜对外公开。如需帮助，请联系客服。 
【摩羯座】苏珊米勒 2012年10月运势（完成） -   土星你终于舍得离开拉～～  
个性化互联网才是未来，Gravity为用户提供实时的个性化阅读体验  
Deconstructing Recommender Systems  
java-statistical-analysis-tool  
sexy。。。//数据科学家：21世纪最性感的职业  
漫漫长假，玩啥好呢？？
互联网网站的反爬虫策略浅析  
Facebook的实时Hadoop系统  
社区热议淘宝开源的优化定制JVM版本：Tabao JVM  
jsoup 1.7.1 发布，解析速度提升 2.3 倍  
服务器日志网站分析的原理及优缺点  
Scala的虚无与飘渺  
利用贝叶斯公式提高谎话的可信性  
期待惊喜 
亲，你喜欢把心仪的商品链接分享给你的朋友，你知道小小的分享会有大大的回报吗？ 关注  并 就有机会赢得四天三晚双人港澳双人游， 就送微积分，可兑换各种精美礼品、优惠券或充值支付宝噢！各位亲双节快乐，天天都有好心情！  
正在买 //  //    
《大数据：互联网大规模数据挖掘与分布式处理》大数据时代的及时雨 全球著名数据库技术专家最新力作   我在 分享了一个很不错的文件："大数据：互联网大规模数据挖掘与分布式处理.pdf"，快来看看吧~  
  
下图简单总结了实时竞价广告系统的 6 个概念，详细的介绍可以参看 “Real-time Bidding (RTB) – How Is It Changing Online Advertising in the Japanese Market”  
学习
也许是KDD2012上最受好评的讲座了：Key Lessons Learned Building Recommender Systems For Large-scale Social Networks，Christian Posse，LinkedIn 首席数据科学家。 
 //  好书推荐
《大数据：互联网大规模数据挖掘与分布式处理》已经上市，chinapub上已经可以下单 。。
谷歌利用GPS实现在全球范围信息同步  
同忽悠//怎么用“The hard problem" 来忽悠程序员  
读《一位资深程序员对当前软件行业的技术感悟》有感 : 黑客与画家  
约会对象也能“个性化推荐”，Grouper通过Facebook个人数据组织“三对三”线下约会  
//   
  google spanner 仰望吧  
//    
为什么转置一个512x512的矩阵，会比513x513的矩阵慢很多？【全文】 
开源分布式存储系统katta 介绍  
//    
google spanner（下一代大規模存儲）論文放出來了，就是數天前我看到拍臺大叫作弊的那篇  
Blur解决的最大的技术问题/功能： 整个数据集的快速大规模索引 自动分片Server故障转移 通过Lucene的NRT实现近实时更新的兼容性 Lucene的FDT的文件压缩，同时保持随机存取性能 Lucene的的WAL（预写日志）提供数据的可靠性 Lucene直接R/W到HDFS中（seek写的问题） Lucene的目...  
来自复杂系统故障的十八条经验 - 复杂系统运转时总是处于降级模式。 由上一条可知，运转中的复杂系统总是残缺不全。之所以还能运转，是因为系统内备有充足的冗余部件，即便存在诸多缺陷，人们仍然有办法让它工作。从以往的事故评估结果来看，...    （分享自  
//  分析的不错//  值得一读
【钓鱼岛假象一直在忽悠你】其实钓鱼岛这个事件，表面看起来是日本皮痒了。实际上整件事前前后后，都有着人为掩盖过的痕迹。只是大家只关注国恨家仇，没有心情去仔细思考罢了……【全文】 
「将个性化阅读进行到底：乐知隆重推出iOS版」—随着3G时代的到来，手机阅读已经成为不可或缺的一部分。乐知作为国内不多的个性化阅读器的后起之秀，最近隆重推出了iPhone版，正式宣布进军iOS的世界。iOS版不仅延续了...    （分享自  
"""
}