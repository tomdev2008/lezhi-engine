package com.buzzinate.lezhi.util

import org.apache.commons.lang.StringUtils
import scala.collection.mutable.HashMap
import scala.Array.canBuildFrom
import scala.collection.mutable.HashSet
import com.buzzinate.nlp.util.TextUtil

class TitleExtractor(split: String, splitCnt: Int, total: Int, partCnt: HashMap[String, Int], npart: Int) {
  private val minCnt = if (total < 4) total else total * 8 / 10
  
  def extract(title: String): String = {
    if ((splitCnt >= minCnt || isSplit(split)) && !title.trim().isEmpty()) {
      var parts = StringUtils.splitByWholeSeparator(title, split)
      while (parts.length > npart && parts.length >= 2) {
        parts = TitleExtractor.mergeParts(parts, split) { p =>
          val commaCnt = StringUtils.countMatches(p, ",")
          math.log(1 + partCnt.getOrElse(p, 0)) * (1 + commaCnt) / (1 + p.length)
        }
      }
      val (part, _) = parts.map { p => 
        val commaCnt = StringUtils.countMatches(p, ",")
        p -> math.log(1 + partCnt.getOrElse(p, 0)) * (1 + commaCnt) / (1 + p.length)
      }.minBy(_._2)
      part.trim
    } else title
  }
  
  def isSplit(str: String): Boolean = {
    str.contains("-") && str.length >= 2
  }
}

object TitleExtractor {
  val newlinechars = Set('\r', '\n')
  
  def prepare(rawTitles: List[String]): TitleExtractor = {
    val splitCnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    splitCnt.put("--", 0)
    for (title <- rawTitles) {
      var prev = ""
      for (ch <- title) {
        if (!isTitlePart(ch) && !newlinechars.contains(ch)) {
          if (prev.length > 0) splitCnt.adjustOrPut(prev + ch, 1, 1)
          splitCnt.adjustOrPut(ch.toString, 1, 1)
          prev = ch.toString
        } else prev = ""
      }
    }
    
    val (split, cnt) = splitCnt.filter{ case (str, _) => str.trim.length > 0 }.maxBy { case (str, cnt) => cnt * str.length * str.length}
    val partcnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    val npartcnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    val total = rawTitles.size
    npartcnt.put(0, total)
    for (title <- rawTitles) {
      val parts = StringUtils.splitByWholeSeparator(title, split)
      for (i <- 0 until parts.length) partcnt.adjustOrPut(parts(i), 1, 1)
      for (i <- 1 to parts.length) npartcnt.adjustOrPut(i, 1, 1)
    }
    
    val (npart, _) = npartcnt.filter { case (npart, cnt) => cnt >= total * 8 / 10 || cnt * 2 >= total && npart <= 2 }.maxBy { case (npart, cnt) => npart }
    
    new TitleExtractor(split, cnt, total, partcnt, npart)
  }
  
  private def mergeParts(parts: Array[String], split: String)(sf: String => Double) = {
     var minsum = Double.MaxValue
     var minidx = 0
     var prevScore = 0d
     for (i <- 0 until parts.length) {
       val score = sf(parts(i))
       if (i > 0) {
         val sum = prevScore + score
         if (minsum > sum) {
           minsum = sum
           minidx = i
         }
         prevScore = score
       }
     }
     if (minidx > 0) {
       parts(minidx-1) = parts(minidx-1) + split + parts(minidx)
       for (i <- minidx until parts.length-1) parts(i) = parts(i+1)
       parts.slice(0, parts.length - 1)
     } else parts
  }
    
  private def isTitlePart(ch: Char): Boolean = {
    if (Character.isLetterOrDigit(ch) || ch == '：' || ch == ',' || ch == '？' || ch == '?' || ch == '!' || ch == '！') true
    else {
      val ct = Character.getType(ch)
      ct == Character.INITIAL_QUOTE_PUNCTUATION || ct == Character.START_PUNCTUATION || ct == Character.FINAL_QUOTE_PUNCTUATION || ct == Character.END_PUNCTUATION
    }
  }
  
  val titles1 = List(
    "纽约时报：中国退出世界金融会议向日施压_外媒_热点事件_四月网",
    "韩联社：朝鲜一年两次召开最高人民会议_外媒_热点事件_四月网",
    "纽约时报：是什么让中国人夜不能寐_外媒_焦点言论_四月网",
    "金融时报：中国对农村的投资很划算_外媒_热点事件_四月网",
    "纽约时报：安哥拉从中国得到什么？_外媒_热点事件_四月网",
    "纽约时报：中日钓鱼岛纠纷急剧升级_外媒_网贴翻译_四月网"
  )
  
  val titles2 = List(
    "警告，CCTV在此干活-- 犯贱吧",
    "春游-- 犯贱吧",
    "空洞-- 犯贱吧",
    "囧-- 犯贱吧",
    "空洞-- 犯贱吧"
  )
  
  val titles3 = List(
    "瑞士无缝内衣品牌随官方来华访问-商讯-商界在线",
    "定制家具 从爱维米格开始-商讯-商界在线",
    "爱维米格高端定制家具 着“手”之功为您创造生活典范-投资-商界在线",
    "瑞士银行前职员举报银行逃税获奖1亿美元-公司-商界在线",
    "品牌蒲公英战略-营销-商界在线"
  )
  
  val titles4 = List(
    "华尔街日报：没有IBM就没有今天的华为？_外媒_热点事件_四月网",
    "经济学人：谁在害怕华为？-外媒-焦点言论-四月网",
    "美国国会警告华为中兴 德网友称美国秀下限_外媒_网贴翻译_四月网",
    "华为中兴回击美报告 网友讽美方捕风捉影_外媒_网贴翻译_四月网",
    "反华派罗姆尼被曝与竟与华为关系密切_外媒_网贴翻译_四月网"
  )
  
  val titles5 = List(
    "邪恶漫画：毁童年的睡美女-- 低价门票",
    "美女专辑-那些你hold不住的美女-第541辑 - 低价门票-- 低价门票",
    "美女专辑-那些你hold不住的美女-第542辑 - 低价门票-- 低价门票",
    "美女专辑-那些你hold不住的美女-第543辑 - 低价门票-- 低价门票",
    "美女专辑-那些你hold不住的美女-第544辑 - 低价门票-- 低价门票",
    "美女专辑-那些你hold不住的美女-第545辑 - 低价门票-- 低价门票",
    "美女专辑-那些你hold不住的美女-第546辑 - 低价门票-- 低价门票",
    "美女专辑-那些你hold不住的美女-第547辑 - 低价门票-- 低价门票"
  )
  
  val titles6 = List(
    "不要轻易和少妇上床：金融危机是这样产生的 | 笑味集",
    "赤裸的职场-金融圈混子手记 | 笑味集",
    "古希腊众神开会应对金融危机 | 笑味集",
    "邪恶漫画之蚬子之家 | 笑味集",
    "【丧尸危机中国平民应对手册】 | 笑味集",
    "色系漫画梦龙Y传第八集之危机中的李梦龙 | 笑味集"
  )
  
  val titles7 = List(
      "铜陵县纪委调查高联村腐败，究竟要查到“猴年马月”？-安徽论坛 - 中国网互动中心",
      "【实名举报】：安徽省铜陵县高联村干部犯罪集团犯罪事..-民生曝光台 - 中国网互动中心",
      "铜陵县纪委调查高联村腐败，究竟要查到“猴年马月”？-福建论坛 - 中国网互动中心",
      "铜陵县高联村封建统治下的罪恶！-浙江论坛 - 中国网互动中心",
      "铜陵县纪委调查高联村腐败，究竟要查到“猴年马月”？-江西论坛 - 中国网互动中心",
      "【金余论谈】投资做到这些，想不赚钱都难12345-家庭理财 - 中国网互动中心",
      "中国坦克全家福-军事杂谈 - 中国网互动中心",
      "北京.2008.奥林匹克.梦开始的地方-北京论坛 - 中国网互动中心",
      "男人让女人心动的瞬间-情感空间 - 中国网互动中心",
      "北京.2008.奥林匹克.梦开始的地方-北京论坛 - 中国网互动中心",
      "2012香港《一路向西》【高清/】高清全集-影视观澜 - 中国网互动中心",
      "桂林公安交警有法不依、执法不公、渎职包屁、滥用职权-消费曝光台 - 中国网互动中心",
      "铜陵县高联村封建统治下的罪恶！-福建论坛 - 中国网互动中心",
      "北京.2008.奥林匹克.梦开始的地方-北京论坛 - 中国网互动中心",
      "铜陵县高联村封建统治下的罪恶！-安徽论坛 - 中国网互动中心",
      "青岛日本留学-上海论坛 - 中国网互动中心"
  )
  
  val titles8 = List(
      "女教授与男博士生情，谁说不可以？-博士生,教授,畸恋,女演员,厦大,搜索-中国宁波网-新闻中心",
      "香港四所大学入选世界百强 香港大学排名第23位-大学定位,世界大学排名,香港高校,逸夫书院,香港科技大学,香港城市大学-中国宁波网-新闻中心",
      "大学教授称20年内解决异地高考都是乐观估计-异地,高考报名,高考移民,北京教育考试院,北京市教委,升学考试-中国宁波网-新闻中心",
      "警察学院教授演示头枕钢杆击车窗逃生可行(图)-车窗玻璃,教授,头枕砸窗-中国宁波网-新闻中心",
      "[第一追问]：从“大学成本计算公式”中得出什么-读大学,大学新生,成本计算-中国宁波网-新闻中心"
  )
  
  val titles9 = List(
      "中新网社区",
      "中新网社区 - 用急救车拉水果进京送礼，哪家医院这么有心？(图)",
      "【纪实摄影】皮包骨社区的女孩们 看完还想减肥吗？-中新社区",
      "中新网社区 - 真要命！急救车送了5家医院找不到一张床位",
      "中新网社区 - 无人给救护车让道 其实是挡了自己未来的“生路”",
      "中新博客精选--“大便弟”刚走“撒尿弟”又现，美丽中国有多远？",
      "中新网社区 - 不怕了！印第安马雅族领导人澄清12月21日非世界末日"
  )
  
  val titles10 = List(
      "史上最“坑爹”的体检项目为何迟迟未取消？-舆情观察 - 中国网互动中心",
      "史上最“坑爹”的体检项目为何迟迟未取消-鹰眼求实 - 中国网互动中心",
      "史上最“坑爹”的体检项目为何迟迟未取消-天津论坛 - 中国网互动中心",
      "贵州安顺公考现“体检门” 头名考生血常规异常-安顺论坛",
      "史上最“坑爹”的体检项目为何迟迟未取消-百姓茶馆",
      "公务员体检报告“对公不对私”存在什么“猫腻”？-百姓茶馆"
  )
  
  val titles11 = List(
      "盘点姚晨大牌气质感穿衣经-服饰-瑞丽网|rayli.com.cn",
      "Victoria Beckham -一招变长腿！女星短外套拗型-服饰-瑞丽网|rayli.com.cn",
      "花枝招展牧人鸟升级再升级-服饰-瑞丽网|rayli.com.cn",
      "姚晨 -一招变长腿！女星短外套拗型-服饰-瑞丽网|rayli.com.cn",
      "探秘时尚女魔头家珍贵私藏物-服饰-瑞丽网|rayli.com.cn",
      "Jessica Alba -一招变长腿！女星短外套拗型-服饰-瑞丽网|rayli.com.cn",
      "Blake Lively -一招变长腿！女星短外套拗型-服饰-瑞丽网|rayli.com.cn",
      "女星巧搭手套轻松提升气场-服饰-瑞丽网|rayli.com.cn",
      "小贝儿子帅气代言Burberry -服饰-瑞丽网|rayli.com.cn",
      "Marie Mai -一招变长腿！女星短外套拗型-服饰-瑞丽网|rayli.com.cn",
      "Miranda Kerr -一招变长腿！女星短外套拗型-服饰-瑞丽网|rayli.com.cn"
  )
  
//  val titledataset = List(titles1, titles2, titles3, titles4, titles5, titles6, titles7, titles8, titles9, titles10, titles11)
  val titledataset = List(titles11)
  
  def main(args: Array[String]): Unit = {
    for (titles <- titledataset) {
      val te1 = prepare(titles)
      for (t <- titles) println(te1.extract(t))
      println("-----")
      val titles2 = titles map {t => te1.extract(t)}
      val te2 = prepare(titles2)
      for (t <- titles2) println(te2.extract(t))
      println()
    }
  }
}