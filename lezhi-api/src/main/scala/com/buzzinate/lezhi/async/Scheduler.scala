package com.buzzinate.lezhi.async

import java.util.concurrent.{Executors, DelayQueue, TimeUnit, Delayed}
import com.buzzinate.lezhi.util.Loggable

class TimeTask(since: Long, delay: Int, repeat: Boolean, fun: Long => Unit) extends Delayed {
  def start(): Unit = {
    fun(since)
  }

  def expireTime(): java.lang.Long = since + delay

  def nextTask(): Option[TimeTask] = {
    if (repeat) Some(new TimeTask(System.currentTimeMillis, delay, true, fun))
    else None
  }

  override def getDelay(unit: TimeUnit): Long = {
    unit.convert(since + delay - System.currentTimeMillis, TimeUnit.MILLISECONDS)
  }

  override def compareTo(o: Delayed): Int = {
    val other = o.asInstanceOf[TimeTask]
    expireTime.compareTo(other.expireTime)
  }
}

object Scheduler extends Loggable {
  val taskqueue = new DelayQueue[TimeTask]

  Executors.newSingleThreadExecutor.submit(new Runnable {
    override def run(): Unit = {
      while (!Thread.interrupted) {
        val timetask = taskqueue.take
        timetask.start
        timetask.nextTask.foreach { tt => taskqueue.offer(tt) }
      }
    }
  })

  schedule(1000 * 600) { since =>
    info("#### total tasks: " + taskqueue.size)
  }

  def scheduleOnce(delay: Int)(fun: Long => Unit) {
    taskqueue.offer(new TimeTask(System.currentTimeMillis, delay, false, fun))
  }

  def schedule(delay: Int)(fun: Long => Unit): Unit = {
    taskqueue.offer(new TimeTask(System.currentTimeMillis, delay, true, fun))
  }
}