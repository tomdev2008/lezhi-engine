package com.buzzinate.dm.util

import java.util.Arrays
import collection.JavaConverters._

object TopItems {
  def top[E](elems: Iterable[E], max: Int)(f: E => Double)(implicit me: Manifest[E]): Array[E] = {
    val pq = new DoublePriorityQueue[E](max)
    elems foreach { e =>
      pq.add(f(e), e)
    }
    pq.values.asScala.toArray
  }
  
  def main(args: Array[String]): Unit = {
    val l = List("a" -> 4, "b" -> 9, "c" -> 5, "d" -> 1, "e" -> 3, "f" -> 9)
    println(top(l, 2){x => x._2}.toList)
  }
}