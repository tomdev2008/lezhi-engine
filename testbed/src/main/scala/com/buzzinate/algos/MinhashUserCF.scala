package com.buzzinate.algos

import com.buzzinate.api.Recommend
import scala.collection.immutable.List
import com.buzzinate.api.Rating
import com.buzzinate.api.TopItem
import scala.collection.mutable.HashMap
import com.buzzinate.util.{MurmurHash3, HashMapUtil, DoublePriorityQueue, ByteUtil}
import com.buzzinate.api.RecommendBuilder
import collection.JavaConverters._
import com.buzzinate.minhash.MinhashBuilder
import com.buzzinate.minhash.BandSim

case class RecUser(userid: Int, score: Double)

class MinhashUserCF(kNN: Map[Int, List[RecUser]], f2items: Map[Int, List[Rating]]) extends Recommend {
  def recommend(u: Int, topN: Int): List[TopItem] = { 
    val rank = new HashMap[Int, Double] with HashMapUtil.DoubleHashMap[Int]
    val myprefs = f2items.getOrElse(u, List()).map(r => r.itemId).toSet
    
    for {
      fs <- kNN.get(u)
      RecUser(u, s) <- fs
      r <- f2items(u)
      if (!myprefs.contains(r.itemId))
    } {
      rank.adjustOrPut(r.itemId, s, s)
    }
    
    rank.map { case (itemId, score) =>
      TopItem(itemId, score)
    }.toList.sortBy { case TopItem(_, score) => -score}.take(topN)
  }
}

class MinhashUserCFBuilder extends RecommendBuilder {

  def train(trainset: List[Rating]): Map[String, Recommend] = {
    val user2mh = trainset.groupBy(r => r.userId) map { case (userid, rs) =>
      val mhb = new MinhashBuilder(20, 5)
      rs foreach { r =>
        mhb.add(r.itemId)
      }
//      println("#### " + userid + " => " + mhb.build.map(x => x.bucket).toList)
      userid -> mhb.build
    }
    
    val bucket2users = user2mh.toList.flatMap { case (userid, bands) =>
      bands.flatMap { band =>
        band.buckets.map(bucket => bucket -> userid)
      }
    }.groupBy(x => x._1).map { case (bucket, xs) =>
      bucket -> xs.map(x => x._2).toSet
    }
    
    val user2f = user2mh.map { case (userid, bands) =>
      val rank = new HashMap[Int, Int] with HashMapUtil.IntHashMap[Int]
      for {
        thisband <- bands
        bucket <- thisband.buckets
        neighbors <- bucket2users.get(bucket)
        neighbor <- neighbors
        if (userid != neighbor)
      } {
        rank.adjustOrPut(neighbor, 1, 1)
      }
      val rpq = new DoublePriorityQueue[Int](40 * 2)
      for ((neighbor, count) <- rank) rpq.add(count, neighbor)
      
      val bandsim = new BandSim(bands)
      val pq = new DoublePriorityQueue[RecUser](40)
      for {
        otheruser <- rpq.values.asScala
        otherbands <- user2mh.get(otheruser)
      } {
        val score = bandsim.sim(otherbands)
//        println("### " + userid + "<->" + otheruser + " => " + score)
        pq.add(score, RecUser(otheruser, score))
      }
      
      println(Integer.toHexString(userid) + " ===> " + pq.values.asScala.map(x => Integer.toHexString(x.userid) + "/" + x.score).mkString(","))
      userid -> pq.values.asScala.toList
    }
    
    val byUser = trainset.groupBy(r => r.userId).map { case (userid, rs) =>
      userid -> rs.map(r => r.itemId).toSet
    }
//    println(nn)
    println("build user-user similarity successfully")
    
    val f2items = trainset.groupBy(r => r.userId)
    
    Map() ++ List(10, 40).map { k =>
      val knn = user2f.map{case (u, fs) => u -> fs.take(k)}
      ("MinhashUserCF-" + k) -> new MinhashUserCF(knn, f2items)
    }
  }
  
  def hash(v: Int): Int = {
    val bs = ByteUtil.int2bytes(v)
    MurmurHash3.MurmurHash3_x64_32(bs, 0x1234ABCD) & Int.MaxValue
  }
}

object MinhashUserCF {
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
        
    val ucfb = new MinhashUserCFBuilder
    val (name, algo) = ucfb.train(train).head
    val rank = algo.recommend(1, 5)
    println(rank)
  }
}