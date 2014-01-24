package com.buzzinate.algos

import com.buzzinate.api.Recommend
import scala.collection.immutable.List
import com.buzzinate.api.Rating
import com.buzzinate.api.TopItem
import com.buzzinate.api.Feature
import scala.collection.mutable.HashMap
import com.buzzinate.util.HashMapUtil
import com.buzzinate.api.RecommendBuilder
import scala.collection.mutable.ListBuffer
import com.buzzinate.util.DoublePriorityQueue
import collection.JavaConverters._

class UserCF(kNN: Map[Int, List[Feature]], f2items: Map[Int, List[Rating]]) extends Recommend {
  def recommend(u: Int, topN: Int): List[TopItem] = { 
    val rank = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
    val myprefs = f2items.getOrElse(u, List()).map(r => r.itemId).toSet
    for {
      fs <- kNN.get(u)
      f <- fs
      r <- f2items(f.feature)
      if (!myprefs.contains(r.itemId))
    } {
      rank.adjustOrPut(r.itemId, f.score, f.score)
    }
    rank.map { case (itemId, score) =>
      TopItem(itemId, score)
    }.toList.sortBy { case TopItem(_, score) => -score}.take(topN)
  }
}

class UserCFBuilder extends RecommendBuilder {

  def train(trainset: List[Rating]): Map[String, Recommend] = {
    val byUser = trainset.groupBy(r => r.userId)
    val item2user = trainset.groupBy(r => r.itemId).map{case (i, rs) => i -> rs.map(r => r.userId)}
    
    val ucnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for (r <- trainset) ucnt.adjustOrPut(r.userId, 1, 1)
    
    val upq = Map() ++ ucnt.keySet.map(u => u -> new DoublePriorityQueue[Int](40))
    
    for ((u, nu) <- byUser) {
//      println("calc user " + u)
      val vcnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
      for {
        r <- nu
        v <- item2user(r.itemId) 
        if (u < v)
      } {
        vcnt.adjustOrPut(v, 1, 1)
      }
      for ((v, cnt) <- vcnt) {
        val sim = cnt / math.sqrt(ucnt(u) * ucnt(v))
        upq(u).add(sim, v)
        upq(v).add(sim, u)
      }
    }
    
    val nn = upq.map { case (u, pq) =>
      u -> pq.entries.asScala.toList.map(e => Feature(e.value, e.key))
    }
    
    println("build user-user similarity successfully")
    
    val f2items = trainset.groupBy(r => r.userId)
    
    Map() ++ List(5, 10, 40).map { k =>
      val knn = nn.map{case (u, nn) => u -> nn.take(k)}
      ("UserCF-" + k) -> new UserCF(knn, f2items)
    }
  }
}

object UserCF {
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
        
    val ucfb = new UserCFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}