package com.buzzinate.testbed

import com.buzzinate.api.Rating
import scala.collection.mutable.ListBuffer
import com.buzzinate.testbed.metric.Recall
import com.buzzinate.testbed.metric.Precision
import com.buzzinate.testbed.metric.Popularity
import com.buzzinate.testbed.metric.Coverage

import com.buzzinate.algos._
import scala.util.Random


object TestBed {
  def main(args: Array[String]): Unit = {
    val algoBuilders = List(new ItemBinCFBuilder)
    val topN = 50
    
    val metrics = List(new Recall, new Precision, new Coverage, new Popularity)
    
    val dataset = Loader.load("dataset/ml-1m/ratings.dat")//.filter(r => r.rate > 3)
    val (trainset, testset) = split(dataset, 8, 5)
    val train = trainset.groupBy(r => r.userId)
    val test = testset.groupBy(r => r.userId)
    
    for (algoBuilder <- algoBuilders) {
      for ((name, algo) <- algoBuilder.train(trainset)) {
        val result = for (metric <- metrics) yield metric.name + ": " + metric.eval(algo, train, test, topN)
        println(name + " => " + result.mkString(","))
      }
    }
  }
  
  def split2(dataset: List[Rating], M: Int): (List[Rating], List[Rating]) = {
    val train = new ListBuffer[Rating]
    val test = new ListBuffer[Rating]
    dataset.groupBy(r => r.userId).foreach { case (_, ratings) =>
      val sorted = ratings.sortBy(r => r.timestamp)
      val size = M - sorted.size / M
      train ++= sorted.take(size)
      test ++= sorted.takeRight(size)
    }
    (train.result, test.result)
  }
  
  def split(dataset: List[Rating], M: Int, k: Int = 0): (List[Rating], List[Rating]) = {
    val random = new Random
    val train = new ListBuffer[Rating]
    val test = new ListBuffer[Rating]
    var cnt = 0
    for (r <- dataset) {
      if (k == (cnt % M)) test += r
      else train += r
      cnt += 1
    }
    println("train size: " + train.size + ", test.size: " + test.size)
    (train.result, test.result)
  }
}