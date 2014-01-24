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
import com.buzzinate.api.Feature
import scala.collection.mutable.HashSet

class ItemCF(kNN: Map[Int, List[Feature]], byUser: Map[Int, List[Rating]]) extends Recommend {
  def recommend(u: Int, topN: Int): List[TopItem] = {
    val rank = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
    val myprefs = byUser.getOrElse(u, Nil).map(r => r.itemId).toSet
    
    for {
      i <- myprefs
      Feature(j, score) <- kNN(i)
      if (!myprefs.contains(j))
    } {
      rank.adjustOrPut(j, score, score)
    }
    
    val r = rank.map { case (itemId, score) =>
      TopItem(itemId, score)
    }.toList.sortBy { case TopItem(_, score) => -score}.take(topN)
    r
  }
}

class ItemCFBuilder extends RecommendBuilder {
  def train(trainset: List[Rating]): Map[String, Recommend] = {
    val byItem = trainset.groupBy(r => r.itemId)
    val user2item = trainset.groupBy(r => r.userId).map{case (u, rs) => u -> rs.map(r => r.itemId)}
    
    val icnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for (r <- trainset) icnt.adjustOrPut(r.itemId, 1, 1)
    
    val ipq = Map() ++ icnt.keySet.map(i => i -> new DoublePriorityQueue[Int](40))
    
//    var maxSim = 0d
//    var maxi = 0
//    var maxj = 0
    
    for ((i, ni) <- byItem) {
//      println("calc item " + i)
      val jcnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
      for {
        r <- ni
        j <- user2item(r.userId) 
        if (i < j)
      } {
        jcnt.adjustOrPut(j, 1, 1)
      }
      for ((j, cnt) <- jcnt) {
        val sim = cnt / math.sqrt(icnt(i) * icnt(j))
        ipq(i).add(sim, j)
        ipq(j).add(sim, i)
//        if (sim < 1 && maxSim < sim) {
//          maxSim = sim
//          maxi = i
//          maxj = j
//        }
      }
    }
//    println("sim =" + maxSim + ", i=" + maxi + ", j=" + maxj)
        
    val nn = ipq.map { case (i, pq) =>
      i -> pq.entries.asScala.toList.map(e => Feature(e.value, e.key))
    }
    
    println("build item-item similarity successfully")
    
    val byUser = trainset.groupBy(r => r.userId)
    Map() ++ List(10, 40).map { k =>
      val knn = nn.map{case (i, nn) => i -> nn.take(k)}
      ("ItemCF-" + k) -> new ItemCF(knn, byUser)
    }
  }
}

object ItemCF {
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
        
    val ucfb = new ItemCFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}