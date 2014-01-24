package com.buzzinate.lezhi.render

import collection.JavaConverters._
import com.buzzinate.lezhi.util.ResourceLoader
import org.arabidopsis.ahocorasick.AhoCorasick
import scala.collection.mutable.ListBuffer
import com.buzzinate.lezhi.util.HungarianAlgorithm
import scala.collection.mutable
import com.buzzinate.json.RenderThumbnail
import org.buzzinate.lezhi.util.MurmurHash3
import java.nio.charset.Charset

case class IdxWeight(idx: Int, weight: Double)

object ThumbnailMatcher {
  val UTF8 = Charset.forName("UTF-8")

  val (thumbnailTree, thumbnails) = loadDefaultPics
  val all = getAll
  
  private def getAll(): List[IdxWeight] = {
    val sr = thumbnailTree.search("ALL".getBytes("UTF-8"))
    val idxweights = new ListBuffer[IdxWeight]
    while (sr.hasNext) {
      idxweights ++= sr.next.getOutputs.asScala
    }
    idxweights.result
  }

  // 采用匈牙利最大权匹配站内图片
  def matchThumbnails(doc2vacPics: Map[String, List[RenderThumbnail]]): Map[String, String] = {
    if (doc2vacPics.isEmpty) return Map()

    val pic2idx = new mutable.HashMap[String, Int]
    doc2vacPics foreach { case (_, pics) =>
      pics foreach { pic =>
        pic2idx.getOrElseUpdate(pic.thumbnail, pic2idx.size)
      }
    }
    val pics = Array.ofDim[String](pic2idx.size)
    pic2idx foreach { case (pic, idx) =>
      pics(idx) = pic
    }

    val cost = Array.ofDim[Double](doc2vacPics.size, pic2idx.size)
    val docs = Array.ofDim[String](doc2vacPics.size)
    doc2vacPics.zipWithIndex foreach { case ((doc, pics), i) =>
      docs(i) = doc
      pics foreach { pic =>
        val idx = pic2idx.getOrElse(pic.thumbnail, -1)
        if (idx >= 0) cost(i)(idx) -= pic.score
      }
    }

    val ha = new HungarianAlgorithm(cost)
    val url2pics = ha.execute.zipWithIndex filter { case (assignment, i) =>
      assignment >= 0
    } map { case (assignment, i) =>
      docs(i) -> pics(assignment)
    }
    Map() ++ url2pics
  }
  
  // 采用匈牙利最大权匹配lezhi图片库里的图片
  def matchDefaultThumbnails(url2title: Map[String, String]): Map[String, String] = {
    if (url2title.isEmpty) return Map()
    
    val cost = Array.ofDim[Double](url2title.size, thumbnails.length)
    val urls = Array.ofDim[String](url2title.size)
    url2title.zipWithIndex foreach { case ((url, title), i) =>
      urls(i) = url
      val sr = thumbnailTree.search(title.getBytes("UTF-8"))
      while (sr.hasNext) {
        sr.next.getOutputs.asScala foreach { case IdxWeight(idx, weight) =>
          cost(i)(idx) += -weight
        }
      }
    }
    
    for (i <- 0 until urls.length) {
      val hi = hashFor(urls(i))
      all foreach { case IdxWeight(j, _) =>
        val hj = hashFor(thumbnails(j))
        if (hi > hj) cost(i)(j) -= 1 / math.log(hi - hj)
        else cost(i)(j) -= 1 / math.log(hi - hj + Int.MaxValue)
      }
    }
    
    val ha = new HungarianAlgorithm(cost)
    val url2thumbnails = ha.execute.zipWithIndex filter { case (assignment, i) =>
      assignment >= 0
    } map { case (assignment, i) => 
      urls(i) -> thumbnails(assignment)
    }
    Map() ++ url2thumbnails 
  }
  
  private def hashFor(str: String): Int = {
    MurmurHash3.MurmurHash3_x64_32(str.getBytes(UTF8), 0x1234ABCD) & Int.MaxValue
  }
  
  private def loadDefaultPics(): (AhoCorasick[IdxWeight], Array[String]) = {
    val picTree = new AhoCorasick[IdxWeight]
    val lines = ResourceLoader.loadResourceFile("tagimages")
    val thumbnails = Array.ofDim[String](lines.length)
    lines.zipWithIndex foreach { case (line, i) =>
      val splits = line.split("-")
      val tags = splits.apply(0).split(",")
      val thumbnail = splits.apply(1)
      thumbnails(i) = thumbnail
      
      val weight = 1.0 / tags.size
      val tw = IdxWeight(i, weight)
      tags foreach { tag =>
        picTree.add(tag.getBytes(UTF8), tw)
      }
    }
    picTree.prepare
    (picTree, thumbnails)
  }
  
  def main(args: Array[String]): Unit = {
    val testUrl2Titles = Map("url0" -> "", "url1"->"军事强国", "url2"->"天猫商城", "url3" -> "阿里巴巴 电子商务领头羊", "url4" -> "姚明登陆NBA篮球大联盟","url5" -> "阿里巴巴 电子商务领头羊","url6" -> "阿里巴巴 电子商务领头羊","url7" -> "阿里巴巴 电子商务领头羊")
//    val testUrl2Titles = Map("url0" -> "", "url1"->"", "url2" -> "", "url3" -> "")
    println(matchDefaultThumbnails(testUrl2Titles))
  }
}