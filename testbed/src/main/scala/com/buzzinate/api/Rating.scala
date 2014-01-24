package com.buzzinate.api

import java.io.IOException

case class Rating (userId: Int, itemId: Int, rate: Int, timestamp: Long)

object Rating {
  def parse(line: String): Rating = {
    line.split("::").toList match {
      case uid::mid::r::ts::Nil => Rating(uid.toInt, mid.toInt, r.toInt, ts.toLong)
      case _ => throw new IOException("Not a valid line " + line)
    }
  }
  
  def main(args: Array[String]): Unit = {
    println(Rating.parse("3858::1952::4::965872975"))
  }
}