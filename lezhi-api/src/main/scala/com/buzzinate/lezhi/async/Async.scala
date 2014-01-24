package com.buzzinate.lezhi.async

import java.lang.{Long => JLong}
import com.buzzinate.lezhi.util.Loggable
import java.util.concurrent.{Executors, TimeUnit, Executor, LinkedBlockingQueue}
import com.google.common.cache.CacheBuilder
import org.buzzinate.lezhi.util.MurmurHash3

class Invoke(requestTime: Long, f: => Unit) {
  def invoke(): Unit = {
    f
  }

  def getRequestTime(): Long = requestTime
}

class RollingKeyset {
  val key2timestamp = CacheBuilder.newBuilder.maximumSize(100000).concurrencyLevel(5).expireAfterWrite(1, TimeUnit.DAYS).build[JLong, JLong]

  def doIf(key: String)(fun: => Unit): Unit = {
    val id = MurmurHash3.hash64(key) & Long.MaxValue
    if (key2timestamp.getIfPresent(id) == null) {
      key2timestamp.put(id, System.currentTimeMillis)
      fun
    }
  }
}

class Async(threadPool: Executor) extends Loggable {
  val MAX_WORKER = 97
  val keyset = new RollingKeyset
  val taskqueues = Array.fill(MAX_WORKER)(new LinkedBlockingQueue[Invoke])

  start

  def asnyc(key: String)(f: => Unit) = {
    keyset.doIf(key) {
      chooseTaskQueue(key).offer(new Invoke(System.currentTimeMillis, f))
    }
  }

  private def start(): Unit = {
    Executors.newSingleThreadExecutor.submit(new Runnable {
      override def run(): Unit = {
        while (!Thread.interrupted) {
          taskqueues.foreach { taskqueue =>
            val invoke = taskqueue.poll(50, TimeUnit.MILLISECONDS)
            if (invoke != null) {
              threadPool.execute(new Runnable {
                override def run(): Unit = {
                  debug("delay async seconds: " + (System.currentTimeMillis - invoke.getRequestTime) / 1000d)
                  invoke.invoke
                }
              })
            }
          }
        }
      }
    })
  }
  
  private def chooseTaskQueue(key: String): LinkedBlockingQueue[Invoke] = {
    val idx = (key.hashCode() & 0x7FFFFFFF) % MAX_WORKER
    taskqueues(idx)
  }
}

object Async {
  def main(args: Array[String]): Unit = {
    val tp = Executors.newFixedThreadPool(8)
    val async = new Async(tp)
    
    for (i <- 0 until 100) {
      async.asnyc(i.toString) {
        println(Thread.currentThread.getId + " println " + i)
        async.asnyc(i.toString) {
          println(Thread.currentThread.getId + " println again " + i)
        }
      }
    }
  }
}