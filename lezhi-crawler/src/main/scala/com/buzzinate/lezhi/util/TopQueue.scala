package com.buzzinate.lezhi.util

import scala.collection.mutable.PriorityQueue

case class Entry[K, V](key: K, value: V)

class EntryOrdering[K, V](implicit val ord: Ordering[K]) extends Ordering[Entry[K, V]] {
  override def compare(x: Entry[K, V], y: Entry[K, V]): Int = -ord.compare(x.key, y.key)
}

class TopQueue[K, V](len: Int)(implicit val kord: Ordering[K]) {
  implicit val ord = new EntryOrdering[K, V]
  val pq = new PriorityQueue[Entry[K, V]]
  
  def put(key: K, value: V): Unit = {
    pq += Entry(key, value)
    if (pq.size > len) pq.dequeue
  }
  
  def toValues(): List[V] = {
    pq.toList.sortBy(e => e.key)(kord.reverse).map(e => e.value)
  }
}

object TopQueue {
  def main(args: Array[String]): Unit = {
    val pq = new TopQueue[Double, Int](4)
    pq.put(1.0, 1)
    pq.put(10.0, 10)
    pq.put(10.0, 11)
    pq.put(10.0, 12)
    pq.put(3.0, 3)
    pq.put(6.0, 6)
    println(pq.toValues)
  }
}