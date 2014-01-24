package com.buzzinate.lezhi.api

import java.util.concurrent.Executors
import scala.collection.JavaConverters._
import com.buzzinate.lezhi.thrift._
import com.buzzinate.lezhi.util.Loggable
import com.twitter.util.Future
import com.twitter.util.FuturePool
import com.buzzinate.lezhi.render.RenderDoc

class NoopRecommendServiceImpl extends RecommendServices.FutureIface with Loggable {
    val futurePool: FuturePool = FuturePool(Executors.newFixedThreadPool(128))

  def recommend(param: RecommendParam): Future[RecommendResult] = {
    Thread.sleep(1000)
//    RenderDoc.empty(param.getTypes.asScala.toList)
    futurePool.apply( RenderDoc.empty(param.types.toList))
  }
  
   def click(param: ClickParam):  Future[Unit] = {
      futurePool.apply()
   }
   
   def recrawl(url: String): Future[Unit] = {
      futurePool.apply()
   }
   
   def correctImg(url: String, rightImg: String, userAgent: Option[String]):Future[Unit]={
      futurePool.apply()
   }
}