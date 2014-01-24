package com.buzzinate.lezhi.util

class OrdComp[E](ord: Ordering[E]) extends java.util.Comparator[E] {
  override def compare(e1: E, e2: E): Int = {
    ord.compare(e1, e2)
  }
} 

class FixedTreeSet[E, B](len: Int, f: E => B)(implicit ord: Ordering[B]) extends java.util.TreeSet[E](new OrdComp(ord on f)) {
  override def add(e: E): Boolean = {
    if (size < len) {
      super.add(e)
      true
    } else {
      val cmp = super.comparator().compare(e, this.last())
      if (cmp < 0) {
        pollLast
        super.add(e)
        true
      } else false
    }
  }
}