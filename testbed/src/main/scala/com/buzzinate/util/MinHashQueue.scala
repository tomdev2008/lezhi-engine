package com.buzzinate.util

import java.util.TreeMap
import collection.JavaConverters._

class MinHashQueue[E](max: Int) {
  val mhq = new TreeMap[Int, E]
  
  def add(hash: Int, e: E): Unit = {
    mhq.put(hash, e)
    if (mhq.size > max) mhq.pollLastEntry()
  }
  
  def values(): List[E] = {
    mhq.values.asScala.toList
  }
  
  def size(): Int = mhq.size
}

object MinHashQueue {
  def main(args: Array[String]): Unit = {
    val mhq = new MinHashQueue[Int](3)
    for ((v, i) <- List(5, 4, 4, 4, 3, 2, 2).zipWithIndex) {
      mhq.add(v, i)
    }
    println(mhq.values)
  }
}