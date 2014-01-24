package com.buzzinate.lezhi.render

import com.buzzinate.lezhi.thrift._
import collection.JavaConverters._
import com.buzzinate.json.RenderItem
import scala.Some

case class RenderDoc(url: String, title: String, thumbnail: String, picType: Option[PicType], score: Double, hotScore: Double)

object RenderDoc {
  val emptyItems = Seq[RecommendItem]()

  def empty(recTypes: List[RecommendTypeParam]): RecommendResult = {
    val results = recTypes map { recType =>
      RecommendItemList(recType, emptyItems)
    }
    RecommendResult(results, "")
  }

  def convert(renderDocs: List[(RecommendTypeParam, List[RenderDoc])]): RecommendResult = {
    val results = renderDocs map { case (recType, docs) =>
      val items = docs map { case RenderDoc(url, title, thumbnail, picType, score, hotScore) =>
        RecommendItem(url, title, Option(thumbnail), Option(score), Option(hotScore))
      }
      RecommendItemList(recType, items)
    }
    RecommendResult.apply(results.toList, "")
  }
  
  def toRenderItems(docs: List[RenderDoc]): java.util.List[RenderItem] = {
    docs map { doc => toRenderItem(doc) } asJava
  }
  
  def fromRenderItems(items: java.util.List[RenderItem]): List[RenderDoc] = {
    items.asScala map { item => fromRenderItem(item) } toList
  }
  
  private def toRenderItem(doc: RenderDoc): RenderItem = {
    val item = new RenderItem
    item.url = doc.url
    item.title = doc.title
    item.pic = doc.thumbnail
    doc.picType map { picType => item.picType = picType }
    item.score = doc.score
    item.hotScore = doc.hotScore
    item
  }
  
  private def fromRenderItem(item: RenderItem): RenderDoc = {
    val picType = if (item.picType == null) None else Some(item.picType)
    RenderDoc(item.url, item.title, item.pic, picType, item.score, item.hotScore)
  }
}