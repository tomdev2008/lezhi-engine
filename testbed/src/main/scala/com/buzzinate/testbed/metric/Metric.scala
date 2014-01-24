package com.buzzinate.testbed.metric

import com.buzzinate.api.Rating
import com.buzzinate.api.Recommend
import com.buzzinate.api.TopItem
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap
import com.buzzinate.util.HashMapUtil

trait Metric {
  val name: String = getClass.getSimpleName
  def eval(algo: Recommend, train: Map[Int, List[Rating]], test: Map[Int, List[Rating]], topN: Int): Double
}

class Recall extends Metric {
  def eval(algo: Recommend, train: Map[Int, List[Rating]], test: Map[Int, List[Rating]], topN: Int): Double = {
    var hit = 0
    var all = 0d
    for (u <- train.keys) {
      val tu = test.getOrElse(u, Nil).map(r => r.itemId).toSet
      val rank = algo.recommend(u, topN)
      for (TopItem(itemId, score) <- rank) {
        if (tu.contains(itemId)) hit += 1
      }
      all += tu.size
    }
    hit / all
  }
  
//  def eval[A](recs: List[A], test: Map[A, Int]): Double = {
//    var hit = 0;
//    recs.foreach{rec =>
//      if(test.contains(rec)) hit +=1}
//    hit / test.size.toDouble
//  }
  
}

class Precision extends Metric {
  def eval(algo: Recommend, train: Map[Int, List[Rating]], test: Map[Int, List[Rating]], topN: Int): Double = {
    var hit = 0
    var all = 0d
    
    for (u <- train.keys) {
      val tu = test.getOrElse(u, Nil).map(r => r.itemId).toSet
      val rank = algo.recommend(u, topN)
      for (TopItem(itemId, score) <- rank) {
        if (tu.contains(itemId)) {
          hit += 1
        }
      }
      all += rank.length
    }
    hit / all
  }
  
  def eval[A](recs: List[A], test: Map[A, Int]): Double = {
    var hit = 0;
    recs.foreach{rec =>
      if(test.contains(rec)) hit +=1}
    hit / recs.size.toDouble
  }
}

class Coverage extends Metric {
  def eval(algo: Recommend, train: Map[Int, List[Rating]], test: Map[Int, List[Rating]], topN: Int): Double = {
    val recitemset = new HashSet[Int]()
    val allitemset = train.values.flatten.map(r => r.itemId).toSet
    
    for (u <- train.keys) {
      val rank = algo.recommend(u, topN)
      for (TopItem(itemId, score) <- rank) recitemset += itemId
    }
    recitemset.size / allitemset.size.toDouble
  }
}

class Popularity extends Metric {
  def eval(algo: Recommend, train: Map[Int, List[Rating]], test: Map[Int, List[Rating]], topN: Int): Double = {
    val itemPopularity = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
    for (r <- train.values.flatten) itemPopularity.adjustOrPut(r.itemId, 1, 1)
    
    var ret = 0d
    var n = 0
    for (u <- train.keys) {
      val rank = algo.recommend(u, topN)
      for (TopItem(itemId, score) <- rank) {
        ret += math.log(1 + itemPopularity.getOrElse(itemId, 0))
        n += 1
      }
    }
    ret / n
  }
}

object Metric {
}