package com.buzzinate.lezhi.test

import com.buzzinate.crawl.{Item, FrontierClient}
import com.buzzinate.lezhi.util.ItemConverter

object TestCustomThumbnail {
  val prop = com.buzzinate.lezhi.util.Config.getConfig("config.properties")

  def main(args: Array[String]): Unit = {
    val frontierClient = new FrontierClient(prop.getProperty("thumbnail.frontier.host", "localhost"))
    val items = List(Item("http://pic.mop.com/mn/130318012120976.shtml", "custom.thumbnail" -> "http://postimg1.mop.com/2013/03/18/13635822414161134.jpg"))
    frontierClient.client.offer(ItemConverter.asThrift(items)) onSuccess { s =>
      println("status: " + s)
    }
  }
}