package com.buzzinate.minhash

import collection.JavaConverters._
import com.buzzinate.util.MurmurHash3
import com.buzzinate.util.ByteUtil
import scala.util.Random
import scala.collection.mutable.HashMap
import com.buzzinate.util.HashMapUtil
import scala.collection.mutable.ListBuffer
import com.buzzinate.util.DoublePriorityQueue

class HashFun(seed: Int) {
  def hash(text: String): Int = MurmurHash3.hash64(text, seed) & 0x7FFFFFFF
  def hash(v: Int): Int = MurmurHash3.MurmurHash3_x64_32(ByteUtil.int2bytes(v), seed) & 0x7FFFFFFF
}

case class Band(buckets: Array[String], hashes: Array[Int]) {
  override def toString(): String = {
    buckets.mkString(",") + "(" + hashes.mkString(",") + ")"
  }
}

class BandSim(bs: Array[Band]) {
  def sim(o: Array[Band]): Double = {
    var common = 0
    var bi = 0
    while (bi < bs.length && bi < o.length) {
      val Band(_, thishashes) = bs(bi)
      val Band(_, hashes) = o(bi)
      var hi = 0
      while (hi < thishashes.length && hi < hashes.length) {
        if (hashes(hi) == thishashes(hi)) common += 1
        hi += 1
      }
      bi += 1
    }
    common
  }
}

class MinhashBuilder(b: Int = 20, r: Int = 5) {
  val random = new Random(0x3c074a61)
  val hashfuns = Array.ofDim[HashFun](b * r)
  for (i <- 0 until hashfuns.length) hashfuns(i) = new HashFun(random.nextInt)
  val hashfun = new HashFun(random.nextInt)
  
  val minhashes = Array.fill(hashfuns.length)(Int.MaxValue)
  
  def add(item: String): MinhashBuilder = {
    for (i <- 0 until minhashes.length) {
      val h = hashfuns(i).hash(item)
      if (h < minhashes(i)) minhashes(i) = h
    }
    this
  }
  
  def add(item: Int): MinhashBuilder = {
    for (i <- 0 until minhashes.length) {
      val h = hashfuns(i).hash(item)
      if (h < minhashes(i)) minhashes(i) = h
    }
    this
  }
  
  def add(items: Iterable[String]): MinhashBuilder = {
    items foreach add
    this
  }
  
  def build(): Array[Band] = {
    val bands = Array.ofDim[Band](b)
    for (i <- 0 until b) {
      val subhashes = minhashes.slice(i * r, i * r + r)
      val min = subhashes.minBy(h => h)
      val max = subhashes.maxBy(h => h)
      bands(i) = Band(Array(i + "-" + min, i + "+" + max), subhashes)
    }
    bands
  }
}

class MinhashMerger(keepSize: Int) {
  val bandsbuf = new ListBuffer[Band]
  
  def add(bands: Array[Band]): Unit = {
    bandsbuf ++= bands
  }
  
  def build(): Array[(Band, Int)] = {
    val bucketCounter = new HashMap[String, Int] with HashMapUtil.IntHashMap[String]
    bandsbuf foreach { b => b.buckets.foreach { bucket => bucketCounter.adjustOrPut(bucket, 1, 1) } }
    val pq = new DoublePriorityQueue[String](keepSize)
    bucketCounter foreach { case (bucket, count) =>
      pq.add(count, bucket)
    }
    val keepbuckets = pq.values.asScala.toSet
    bandsbuf.filter(b => b.buckets.exists{ bucket => keepbuckets.contains(bucket)}).flatMap { band =>
      band.buckets.map { bucket =>
        val count = bucketCounter.getOrElse(bucket, 0)
        (band, count)
      }
    }.toArray
  }
}

object TestMinhashCluster {
  def main(args: Array[String]): Unit = {
     val urls = List(
        "http://www.gmw.cn/cg/2013-03/05/content_6896942.htm",
        "http://e.gmw.cn/2012-11/26/content_5806068.htm",
        "http://www.docin.com/p-96249765.html",
        "http://www.chexun.com/2012-02-15/100503456_1.html",
        "http://e.gmw.cn/2012-07/31/content_4677804_2.htm",
        "http://e.gmw.cn/2012-10/15/content_5366112_4.htm",
        "http://e.gmw.cn/2012-10/16/content_5381093_4.htm",
        "http://www.chinadaily.com.cn/hqcj/zgjj/2012-12-06/content_7697417.html",
        "http://www.iteye.com/topic/1128172",
        "http://www.yxad.com/sina/402318334.html",
        "http://www.yxad.com/sina/492712431.html",
        "http://www.yxad.com/sina/494061558.html",
        "http://forum.china.com.cn/thread-2392252-1-1.html",
        "http://bbs.voc.com.cn/topic-1434709-1-1.html",
        "http://www.yxad.com/sina/352546467.html",
        "http://www.yxad.com/sina/479520916.html",
        "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-13/content_7492510_12.html",
        "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-05/content_7431082.html",
        "http://www.chinadaily.com.cn/micro-reading/dzh/2012-11-02/content_7408792.html",
        "http://dota.uuu9.com/201206/88135.shtml",
        "http://dota.uuu9.com/201211/93663.shtml",
        "http://www.chinadaily.com.cn/dfpd/shehui/2012-09/11/content_15751025.htm",
        "http://www.chinadaily.com.cn/dfpd/shehui/2012-10/10/content_15805651.htm",
        "http://news.subaonet.com/sports/2012/0110/854834.shtml",
        "http://fm.m4.cn/2012-10/1186220.shtml",
        "http://news.subaonet.com/2012/0921/1002468.shtml",
        "http://yj.soufun.com/tiaozao/detail_1015510/",
        //"http://www.airm.cn/index.php/Seojs/contentSeojs?id=65", 
        "http://www.yangod.com/archives/27342",
        "http://article.woshao.com/9ad6dee6415a11e081e1000c2959fd2a/",
        "http://article.woshao.com/dd563b6cec0911e1a0aa000c291345e1/",
        "http://www.chinanews.com/mil/2012/06-15/3965152.shtml")
    
    val size = urls.size
    val v1 = urls.slice(0, size - 5).toSet
    val v2 = urls.slice(5, size).toSet
    println(sim(v1, v2))
    val b1 = new MinhashBuilder().add(v1).build
    println("v1: " + b1.flatMap(b => b.buckets).toList)
//    b1 foreach { band =>
//      println(band.bucket + " => " + band.hashes.map(Integer.toHexString).toList)
//    }
     
    val b2 = new MinhashBuilder().add(v2).build
    println("v2: " + b2.flatMap(b => b.buckets).toList)
//    b2 foreach { band =>
//      println(band.bucket + " => " + band.hashes.map(Integer.toHexString).toList)
//    }
    println(sim(b1.flatMap(b => b.buckets).toSet, b2.flatMap(b => b.buckets).toSet))
    println(new BandSim(b1).sim(b2))
  }
  
  def sim(v1: Set[String], v2: Set[String]): Double = {
    var total = v1.size + v2.size
    var common = 0
    v1.foreach { s =>
      if (v2.contains(s)) common += 1
    }
    total -= common
    if (total > 0) common / total.toDouble else 0
  }
}