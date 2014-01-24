package com.buzzinate.lezhi.async

import java.util.concurrent.{Executors, Executor, LinkedBlockingQueue}
import java.util.concurrent.atomic.AtomicLong
import com.buzzinate.lezhi.util.Loggable

abstract class Batch[T](threadPool: Executor, batchSize: Int, interval: Int) extends Loggable {
  val objs = new LinkedBlockingQueue[T]
  val lastFlush = new AtomicLong(System.currentTimeMillis)

  Scheduler.schedule(interval) { since =>
    debug("Check since " + (System.currentTimeMillis - since))
    checkFlush(0)
  }

  def !(obj: T): Unit = add(obj)

  def add(obj: T): Unit = {
    objs.offer(obj)
    checkFlush(batchSize)
  }

  def flush(batch: java.util.ArrayList[T]): Unit

  private def checkFlush(checkSize: Int): Unit = {
    val now = System.currentTimeMillis
    if (objs.size > checkSize || now > lastFlush.get + interval) {
      val batch = new java.util.ArrayList[T]
      objs.drainTo(batch)
      lastFlush.set(now)
      if (batch.size > 0) {
        threadPool.execute( new Runnable {
          override def run(): Unit = {
            flush(batch)
          }
        })
      }
    }
  }
}

object Batch {
  def main(args: Array[String]): Unit = {
    val tp = Executors.newFixedThreadPool(8)
    val b = new Batch[Int](tp, 100, 1000) {
      def flush(ints: java.util.ArrayList[Int]): Unit = {
        println("batch: " + ints)
      }
    }
    for (i <- 0 until 99) b.add(i)
  }
}