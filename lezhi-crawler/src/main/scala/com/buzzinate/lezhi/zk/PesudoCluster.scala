package com.buzzinate.lezhi.zk

class PesudoCluster(pid: String) extends Cluster {
  def id(): String = pid
  def join(): Unit = {}
}