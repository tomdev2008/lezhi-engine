package com.buzzinate.lezhi.api

import com.twitter.util._
import com.buzzinate.lezhi.thrift.RecommendServices
import com.buzzinate.lezhi.util.Loggable
import java.util.concurrent.Executors
import com.buzzinate.lezhi.thrift.RecommendParam
import com.buzzinate.lezhi.thrift.ClickParam
import com.buzzinate.lezhi.thrift.RecommendResult
import com.buzzinate.lezhi.render.RenderDoc

class RecommendServiceImpl extends RecommendServices.FutureIface with Loggable {
  val futurePool = FuturePool(Executors.newFixedThreadPool(128))
  val recommendService = new RecommendServiceHandler
  implicit val timer = new ScheduledThreadPoolTimer(5)

  /**
   * lezhi推荐
   */
  def recommend(param: RecommendParam): Future[RecommendResult] = {
    info("recommend request: " + param.url + " / " + param.title.get)
    futurePool.apply(recommendService.recommend(param)).within(Duration.fromMilliseconds(1500)) transform {
      case Return(v) => Future.value(v)
      case Throw(t) => {
        error("Timeout: " + param.url + " / " + param.title.get, t)
        Future.value(RenderDoc.empty(param.types.toList))
      }
    }
  }

  /**
   * 记录用户查看行为
   */
  def click(param: ClickParam): Future[Unit] = {
    futurePool.apply(recommendService.click(param))
  }

  /**
   * 重新抓取
   */
  def recrawl(url: String): Future[Unit] = {
    futurePool.apply(recommendService.recrawl(url))
  }

  /**
   * 纠正代表文章的图片
   */
  def correctImg(url: String, rightImg: String, userAgent: Option[String]): Future[Unit] = {
    futurePool.apply(recommendService.correctImg(url, rightImg, userAgent))
  }
}