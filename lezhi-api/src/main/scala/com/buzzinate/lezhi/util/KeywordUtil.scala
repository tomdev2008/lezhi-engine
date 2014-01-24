package com.buzzinate.lezhi.util

import collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import com.buzzinate.model.KeywordInfo
import org.ansj.splitWord.Analysis
import org.ansj.splitWord.analysis.ToAnalysis
import java.io.StringReader
import org.ansj.domain.Term
import java.util.ArrayList
import org.ansj.util.recognition.NatureRecognition
import com.buzzinate.nlp.util.TextUtil
import org.apache.commons.lang.StringUtils
import org.ansj.splitWord.Segment
import org.ansj.library.TwoWordLibrary
import java.text.SimpleDateFormat
import java.util.Date
import com.buzzinate.nlp.util.DictUtil

object KeywordUtil {
  val te = new com.buzzinate.nlp.util.TitleExtractor

  def main(args: Array[String]): Unit = {
    println(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(1355110483043L)));
    println("refine ...")
    println(refineKeywords("徐智苑多大了 - 中国广告知道网"))
    println(refineKeywords("索尼st25开不了机怎样办 - 中国广告知道网"))
    println(refineKeywords("盘点明星怪癖秘闻 张柏芝祼睡罗志祥坐着尿尿(10)_娱乐频道_光明网"))
    println(refineKeywords("狄波拉大赞张柏芝好媳妇 “锋芝”复合自己决定- Micro Reading"))

    println("extract ...")
    println(extractKeywords("盘点明星怪癖秘闻 张柏芝祼睡罗志祥坐着尿尿(10)_娱乐频道_光明网", List()))
    println(extractKeywords("金融时报：中国银行不满苛刻规定撤离伦敦_外媒_热点事件_四月网", List()))
    println(extractKeywords("狄波拉大赞张柏芝好媳妇 “锋芝”复合自己决定- Micro Reading", List()))
    println(extractKeywords("爱情就是两个人一起犯贱-- 犯贱吧", List()))
    println(extractKeywords("评论：Apple会是下一个SONY吗？", List()))
    println(extractKeywords("如是我闻", List()))
    println(extractKeywords("中国：剩女的困境 - 萝卜网", List()))
    
    println("dictidf ...")
    List("英国", "伦敦", "英伦敦", "在伦敦", "纯法系", "谢霆锋", "过程中", "上市", "苹果", "的信息", "appl") foreach { word =>
      println(word + " ==> " + dictidf(word))
    }
  }
  
  def refineKeywords(rawTitle: String): List[KeywordInfo] = {
    val title = te.extract(rawTitle)
    Segment.split(title).asScala.toList filter { term =>
      val usefulpos = term.getNatrue.natureStr.startsWith("n") && term.getNatrue.natureStr != null || term.getNatrue == "userDefine"
      usefulpos && term.getName.length >= 2
    } map { term =>
      KeywordInfo(term.getName, 1, 2)
    }
  }

  def extractKeywords(rawTitle: String, keywords: List[KeywordInfo]): List[KeywordInfo] = {
    val keywordset = keywords.map(kw => kw.word).toSet
    val stemTitle = TextUtil.stemAll(StringUtils.trim(te.extract(rawTitle))).toLowerCase
    keywords ++ getNounKeywords(stemTitle).filterNot(w2f => keywordset.contains(w2f._1)).map(w2f => KeywordInfo(w2f._1, w2f._2, KeywordInfo.TITLE))
  }

  def getNounKeywords(title: String): Map[String, Int] = {
    val wordList = new ListBuffer[String]
    
    var prevTerm: Term = null
    var prevUseful = false
    Segment.split(title).asScala.toList foreach { term =>
      val word = term.getName
      val isUsefulPos = !DictUtil.isUseless(term)
      if (isUsefulPos) {
        if (word.length > 1) wordList += word
        if (word.length == 1 && prevUseful) wordList += (prevTerm.getName + word)
      }
      prevTerm = term
      prevUseful = isUsefulPos
    }
    
    wordList.result.groupBy(word => word).map{w2l => 
      val (word, list) = w2l
      word -> list.size
    }
  }

  // calculate the idf from ansj dictionary
  def dictidf(word: String): Double = DictUtil.splitIdf(word)
}