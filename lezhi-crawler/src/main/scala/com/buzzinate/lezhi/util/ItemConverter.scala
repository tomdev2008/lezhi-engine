package com.buzzinate.lezhi.util

import com.buzzinate.crawl.Item
import com.buzzinate.thrift.frontier.{Item => TItem}
import org.buzzinate.lezhi.util.SignatureUtil

object ItemConverter {
  def asThrift(items: List[Item]): List[TItem] = {
    items.map { item =>
      val id = SignatureUtil.signature(item.url)
      val host = DomainNames.safeGetHost(item.url)
      val queue = if (host.contains("51chudui.com")) "51chudui.com" else host
      TItem(id, Item.toString(item), queue)
    }
  }
}