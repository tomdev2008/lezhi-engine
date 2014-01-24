package com.buzzinate.stream

import com.twitter.util._
import scala.collection.mutable.ListBuffer
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

trait Track {
  def onError(t: Throwable): Unit
  def onFilter(): Unit
  def onCommit(): Unit
  
  val acked = new AtomicBoolean(false)
  
  def error(t: Throwable): Unit = {
    if (acked.compareAndSet(false, true)) onError(t)
  }
  
  def filter(): Unit = {
    if (acked.compareAndSet(false, true)) onFilter
  }
  
  def commit(): Unit = {
    if (acked.compareAndSet(false, true)) onCommit
  }
}

case class WithTrack[A](data: A, track: Track)

trait Transform[A] {
  def doOffer(e: WithTrack[A]): Unit
}

trait Offerable[A] {
  def offer(e: WithTrack[A]): Unit
}

class FilterTransform[A](dest: Offerable[A], f: A => Boolean) extends Transform[A] {
  def doOffer(e: WithTrack[A]) = {
    if (f(e.data)) dest.offer(e)
    else e.track.filter
  }
}

class NormalTransform[A, B](dest: Offerable[B], f: A => B) extends Transform[A] {
  def doOffer(e: WithTrack[A]) = dest.offer(WithTrack(f(e.data), e.track))
}

class FlatFutureTransform[A, B](dest: Offerable[B], f: A => Future[Iterable[B]]) extends Transform[A] {
  def doOffer(e: WithTrack[A]) = {
    try {
      f(e.data) onSuccess { bs =>
        bs foreach { b => dest.offer(WithTrack(b, e.track)) }
      } onFailure { t =>
        e.track.error(t)
      }
    } catch {
      case t => e.track.error(t)
    }
  }
}

class FutureTransform[A, B](dest: Offerable[B], f: A => Future[B]) extends Transform[A] {
  def doOffer(e: WithTrack[A]) = {
    try {
      f(e.data) onSuccess { b =>
        dest.offer(WithTrack(b, e.track))
      } onFailure { t =>
        e.track.error(t)
      }
    } catch {
      case t => e.track.error(t)
    }
  }
}

trait LazyQueue[A] extends Offerable[A] {
  // TODO: 线程安全？
  val transforms = new ListBuffer[Transform[A]]
  
  def offer(e: WithTrack[A]): Unit = {
    transforms foreach { t => t.doOffer(e) }
  }
}

class FutureStream[A](futurePool: FuturePool) extends LazyQueue[A] {
  def makeFuture[A, B](f: A => B): A => Future[B] = {
    { e =>
      futurePool.apply(f(e))
    }
  }
  
  def map[B](f: A => B): FutureStream[B] = {
    val s = new FutureStream[B](futurePool)
    transforms += new FutureTransform(s, makeFuture(f))
    s
  }
  
  def filter(f: A => Boolean): FutureStream[A] = {
    val s = new FutureStream[A](futurePool)
    transforms += new FilterTransform(s, f)
    s
  }
  
  def flatMap[B](f: A => Iterable[B]): FutureStream[B] = {
    val s = new FutureStream[B](futurePool)
    transforms += new FlatFutureTransform(s, makeFuture(f))
    s
  }
  
  def mapFuture[B](f: A => Future[B]): FutureStream[B] = {
    val s = new FutureStream[B](futurePool)
    transforms += new FutureTransform(s, f)
    s
  }
  
  def batch(batchSize: Int, interval: Int)(f: Iterable[A] => Unit): FutureStream[A] = {
    val s = new BatchStream[A](batchSize, interval, f)
    transforms += new NormalTransform(s, x => x)
    this
  }
  
  def commit(): CommitStream[A] = {
    val s = new CommitStream[A]
    transforms += new NormalTransform(s, x => x)
    s
  }
}

class CommitStream[A] extends Offerable[A] {
  def offer(e: WithTrack[A]): Unit = e.track.commit
}

class BatchStream[A](batchSize: Int, interval: Int, f: Iterable[A] => Unit) extends Offerable[A] {
  val buf = new ListBuffer[WithTrack[A]]
  var lastFlush = System.currentTimeMillis
  val lock = new ReentrantLock

  BatchStream.timer.schedule(Duration.fromMilliseconds(interval)) {
    checkBatch
  }
  
  def offer(e: WithTrack[A]): Unit = {
    lock.lock
    try {
      buf += e
    } finally {
      lock.unlock
    }
    checkBatch
  }

  private def checkBatch(): Unit = {
    lock.lock
    val batches = try {
      if (buf.size >= batchSize || System.currentTimeMillis >= lastFlush + interval) {
        val res = buf.result
        buf.clear
        lastFlush = System.currentTimeMillis
        Some(res)
      } else None
    } finally {
      lock.unlock
    }

    batches.map { batch =>
      try {
        f(batch.map(x => x.data))
      } catch {
        case t => batch.map(x => x.track.error(t))
      }
    }
  }
}

object BatchStream {
  val timer = new JavaTimer(true)
}

trait Source[A] extends LazyQueue[A] {
  def start(): Unit
}

class MemorySource[A](ite: Iterable[A]) extends Source[A] {
  case class MemoryTrack[A](id: A) extends Track {
    def onError(t: Throwable): Unit = {
      System.err.println(id + " with error: " + t.getMessage)
    }
    
    def onFilter(): Unit = {
      println(id + " is filtered")
    }
    
    def onCommit(): Unit = {
      println("commit " + id)
    }
  }
  
  def offer(e: A): Unit = offer(WithTrack(e, MemoryTrack(e)))
  
  def start() = {
    ite.foreach { e =>
      offer(WithTrack(e, MemoryTrack(e)))
    }
  }
}

object FutureStream {
  def from[A](src: Source[A], futurePool: FuturePool = FuturePool(Executors.newFixedThreadPool(128))): FutureStream[A] = {
    val s = new FutureStream[A](futurePool)
    src.transforms += new NormalTransform(s, x => x)
    s
  }
  
  def main(args: Array[String]): Unit = {
    val src = new MemorySource(0 until 10)
    
    from(src).mapFuture { x =>
      Future.value(x.toString)
    }.flatMap { str =>
      for (i <- 0 until str.length) yield str.substring(i, i + 1)
    }.batch(1, 2000) { strs =>
      println("batch2: " + strs.mkString(","))
    }.batch(1, 100) { strs =>
      println("batch1: " + strs.mkString(","))
    }.commit
    
    src.start
  }
}