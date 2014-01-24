package com.buzzinate.keywords

import collection.JavaConverters._
import com.buzzinate.keywords.title.NounExtractor
import scala.collection.mutable.HashSet
import com.buzzinate.keywords.title.QuotedWordExtractor
import scala.collection.mutable.ListBuffer
import org.arabidopsis.ahocorasick.WordFreqTree
import com.buzzinate.keywords.util.HashMapUtil
import scala.collection.mutable.HashMap
import com.buzzinate.nlp.util.TextUtil
import org.apache.commons.lang.StringUtils
import java.util.regex.Pattern
import com.buzzinate.keywords.title.EntropyExtractor
import com.buzzinate.keywords.title.WordCandidate
import org.jsoup.Jsoup
import com.buzzinate.keywords.util.ExtractUtil
import com.buzzinate.keywords.util.SnippetBlock
import com.buzzinate.nlp.segment.TextProcess
import com.buzzinate.keywords.content.DlgExtractor
import com.buzzinate.keywords.util.BoundryChecker
import com.buzzinate.nlp.util.DictUtil

object LezhiKeywordsExtractor {
  val CONTENT = 0
  val META = 1
  val TITLE = 2
  val META_TITLE = 3
  
  val urlReg = Pattern.compile("http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?", Pattern.CASE_INSENSITIVE)
  val rawExtractors = List(new NounExtractor, new EntropyExtractor, new QuotedWordExtractor)
  
  def extract(url: String, html: String): List[Keyword] = {
    val doc = Jsoup.parse(html, url)
    val rawTitle = Jsoup.parse(doc.title).text()
    ExtractUtil.cleanup(doc.body)
    val title = ExtractUtil.extractTitle(doc.body, rawTitle)
    val metaKeywords = ExtractUtil.extractMeta(doc, "keywords")
    
    val blocks = ExtractUtil.extractBlocks(doc, title) map { block =>
      SnippetBlock(block.snippets map { snippet => TextProcess.normalize(urlReg.matcher(snippet).replaceAll(""))}, block.score, block.isArticle, block.imgs)
    }
    
    val normalTitle = TextUtil.fillText(title)
    val normalRawTitle = TextUtil.fillText(doc.title)
    
    val wordset = new HashSet[String]
    wordset ++= DlgExtractor.extract(normalTitle, TextUtil.fillText(doc.title), blocks, 6).map { case (word, _) => word }
    
    val allsnippets = blocks.filter(_.isArticle).flatMap { b => b.snippets }
    var wordCandidates = List[WordCandidate]()
    rawExtractors foreach { re =>
      wordCandidates = re.extract(wordCandidates, title, allsnippets)
    }
    
    val bc = new BoundryChecker(title)
    wordCandidates foreach { wc =>
      if (bc.valid(wc.word)) wordset += wc.word 
    }
    
    for (mw <- splitMetaKeywords(List(StringUtils.replace(metaKeywords, title, "")))) {
      val kw = TextUtil.stemAll(StringUtils.trim(mw)).toLowerCase
      wordset += kw
    }
    
    val wordcnt = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    val wft = new WordFreqTree
    wordset.foreach { word => wft.add(word) }
    wft.build
    
    for (snippet <- List(title) ++ allsnippets) {
      val ws = wft.search(snippet)
      for (w <- ws.asScala) {
        wordcnt.adjustOrPut(w, 1, 1)
      }
    }
    
    val titleWords = wft.search(title).asScala.toSet
    val metaWords = wft.search(metaKeywords).asScala.toSet
    
    var nMeta = metaWords.size
    var nUsefulMetas = 0
    metaWords foreach { mw =>
      if (wordcnt.contains(mw)) nUsefulMetas += 1
    }
    if (nMeta < 10 || nUsefulMetas * 3 > nMeta) {
      metaWords foreach { mw => if (!wordcnt.contains(mw)) wordcnt.put(mw, 0) }
    }
    
    wordcnt.toList.map { case (word, freq) =>
      var field = CONTENT
      if (titleWords.contains(word) && metaWords.contains(word)) field = META_TITLE
      else if (titleWords.contains(word)) field = TITLE
      else if (metaWords.contains(word)) field = META
      Keyword(word, field, freq, 0d)
    }.filterNot(kw => DictUtil.isStop(kw.word))
  }
  
  def hasLetter(word: String) = word.exists(ch => Character.isLetter(ch))
  
  def splitMetaKeywords(metaKeywords: List[String]): List[String] = {
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
}