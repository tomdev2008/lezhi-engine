package com.buzzinate.keywords.title

import scala.collection.JavaConverters._
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import com.buzzinate.nlp.util.PorterStemmer
import org.ansj.splitWord.Segment
import com.buzzinate.nlp.util.WordUtil
import com.buzzinate.nlp.util.DictUtil

class NounExtractor extends RawExtractor {
  def extract(rawWordCandidates: List[WordCandidate], title: String, snippets: List[String]): List[WordCandidate] = {
    val wclist = new ListBuffer[WordCandidate]
    Segment.split(title).asScala foreach { term =>
      if (term.getNatrue.natureStr == "en"){
        if (term.getName.size >= 3 && !WordUtil.isKnownWord(term.getName)) wclist += WordCandidate(NounExtractor.stemmer.stem(term.getName).toLowerCase, -1)
      } else {
        if (NounExtractor.isNounAdj(term.getNatrue.natureStr) && term.getName.size >= 2) wclist += WordCandidate(term.getName, -1)
      }
    }
    rawWordCandidates ++ wclist.result
  }
}

object NounExtractor {
  val stemmer = new PorterStemmer
  
  def isNounAdj(nature: String) = nature.startsWith("n") || nature.startsWith("a") || nature.equals("en") || nature == "userDefine"
  
  def main(args: Array[String]): Unit = {
    val ne = new NounExtractor
    val wc = ne.extract(List(), "[视频] 咖啡解说:新CW龙鹰上阵纯法系爆发", List())
    println(wc)
    println(Segment.split("[视频] 咖啡解说:新CW龙鹰上阵纯法系爆发"))
  }
}