package com.buzzinate.charset

import scala.collection.mutable.HashTable
import scala.collection.mutable.DefaultEntry
import scala.collection.mutable.HashMap

class MultiCharsetDetector {
  val charset2super = Map("gb2312" -> "gb18030", "gbk" -> "gb18030")
  val charset2p = Map("gb18030" -> 2, "UTF-8" -> 2, "gbk" -> 1, "gb2312" -> 1)
  val charsetCnt = new HashMap[String, Double] with DoubleHashMap[String]
  
  def addCharset(charset: String, weight: Double = 1): Unit = {
    if (charset != null) {
      charsetCnt.adjustOrPut(charset.toLowerCase, weight, weight)
      charset2super.get(charset.toLowerCase).map{ sc => 
        charsetCnt.adjustOrPut(sc, weight, weight)
      }
    }
  }
  
  def getCharset(): String = {
    var max = 0d
    var finalCharset = ""
    for ((charset, cnt) <- charsetCnt) {
      val score = cnt * 5 + charset2p.getOrElse(charset, 0)
      if (score > max) {
        max = score
        finalCharset = charset
      }
    }
//    println("Charset Counter: " + charsetCnt)
    finalCharset
  }
}

trait DoubleHashMap[A] extends HashTable[A, DefaultEntry[A, Double]] {
  def adjustOrPut(key: A, incr: Double, value: Double): Double = {
    val e = findEntry(key)
    if (e == null) {
      addEntry(new DefaultEntry[A, Double](key, value))
      0
    } else {
      val old = e.value
      e.value += incr
      old
    }
  }
}