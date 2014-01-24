package com.buzzinate.util
import scala.collection.mutable.ListBuffer

object ListUtil {
  
  def splitList[A](list: List[A], maxSize : Int = 50): List[List[A]] = {
    if(list.size > maxSize){
      val splitsListBuffer = new ListBuffer[List[A]]
      val (listA, listB) = list.splitAt(list.size / 2)
      splitsListBuffer.appendAll(splitList(listA, maxSize))
      splitsListBuffer.appendAll(splitList(listB, maxSize))
      splitsListBuffer.toList
    } else {
      return List(list)
    }
  }

}