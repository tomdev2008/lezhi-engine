package com.buzzinate.lezhi

import collection.JavaConverters._
import com.buzzinate.lezhi.util.Loggable
import com.buzzinate.crawl.Item
import com.buzzinate.thrift.frontier.UrlFrontier
import com.buzzinate.thrift.frontier.{Item => TItem}
import org.buzzinate.lezhi.util.SignatureUtil
import com.buzzinate.lezhi.util.DomainNames
import com.buzzinate.lezhi.async.Batch
import java.util.concurrent.Executor

class FrontierSender(threadPool: Executor, frontier: UrlFrontier.FinagledClient) extends Batch[Item](threadPool, 100, 1000 * 60) with Loggable {
  def flush(items: java.util.ArrayList[Item]): Unit = {
    frontier.offer(asThrift(items.asScala.toList))()
  }
  
  private def asThrift(items: List[Item]): List[TItem] = {
    items.map { item =>
      val id = SignatureUtil.signature(item.url)
      val host = DomainNames.safeGetHost(item.url)
      val queue = if (host.contains("51chudui.com")) "51chudui.com" else host
      TItem(id, Item.toString(item), queue, Some(1000 * 3600 * 3))
    }
  }
}