package com.buzzinate.http

import com.twitter.util.Future
import org.apache.commons.httpclient.HostConfiguration
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit
import scala.collection.mutable.HashMap
import java.util.concurrent.DelayQueue
import java.util.concurrent.locks.ReentrantLock
import com.twitter.util.Promise
import java.util.concurrent.ExecutorService
import com.buzzinate.up.FutureUtil
import org.apache.log4j.Logger



class PolicyHttpScheduler(val client: HttpClient, threadPool: ExecutorService, coreSize: Int, batchSize: Int) extends HttpScheduler {
  val rollQueues = new RollQueues[HostConfiguration](coreSize, batchSize, hc => PolicyHttpScheduler.retainSites.contains(hc.getHost))
  
  new Thread(new Runnable {
    def run() {
      start
    }
  }).start
    
  override def submit(hostconfig: HostConfiguration, method: HttpMethod): Future[HttpResponse] = {
    val (task, future) = FutureUtil.convert {
      try {
        client.executeMethod(hostconfig, method)
        HttpResponse.fromHttpMethod(method)
      } catch {
        case t => throw new RuntimeException(t.getMessage + " with url:" + method.getURI, t)
      } finally {
        method.releaseConnection
      }
    }
    
    rollQueues.submit(hostconfig, task)
    
    future
  }
  
  def start(): Unit = {
    var lastInfoTime = 0L
    while (!Thread.interrupted) {
      val q = rollQueues.take
      if (System.currentTimeMillis > lastInfoTime + 1000 * 600) {
        PolicyHttpScheduler.info(rollQueues.stat)
        lastInfoTime = System.currentTimeMillis
      }
      val subtask = q.queue.poll // should not be null
      if (subtask == null) {
        rollQueues.back(q)
      } else {
        val wraptask = new Runnable {
          def run() {
            try {
              val delay = System.currentTimeMillis - q.lastFetchTime
              if (delay > 2000) PolicyHttpScheduler.info("Delay " + q.key + " => " + delay)
              subtask.run
            } catch {
              case t => t.printStackTrace()
            } finally {
              rollQueues.back(q.update)
            }
          }
        }
        threadPool.submit(wraptask)
      }
    }
  }
}

object PolicyHttpScheduler {
  val logger = Logger.getLogger(classOf[PolicyHttpScheduler])

  val retainSites = Set("www.docin.com", "et.21cn.com")
  
  def info(msg: => String): Unit = {
    if (logger.isInfoEnabled) {
      logger.info(msg)
    }
  }
}