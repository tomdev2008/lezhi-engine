package com.buzzinate.keywords.title

import scala.collection.immutable.List
import java.util.regex.Pattern
import scala.collection.mutable.HashSet
import org.apache.commons.lang.StringUtils

class QuotedWordExtractor extends RawExtractor {
  def extract(rawWordCandidates: List[WordCandidate], title: String, snippets: List[String]): List[WordCandidate] = {
    val wordset = new HashSet[String]
    for (pat <- QuotedWordExtractor.pats) {
      val matcher = pat.matcher(title)
      while (matcher.find) {
        val phrase = matcher.group(1)
        wordset += phrase
        wordset += substringBeforeAny(phrase, ":：")
        wordset += substringBeforeAny(phrase, "0123456789")
        wordset += substringBeforeAny(phrase, "II")
      }
    }
    
    rawWordCandidates ++ wordset.toList.filter(word => word.size >= 2).map { word =>
      WordCandidate(word, -1)
    }
  }
  
  def substringBeforeAny(str: String, separators: String): String = {
    val pos = StringUtils.indexOfAny(str, separators)
    if (pos == StringUtils.INDEX_NOT_FOUND) str else str.substring(0, pos)
  }
}

object QuotedWordExtractor {
  val pats = List(Pattern.compile("《([^《^》]*)》"), Pattern.compile("#([^#]*)#") , Pattern.compile("【([^【^】]*)】"), Pattern.compile("“([^“^”]*)”"))
  
  def main(args: Array[String]): Unit = {
    val text = "《行星边际2》试玩视频 陆空战斗让人眼花缭乱"
    println(new QuotedWordExtractor().extract(text, List()))
  }
}