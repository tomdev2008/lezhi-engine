package com.buzzinate.keywords.util

class PriorityQueue[E](top: Int) {
  import PriorityQueue.Entry

  private var nsize = 0
  private val heap = Array.ofDim[Entry[E]](top+1)

  /**
   * Adds an Object to a PriorityQueue in log(size) time. It returns the
   * object (if any) that was dropped off the heap because it was full. This
   * can be the given parameter (in case it is smaller than the full heap's
   * minimum, and couldn't be added), or another object that was previously
   * the smallest value in the heap and now has been replaced by a larger one,
   * or null if the queue wasn't yet full with maxSize elements.
   */
  def add(key: Double, value: E): Option[Entry[E]] = {
    if (nsize < top) {
      nsize += 1
      heap(nsize) = Entry(key, value)
      upHeap
      None
    } else if (nsize > 0 && key >= heap(1).key) {
      val old = heap(1)
      heap(1) = Entry(key, value)
      updateTop
      Some(old)
    } else Some(Entry(key, value))
  }

  def values(): List[E] = {
    val entries = Array.ofDim[Entry[E]](nsize)
    System.arraycopy(heap, 1, entries, 0, nsize)
    entries sortBy(-_.key) map(_.value) toList
  }

  /**
   * Removes and returns the least element of the PriorityQueue in log(size)
   * time.
   */
  def pop(): Option[Entry[E]] = {
    if (nsize > 0) {
      val result = heap(1)
      heap(1) = heap(nsize)
      heap(nsize) = null
      nsize -= 1
      downHeap
      Some(result)
    } else None
  }

  /** Returns the number of elements currently stored in the PriorityQueue. */
  def size(): Int = nsize

  /**
   * Should be called when the Object at top changes values. Still log(n)
   * worst case, but it's at least twice as fast to
   *
   * <pre>
   * pq.top().change();
   * pq.updateTop();
   * </pre>
   *
   * instead of
   *
   * <pre>
   * o = pq.pop();
   * o.change();
   * pq.push(o);
   * </pre>
   *
   * @return the new 'top' element.
   */
  def updateTop(): Entry[E] = {
    downHeap
    heap(1)
  }

  private def upHeap(): Unit = {
    var i = nsize
    val node = heap(i)
    var j = i >>> 1
    while (j > 0 && node.key < heap(j).key) {
      heap(i) = heap(j)
      i = j
      j = j >>> 1
    }
    heap(i) = node
  }

  private def downHeap(): Unit = {
    var i = 1
    val node = heap(i)
    var j = i << 1
    var k = j + 1
    if (k <= nsize && heap(k).key < heap(j).key) {
      j = k
    }
    while (j <= nsize && heap(j).key < node.key) {
      heap(i) = heap(j)
      i = j
      j = i << 1
      k = j + 1
      if (k <= nsize && heap(k).key < heap(j).key) {
        j = k
      }
    }
    heap(i) = node
  }
}

object PriorityQueue {
  case class Entry[E](key: Double, value: E)

  def main(args: Array[String]) {
    val pq = new PriorityQueue[(Int, Double)](4)
    val ds = Array[Double](0.1, 0.2, 0.7, 0.2, 0.2, 0.3, 0.3, 0.5, 0.5, 0.5)
    for (i <- 0 until ds.length) {
      val e = pq.add(ds(i), (i, ds(i)))
      println((i, ds(i)) + " => " + e)
    }
    println(pq.values)
  }
}