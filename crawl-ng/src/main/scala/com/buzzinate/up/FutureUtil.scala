package com.buzzinate.up

import com.twitter.util.Future
import java.util.concurrent.atomic.AtomicBoolean
import com.twitter.util.Promise
import com.twitter.util.Try
import java.util.concurrent.CancellationException

object FutureUtil {
   def convert[T](f: => T): (Runnable, Future[T]) = {
    val runOk = new AtomicBoolean(true)
    val p = new Promise[T]
    val task = new Runnable {
      
      def run() {
        // Make an effort to skip work in the case the promise
        // has been cancelled or already defined.
        if (!runOk.compareAndSet(true, false)) return

        p.update(Try(f))
      }
    }

    p.setInterruptHandler {
      case cause =>
        if (runOk.compareAndSet(true, false)) {
          val exc = new CancellationException
          exc.initCause(cause)
          p.setException(exc)
        }
    }

    (task, p)
  }
}