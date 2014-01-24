package com.buzzinate.lezhi.util

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