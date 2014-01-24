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
import scala.collection.mutable.HashSet
import scala.util.Random
import com.buzzinate.minhash.MinhashBuilder
import com.buzzinate.minhash.BandSim

case class RecItem(itemid: Int, score: Double)

class MinhashItemCF(item2item: Map[Int, List[RecItem]], byUser: Map[Int,Set[Int]]) extends Recommend {
  def recommend(u: Int, topN: Int): List[TopItem] = {
    val rank = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
    val myprefs = byUser.getOrElse(u, Set.empty)
    
    for {
      mypref <- myprefs
      ris <- item2item.get(mypref)
      RecItem(otheritem, score) <- ris
      if (!myprefs.contains(otheritem))
    } {
      rank.adjustOrPut(otheritem, score, score)
    }
    
    val r = rank.map { case (itemId, score) =>
      TopItem(itemId, score)
    }.toList.sortBy { case TopItem(_, score) => -score}
    r.take(topN)
  }
}

class MinhashItemCFBuilder extends RecommendBuilder {
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
        band.buckets.map(bucket => bucket -> itemid)
      }
    }.groupBy(x => x._1).map { case (bucket, xs) =>
      bucket -> xs.map(x => x._2)
    }
    
    val item2item = item2mh.map { case (itemid, bands) =>
      val itemset = new HashSet[Int]
      for {
        myband <- bands
        bucket <- myband.buckets
        subitems <- bucket2items.get(bucket)
      } {
        itemset ++= subitems
      }
      itemset -= itemid
      
      val bandsim = new BandSim(bands)
      val pq = new DoublePriorityQueue[RecItem](40)
      for {
        otheritem <- itemset
        otherbands <- item2mh.get(otheritem)
      } {
        val score = bandsim.sim(otherbands)
        pq.add(score, RecItem(otheritem, score))
      }
      itemid -> pq.values.asScala.toList
    }
    
//    println(item2mhes)
    println("build item minhashes successfully")
    
    val byUser = trainset.groupBy(r => r.userId).map { case (userid, rs) =>
      userid -> rs.map(r => r.itemId).toSet
    }
    
    Map() ++ List(10, 40).map { k =>
      val subitem2item = item2item.map(x => x._1 -> x._2.take(k)) 
      ("MinhashItemCF-" + k) -> new MinhashItemCF(subitem2item, byUser)
    }
  }
}

object MinhashItemCF {
  
  def main(args: Array[String]): Unit = {
    val random = new Random(0x3c074a61)
    for (i <- 0 until 10) {
      println(random.nextInt)
    }
    
    if (true) return
    
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
        
    val ucfb = new MinhashItemCFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}