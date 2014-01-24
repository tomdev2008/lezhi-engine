package com.buzzinate.lezhi.test

import com.buzzinate.lezhi.Servers

object TestTrending {
  def main(args: Array[String]): Unit = {
    Servers.getTrending("http://www.chinadaily.com.cn") foreach { d =>
      println(d)
    }
  }
}
