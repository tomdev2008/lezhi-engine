package com.buzzinate.lezhi.crawl.batch

import com.buzzinate.keywords.Keyword
import com.buzzinate.model.KeywordInfo

object KeywordInfoUtil {
  def convert(keywords: List[Keyword]): List[KeywordInfo] = {
    keywords.map { kw => 
      KeywordInfo(kw.word, kw.freq, kw.field)
    }
  }
  
  def toMap(kis: List[KeywordInfo]): Map[String, String] = Map() ++ kis.map(ki => KeywordInfo.toKV(ki))
}