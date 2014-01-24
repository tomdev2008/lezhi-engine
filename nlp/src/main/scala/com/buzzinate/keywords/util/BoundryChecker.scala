package com.buzzinate.keywords.util

import org.ansj.splitWord.Segment
import collection.JavaConverters._
import com.buzzinate.nlp.util.TextUtil
import org.ansj.splitWord.analysis.ToAnalysis

class BoundryChecker(text: String) {
  private val filledText = TextUtil.fillText(text)
  private val validOffsets = Array.fill(filledText.length + 1)(false)
  validOffsets(0) = true
  Segment.splitRaw(filledText, ToAnalysis.USE_USER_DEFINE).asScala foreach { term => 
    validOffsets(term.getOffe + term.getName.length) = true 
  }
  
  def valid(word: String): Boolean = {
    val fillWord = TextUtil.fillWord(word)
    val idx = filledText.indexOf(fillWord)
    idx >= 0 && validOffsets(idx) && validOffsets(idx + fillWord.length)
  }
}

object BoundryChecker {
  def main(args: Array[String]): Unit = {
//    val title = "咸阳电信"
//    val bc = new BoundryChecker(title)
//    println(bc.valid("咸阳电"))
//    println(bc.valid("咸阳"))
//    println(bc.valid("电信"))
    
    val title1 = "三星i9100真伪查询"
    val bc1 = new BoundryChecker(title1)
    println(bc1.valid("i9100"))
  }
}