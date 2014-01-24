package com.buzzinate.http

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit
import java.util.concurrent.DelayQueue
import scala.collection.mutable.HashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock

trait LockSupport {
  def lock(): Unit
  def unlock(): Unit
  
  def inlock[T](fun: => T): T = {
    lock
    try {
      fun
    } finally {
      unlock
    }
  }
}

class TaskQueues[K] {
  type Queue = LinkedBlockingQueue[Runnable]
  
  val key2queue = new HashMap[K, Queue]
  val lock = new ReentrantLock with LockSupport
  
  def offerOrNew(key: K, task: Runnable)(f: (K, Queue) => Unit): Unit = lock.inlock {
    val queue = key2queue.getOrElseUpdate(key, new Queue)
    queue.offer(task)
    if (queue.peek == task) f(key, queue)
  }
  
  def checkEmpty(key: K)(f: Boolean => Unit): Unit = lock.inlock {
    val empty = key2queue.get(key).map(q => q.isEmpty).getOrElse(true)
    f(empty)
  }
  
  def total(): Int = lock.inlock { key2queue.size }
}


case class PendingQueue[K, Q](key: K, queue: Q, lastFetchTime: Long, count: Int = 0) extends Delayed {
  override def compareTo(o: Delayed): Int = {
    lastFetchTime.compareTo(o.asInstanceOf[PendingQueue[K, Q]].lastFetchTime)
  }
  
  override def getDelay(unit: TimeUnit): Long = {
    unit.convert(lastFetchTime + 500 - System.currentTimeMillis, TimeUnit.MILLISECONDS)
  }
  
  def update(): PendingQueue[K, Q] = PendingQueue(key, queue, System.currentTimeMillis, count + 1)
}

class RollQueues[K](activeSize: Int, batchSize: Int, retainFun: K => Boolean) {
  type Queue = LinkedBlockingQueue[Runnable]
  
  val queues = new TaskQueues[K]
  val active = new DelayQueue[PendingQueue[K, Queue]]
  val backup = new DelayQueue[PendingQueue[K, Queue]]
  val activeCount = new AtomicInteger(0)
  
  def submit(key: K, task: Runnable): Unit = {
    queues.offerOrNew(key, task) { case (key, queue) =>
      backup.offer(PendingQueue(key, queue, System.currentTimeMillis - 500))
    }
  }
  
  def take(): PendingQueue[K, Queue] = {
    while (active.peek == null) {
      val ac = activeCount.get
      if (ac < activeSize && backup.peek != null && activeCount.compareAndSet(ac, ac + 1)) {
        active.offer(backup.take)
      }
    }
    active.take
  }
  
  def stat(): String = "# active: " + activeCount.get + " / # total: " + queues.total
  
  def back(pq: PendingQueue[K, Queue]): Unit = {
    queues.checkEmpty(pq.key) { empty =>
      if (!empty) {
        if (pq.count < batchSize || retainFun(pq.key)) {
//          println("back active: " + pq.key)
          active.offer(pq)
        }
        else {
//          println("back nonactive: " + pq.key)
          activeCount.decrementAndGet
          backup.offer(pq)
        }
      } else activeCount.decrementAndGet
    }
  }
}