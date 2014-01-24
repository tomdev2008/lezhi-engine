package com.buzzinate.corpus.freq

import com.buzzinate.keywords.util.IO
import org.apache.commons.lang.StringUtils
import com.buzzinate.nlp.util.TextUtil
import com.buzzinate.doublearray.make.DoubleArrayMaker

object BuildFreq {
  def main(args: Array[String]): Unit = {
    buildArray
  }
  
  def buildArray(): Unit = {
    IO.loadFromFile("D:/data/wordfreqs/english.freq", "UTF-8") { lines =>
      val words = new java.util.HashSet[String]
      lines foreach { line =>
        val strs = line.split("\t")
        words.add(strs(0))
      }
      new DoubleArrayMaker().make(words, "english.freq.array");
    }
  }
    
  def buildFreq(): Unit = {
    IO.printToFile("D:/data/wordfreqs/english.freq") { p =>
      IO.loadFromFile("D:/data/wordfreqs/ANC-token-count.txt", "UTF-8") { lines =>
        lines foreach { line =>
          val strs = line.split("\t")
          if (strs.length >= 2) {
            val word = strs(0)
            val freq = strs(1)
            if (isWord(word) && freq.toLong >= 5) {
              val sw = TextUtil.stem(StringUtils.substringBefore(word, "_").toLowerCase)
//              println(sw + "\t" + freq)
              p.println(sw + "\t" + freq)
            }
          } else println(line)
        }
      }
    }
  }
  
  def isWord(word: String): Boolean = {
    var bw = true
    word.foreach { ch =>
      if (!Character.isLetter(ch) && ch != '\'') bw = false
    }
    bw
  }
}