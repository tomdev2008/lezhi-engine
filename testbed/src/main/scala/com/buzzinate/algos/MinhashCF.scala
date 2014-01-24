package com.buzzinate.algos

import com.buzzinate.api.RecommendBuilder
import scala.collection.immutable.List
import com.buzzinate.api.Rating
import com.buzzinate.api.Recommend
import com.buzzinate.api.TopItem
import com.buzzinate.util.HashMapUtil
import scala.collection.mutable.HashMap
import com.buzzinate.util.DoublePriorityQueue
import collection.JavaConverters._
import com.buzzinate.minhash.MinhashBuilder
import com.buzzinate.minhash.MinhashMerger
import com.buzzinate.minhash.Band
import com.buzzinate.minhash.BandSim

case class MinFeature(band: Band, count: Int)

class MinhashCF(user2mh: Map[Int, Array[(Band, Int)]], bucket2items: Map[String, List[Int]], item2mh: Map[Int, Array[Band]], byUser: Map[Int,Set[Int]]) extends Recommend {
  def recommend(u: Int, topN: Int): List[TopItem] = {
    val rank = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
    val myprefs = byUser.getOrElse(u, Set.empty)
    
    val mybands = user2mh.getOrElse(u, Array.empty[(Band, Int)])
    val itemcnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for {
      (myband, count) <- mybands
      bucket <- myband.buckets
      otheritems <- bucket2items.get(bucket)
      otheritem <- otheritems
      if (!myprefs.contains(otheritem))
    } {
      itemcnt.adjustOrPut(otheritem, count, count)
//      val mh
//      rank.adjustOrPut(otheritem, score, score)
    }
    val ipq = new DoublePriorityQueue[Int](topN * 3)
    itemcnt.foreach { case (item, cnt) =>
      ipq.add(cnt, item)
    }
    val itemset = ipq.values.asScala
    
    val mybandsim = new BandSim(mybands.map(x => x._1))
    for {
      item <- itemset
      band <- item2mh.get(item)
    } {
      val score = mybandsim.sim(band)
      rank.adjustOrPut(item, score, score)
    }
    
    val r = rank.map { case (itemId, score) =>
      TopItem(itemId, score)
    }.toList.sortBy { case TopItem(_, score) => -score}
    r.take(topN)
  }
}

class MinhashCFBuilder extends RecommendBuilder {
  def train(trainset: List[Rating]): Map[String, Recommend] = {
    val item2mh = trainset.groupBy(r => r.itemId) map { case (itemid, rs) =>
      val mhb = new MinhashBuilder(20, 5)
      rs foreach { r =>
        mhb.add(r.userId)
      }
      itemid -> mhb.build
    }
    
    val bucket2items = item2mh.toList.flatMap { case (itemid, bands) =>
      bands.flatMap { band =>
        band.buckets.map( bucket => bucket -> itemid)
      }
    }.groupBy(x => x._1).map { case (bucket, xs) =>
      bucket -> xs.map(x => x._2)
    }
    
    val byUser = trainset.groupBy(r => r.userId).map { case (userid, rs) =>
      userid -> rs.map(r => r.itemId).toSet
    }
    
    val user2mh = byUser map { case (userid, items) =>
      val merger = new MinhashMerger(80)
      for {
        item <- items
        bands <- item2mh.get(item)
      } {
        merger.add(bands)
      }
      userid -> merger.build
    }
    println("build minhashes successfully")
    
    Map("MinhashCF" -> new MinhashCF(user2mh, bucket2items, item2mh, byUser))
  }
}

object MinhashCF {
  
  def main(args: Array[String]): Unit = {
    val train = List(
        Rating(1, 1, 4, 0),
        Rating(1, 2, 4, 0),
        Rating(1, 4, 4, 0),
        Rating(2, 1, 4, 0),
        Rating(2, 3, 4, 0),
        Rating(3, 2, 4, 0),
        Rating(3, 5, 4, 0),
        Rating(4, 3, 4, 0),
        Rating(4, 4, 4, 0),
        Rating(4, 5, 4, 0)
        )
        
    val ucfb = new MinhashCFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}