package com.buzzinate.lezhi.test

import com.buzzinate.lezhi.thrift.{RecommendTypeParam, PicType, RecommendParam, RecommendType}
import collection.JavaConverters._
import com.buzzinate.lezhi.api.RecommendServiceImpl
import scala.collection.mutable.ListBuffer

object TestPref {
  def main(args: Array[String]): Unit = {
    val service = new RecommendServiceImpl
    
   
    val typeParams = new ListBuffer[RecommendTypeParam]
    val typeParam1 = RecommendTypeParam(1, RecommendType.Insite, PicType.Insite, 8)
    typeParams.append(typeParam1)

    val typeParam2 = RecommendTypeParam.apply(2, RecommendType.Personalized, PicType.Insite, 8)
    typeParams.append(typeParam2)

    val params = RecommendParam("http://test.buzzinate.com/wordpress/?p=100",Seq[RecommendTypeParam](),Option("三九养生堂携手bShare进军社交网络! | bshare之家"),Option("http://test.buzzinate.com"),"1", None, None, None,None )
    
    for (i <- 0 until Int.MaxValue) {
      val start = System.currentTimeMillis
      val r = service.recommend(params)
      println(r)
      println("cost: " + (System.currentTimeMillis - start))
    }
  }
}