package com.buzzinate.model

import org.apache.commons.lang.StringUtils

object DelimitedKeywordText {
  // Format keywords to <word>|<freq>,<field> <word2>|<freq2>,<field2>
  def toText(kis: List[KeywordInfo]): String = {
    kis.filterNot(ki => ki.word.indexOf('|') >= 0).map { ki =>
      String.format("%s|%s,%s", StringUtils.replace(ki.word, " ", "_"), ki.freq.toString, ki.field.toString)
    }.mkString(" ")
  }
  
  def toIdfText(kis: List[KeywordInfo])(idf: String => Double): String = {
    kis.filterNot(ki => ki.word.indexOf('|') >= 0).map { ki =>
      String.format("%s|%s,%s,%s", StringUtils.replace(ki.word, " ", "_"), ki.freq.toString, ki.field.toString, idf(ki.word).toString)
    }.mkString(" ")
  }
  
  // Change <word>|<freq>,<field> <word2>|<freq2>,<field2>
  // to <word>|<freq>,<field>,<idf> <word2>|<freq2>,<field2>,<idf2>
  def toIdfText(text: String)(idf: String => Double): String = {
     StringUtils.split(text, " ").map { kw =>
       kw + "," + idf(StringUtils.replace(StringUtils.substringBefore(kw, "|"), "_", " "))
     }.mkString(" ")
  }
}