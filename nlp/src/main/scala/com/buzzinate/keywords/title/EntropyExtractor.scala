package com.buzzinate.keywords.title

import scala.collection.immutable.List
import scala.collection.mutable.HashMap
import org.apache.commons.lang.StringUtils
import com.abahgat.suffixtree.GeneralizedSuffixTree
import com.buzzinate.keywords.util.HashMapUtil
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashSet

import com.buzzinate.nlp.segment.Atom.AtomType;
import com.buzzinate.nlp.util.TextUtil
import org.ansj.splitWord.Segment
import collection.JavaConverters._
import com.buzzinate.nlp.segment.AtomSplit
import com.buzzinate.nlp.segment.Atom

case class WordSnippet(words: List[String])
case class WordWeight(word: String, freq: Int, mi: Double, leftEntropy: Double, rightEntropy: Double)

class EntropyExtractor(minEntropy: Double = 0.5d) extends RawExtractor {
  import EntropyExtractor._
  
  def extract(rawWordCandidates: List[WordCandidate], title: String, snippets: List[String]): List[WordCandidate] = {
    val includeTitle = snippets.exists(snippet => snippet.contains(title))
    val texts = if (includeTitle) snippets else List(title) ++ snippets
    val result = extract(rawWordCandidates, title, texts, 0)
    val filledTitle = TextUtil.fillText(title)
    result.filter(wc => wc.word.trim.size > 0).filterNot { wc => 
      val filledWord = TextUtil.fillWord(wc.word)
      wc.word.length > 5 || StringUtils.countMatches(wc.word, " ") > 1 || filledTitle.indexOf(filledWord) == -1
    }
  }
  
  def extract(rawWordCandidates: List[WordCandidate], title: String, snippets: List[String], depth: Int): List[WordCandidate] = {
    val sf = new GeneralizedSuffixTree
    for (ws <- split(title)) {
      sf.put(ws.words.mkString)
    }
   
    val extwordcnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    val wordcnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    val invalidWords = new HashSet[String]()
    for (orgsnippet <- snippets) {
      val snippet = orgsnippet.toLowerCase
      for (i <- 0 until snippet.length) {
        val sub = sf.searchPrefix(snippet, i)
        val idx = title.indexOf(sub)
        if (sub.size > 0) wordcnt.adjustOrPut(sub.substring(0, 1), 1, 1)
        var j = 2
        while (j <= sub.size) {
          wordcnt.adjustOrPut(sub.substring(0, j), 1, 1)
          val word = sub.substring(0, j)
          extwordcnt.adjustOrPut(word, 1, 1)
          if (i > 0) extwordcnt.adjustOrPut(word + "<" + formatchar(snippet.charAt(i), snippet.charAt(i-1)), 1, 1)
          else extwordcnt.adjustOrPut(word + "<$", 1, 1)
          val to = i + j
          if (to < snippet.length) extwordcnt.adjustOrPut(word + ">" + formatchar(snippet.charAt(to-1), snippet.charAt(to)), 1, 1)
          else extwordcnt.adjustOrPut(word + ">$", 1, 1)
          j += 1
        }
      }
    }
    
    val result = new ListBuffer[WordCandidate]
    
    var prevWord = ""
    val leftfreqs = new ListBuffer[Int]
    val rightfreqs = new ListBuffer[Int]
    for ((word, cnt) <- extwordcnt.toList.sortBy(wc => substringBeforeAny(wc._1, "<>"))) {
      val w = substringBeforeAny(word, "<>")
      
      if (w != prevWord && isUseful(prevWord)) {
        val freq = wordcnt(prevWord)
        val ww = WordWeight(prevWord, freq, calcMI(prevWord, wordcnt), calcEntropy(leftfreqs), calcEntropy(rightfreqs))
        if (ww.leftEntropy >= 0.5d && ww.rightEntropy >= 0.5d && ww.mi >= 0.45d) result += WordCandidate(ww.word, ww.freq)
        else if (ww.freq >= 3) invalidWords += ww.word
        leftfreqs.clear
        rightfreqs.clear
      }
      if (word.contains("<$")) for (k <- 0 until cnt) leftfreqs += 1
      else if (word.contains("<")) leftfreqs += cnt
      if (word.contains(">$")) for (k <- 0 until cnt) rightfreqs += 1
      else if (word.contains(">")) rightfreqs += cnt
      prevWord = w
    }
    if (prevWord != "") {
      val freq = wordcnt(prevWord)
      val ww = WordWeight(prevWord, freq, calcMI(prevWord, wordcnt), calcEntropy(leftfreqs), calcEntropy(rightfreqs))
      if (ww.leftEntropy >= 0.5d && ww.rightEntropy >= 0.5d && ww.mi >= 0.45d) result += WordCandidate(ww.word, ww.freq)
      else if (ww.freq >= 3) invalidWords += ww.word
    }
    
    if (depth < 1) {
      val freqWords = new ListBuffer[String]
      result map { wc => wc.word } sortBy { word => word.length} foreach { word =>
        if (!freqWords.exists(fw => word.contains(fw)) && word.trim.size > 0) freqWords += word
      }
      var modifyTitle = TextUtil.fillText(title)
      freqWords foreach { fw => modifyTitle = StringUtils.replace(modifyTitle, TextUtil.fillWord(fw), ", ") }
      result ++= extract(List(), modifyTitle, snippets ++ List(modifyTitle), 1)
    }
    
    result.result ++ rawWordCandidates.filterNot(wc => invalidWords.contains(wc.word))
  }
}

object EntropyExtractor {
  
  private def split(title: String): List[WordSnippet] = {
    AtomSplit.splitSnippets(title).asScala.toList map { snippet =>
      WordSnippet(AtomSplit.split0(snippet).asScala.toList)
    }
  }
  
  private def isUseful(word: String): Boolean = {
    if (word.trim.length == 0) false
    else {
      var validSpace = true
      var prevAscii = false
      for (i <- 0 until word.length) {
        val ch = word.charAt(i)
        if (Character.isWhitespace(ch) && !prevAscii) validSpace = false
        prevAscii = TextUtil.isAscii(ch)
      }
      validSpace && Character.isLetterOrDigit(word.charAt(0)) && Character.isLetterOrDigit(word.charAt(word.length - 1))
    }
  }
  
  private def formatchar(neighbor: Char, ch: Char) = if (!Character.isDigit(neighbor) && Character.isDigit(ch)) '$' else ch
  
  private def calcMI(word: String, wordcnt: HashMap[String, Int]): Double = {
    val f = wordcnt.getOrElse(word, 0).toDouble
    val lf = wordcnt.getOrElse(word.substring(0, word.size-1), 0)
    val rf = wordcnt.getOrElse(word.substring(1), 0)
    if (word.size > 2) f / (lf + rf - f)
    else math.max(f / lf, f / rf)
  }
  
  private def calcEntropy(freqs: Iterable[Int]): Double = {
    val sumFreq = freqs.sum.toDouble
    var e = 0d
    for (freq <- freqs) {
      val p = freq / sumFreq
      e += - p * math.log(p)
    }
    e
  }
  
  private def substringBeforeAny(str: String, separators: String): String = {
    val pos = StringUtils.indexOfAny(str, separators)
    if (pos == StringUtils.INDEX_NOT_FOUND) str else str.substring(0, pos)
  }
  
  def main(args: Array[String]): Unit = {
    for (s <- split("美版 iphon4 晋级到 5.0.1 后 sim 卡无法辨认 跪求高手指条明路.... ")) println(s)
    for (s <- split("定义在R上的函数f(x)满足f(x)=log2(1-x),x小于等于0 f(x-1)-f(x-2),x>0 则F（2013）＝")) println(s)
    for (s <- split("360百度暗战：360导航问答被连夜替换为奇虎")) println(s)
    println(Segment.split("陆毅微博晒三岁女儿萝莉照片, LinkedIn Engineering发布SenseiDB 1.0.0, 程序员不是一般的人, Delicious变得越来越不好用了, Google发布“知识图谱”：为用户提供有完整知识体系的搜索结果, 应用构建，快照和隔离体, 品牌应该如何应对变身“搜索达人”的消费者？"))
    println(Segment.split("陆毅微博晒三岁女儿萝莉照片"))
    val snippets = split("陆毅微博晒三岁女儿萝莉照片")
    for (s <- snippets) println(s)
  }
}