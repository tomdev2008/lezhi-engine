package com.buzzinate.lezhi.util

import com.buzzinate.up.UpYunClient
import com.buzzinate.up.UpYunUtil
import com.twitter.util.Future

object ImageUploader extends Loggable {
  val upyun = new UpYunClient("lezhi", "buzzinate", "buzzinate")
  
  def upload(imgsrc: String, url: String, format: String, data: Array[Byte]): Future[String] = {
      val filename = "/" + UpYunUtil.md5(imgsrc) + "." + format
      upyun.upload(DomainNames.safeGetPLD(url), filename, data, true).map { uf =>
        info("################ thumbnail: " + url + " ==> " + uf.url)
        uf.url
      }
  }
}