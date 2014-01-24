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

class ItemCFIUF(kNN: Map[Int, List[Feature]], byUser: Map[Int, List[Rating]]) extends Recommend {
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
    
    rank.map { case (itemId, score) =>
      TopItem(itemId, score)
    }.toList.sortBy { case TopItem(_, score) => -score}.take(topN)
  }
}

class ItemCFIUFBuilder extends RecommendBuilder {
  def train(trainset: List[Rating]): Map[String, Recommend] = {
    val byItem = trainset.groupBy(r => r.itemId)
    val user2item = trainset.groupBy(r => r.userId).map{case (u, rs) => u -> rs.map(r => r.itemId)}
    
    val icnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for (r <- trainset) icnt.adjustOrPut(r.itemId, 1, 1)
    
    val ucnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for (r <- trainset) ucnt.adjustOrPut(r.userId, 1, 1)
    val u2iuf = ucnt.map {case (u, cnt) => u -> 1 / math.log(1 + cnt)}
    
    val ipq = Map() ++ icnt.keySet.map(i => i -> new DoublePriorityQueue[Int](40))
    
    for ((i, ni) <- byItem) {
      println("calc item " + i)
      val jcnt = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
      for {
        r <- ni
        j <- user2item(r.userId) 
        if (i < j)
      } {
        val iuf = u2iuf(r.userId) 
        jcnt.adjustOrPut(j, iuf, iuf)
      }
      for ((j, cnt) <- jcnt) {
        val sim = cnt / math.sqrt(icnt(i) * icnt(j))
        ipq(i).add(sim, j)
        ipq(j).add(sim, i)
      }
    }
    
    val nn = ipq.map { case (i, pq) =>
      i -> pq.entries.asScala.toList.map(e => Feature(e.value, e.key))
    }
    
    println("build item-item similarity successfully")
    
    val byUser = trainset.groupBy(r => r.userId)
    Map() ++ List(5, 10, 20, 40).map { k =>
      val knn = nn.map{case (i, nn) => i -> nn.take(k)}
      ("ItemCFIUF-" + k) -> new ItemCFIUF(knn, byUser)
    }
  }
}

object ItemCFIUF {
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
        
    val ucfb = new ItemCFIUFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}