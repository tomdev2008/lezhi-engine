package com.buzzinate.lezhi.zk

trait Cluster {
  def id(): String
  def join(): Unit
}