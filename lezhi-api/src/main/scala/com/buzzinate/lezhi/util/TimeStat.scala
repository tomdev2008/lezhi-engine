package com.buzzinate.lezhi.util

import scala.collection.mutable.HashMap

class TimeStat {
  var start = System.currentTimeMillis
  val stats = new HashMap[String, Long]
  
  def begin(): Unit = {
    start = System.currentTimeMillis
  }
  
  def end(name: String): Unit = {
    val now = System.currentTimeMillis
    stats += name -> (now - start)
    start = now
  }
  
  def print(): Unit = {
    println("timestat: " + stats.map(nt => nt._1 + "=" + nt._2).toList.sortBy(x => x).mkString(", "))
  }
}