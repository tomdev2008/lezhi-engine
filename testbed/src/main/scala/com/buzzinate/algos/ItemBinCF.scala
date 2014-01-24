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

class ItemBinCF(kNN: Map[Int, List[Feature]], byUser: Map[Int, List[Rating]]) extends Recommend {
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

class ItemBinCFBuilder extends RecommendBuilder {
  def norm(usercnt: Int, itemcnt: Int): Double = {
    1 / (usercnt * math.sqrt(itemcnt))
//    1d / math.sqrt(usercnt.toDouble * itemcnt)
//    2d / (1 + math.log(usercnt) * math.log(itemcnt))
  }

  def train(trainset: List[Rating]): Map[String, Recommend] = {
    val ucnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    val icnt = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for (r <- trainset) {
      ucnt.adjustOrPut(r.userId, 1, 1)
      icnt.adjustOrPut(r.itemId, 1, 1)
    }

    val prefs = trainset.map { r =>
      (r.userId, r.itemId, norm(ucnt.getOrElse(r.userId, 0), icnt.getOrElse(r.itemId, 0)))
    }

    val u2total = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
    prefs.foreach { case (userid, itemid, pref) =>
      u2total.adjustOrPut(userid, pref, pref)
    }
    val prefs1 = prefs.map { case (userid, itemid, pref) =>
      (userid, itemid, pref - u2total.getOrElse(userid, 0d))
    }

    val i2normlen = prefs1.groupBy(x => x._2).map { case (item, ps) =>
      var sum2 = 0d
      ps foreach { case (_, _, pref) => sum2 += pref * pref }
      (item, math.sqrt(sum2))
    }

    val prefs2 = prefs1.map { case (userid, itemid, pref) =>
      (userid, itemid, pref / i2normlen.getOrElse(itemid, 0d))
    }

    val user2prefs = prefs2.groupBy(x => x._1)

    val nn = prefs2.groupBy(x => x._2).map { case (item1, ps) =>
      val j2sum = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
      ps foreach { case (userid, _, pref1) =>
        user2prefs.getOrElse(userid, Nil).foreach { case (_, item2, pref2) =>
          j2sum.adjustOrPut(item2, pref1 * pref2, pref1 * pref2)
        }
      }
      val pq = new DoublePriorityQueue[Int](10)
      j2sum.foreach { case (item2, sum) =>
        pq.add(sum, item2)
      }
      (item1, pq.entries.asScala.toList.map(e => Feature(e.value, e.key)))
    }
    println("build item-item similarity successfully")
    
    val byUser = trainset.groupBy(r => r.userId)
    Map() ++ List(10).map { k =>
      val knn = nn.map{case (i, inn) => i -> inn.take(k)}
      ("ItemBinCF-" + k) -> new ItemBinCF(knn, byUser)
    }
  }
}

object ItemBinCF {
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
        
    val ucfb = new ItemBinCFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}