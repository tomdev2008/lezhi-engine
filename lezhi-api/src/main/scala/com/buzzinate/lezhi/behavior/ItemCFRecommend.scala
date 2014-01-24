package com.buzzinate.lezhi.behavior

import java.lang.{Long => JLong}
import collection.JavaConverters._
import com.buzzinate.lezhi.util._
import com.buzzinate.lezhi.elastic.RecDoc
import com.buzzinate.lezhi.redis.RedisCache
import redis.clients.jedis.ShardedJedisPool
import com.buzzinate.json.RecItem
import com.google.common.collect.ArrayListMultimap
import com.alibaba.fastjson.JSON
import com.buzzinate.lezhi.render.Render
import org.apache.hadoop.hbase.client.HTablePool
import com.buzzinate.lezhi.store.HbaseTable

case class DocScore(doc: RecDoc, maxScore: Float)

class ItemCFRecommend(jedisPool: ShardedJedisPool, htablepool: HTablePool) extends Loggable {
  val userdb = new UserProfile(htablepool, 50)
  val docdb = new DocDB(htablepool)
  val topdb = new HbaseTable(htablepool, "behavior", "top")
  val rediscache = new RedisCache(jedisPool, "itemcf")

  val render = new Render(rediscache, htablepool, "crawl")

  def recommend(site: String, title: String, count: Int, metaKeywords: String): List[RecDoc] = {
    val docid = SignatureUtil.signatureTitle(title)
    val docs = rediscache.getListOrElse(Constants.keyItem(site, docid), Constants.ONE_HOUR_SECS * 10) {
      RecDoc.asJava(top(site, docid, math.max(count * 2, 20)))
    }

    RecDoc.asScala(docs) filterNot (x => SignatureUtil.signatureTitle(x.title) == docid) take(count)
  }

  def top(site: String, docid: Long, count: Int): List[RecDoc] = {
    val idscores = topdb.getRowByCount("icf-" + site + "-" + JLong.toString(docid, Character.MAX_RADIX), count).map { case (_, json) =>
      val map = JSON.parse(json).asInstanceOf[java.util.Map[String, Any]].asScala
      val id = map.get("docid").get.asInstanceOf[Number].longValue
      val score = map.get("score").get.asInstanceOf[Number].doubleValue
      (id, score.toFloat)
    }

    val docs = docdb.get(site, idscores.keys.toList).map { case (id, doc) =>
      RecDoc(doc.url, doc.title, "", doc.timestamp, 1L, idscores.getOrElse(id, 0f))
    }.toList.sortBy(d => -d.score)
    render.fillThumbnail(docs)
  }

  def recommend(site: String, userid: String, count: Int): List[RecDoc] = {
    val ids = userdb.get(site, userid)
    val keys = ids.toList map { id => Constants.keyItem(site, id)}

    val pq = new PriorityQueue[Long](2)
    val id2items = ArrayListMultimap.create[Long, DocScore]
    rediscache.batchGetCacheList[RecItem](keys) foreach { case (key, items, time) =>
      val score = - items.size * math.log(1 + time)
      pq.add(score, Constants.docid(key))

      val docs = RecDoc.asScala(items)
      val maxScore = maxByScore(docs)
      docs.foreach { rd =>
        val id = SignatureUtil.signatureTitle(rd.title)
        id2items.put(id, DocScore(rd, maxScore))
      }
    }

    // TODO: 避免每次推荐更新
    pq.values map { docid =>
      val docs = top(site, docid, math.max(count * 2, 20))

      val maxScore = maxByScore(docs)
      docs foreach { rd =>
        val id = SignatureUtil.signatureTitle(rd.title)
        if (!ids.contains(id)) id2items.put(id, DocScore(rd, maxScore))
      }
      rediscache.putCacheList(Constants.keyItem(site, docid), RecDoc.asJava(docs).asJava)
    }

    val result = new PriorityQueue[RecDoc](count)
    id2items.asMap.asScala foreach { case (_, docs) =>
      val sds = docs.asScala
      sds.headOption foreach { case DocScore(doc, _) =>
        val score = sds.map{ case DocScore(d, maxScore) => d.score / maxScore }.sum
        result.add(score, doc)
      }
    }

    result.values
  }

  @inline
  def maxByScore(docs: List[RecDoc]): Float = {
    var maxScore = 0f
    docs foreach { d =>
      if (d.score > maxScore) maxScore = d.score
    }
    maxScore
  }
}

object ItemCFRecommend {
  import com.buzzinate.lezhi.Servers

  def main(args: Array[String]): Unit = {
    val cf = new ItemCFRecommend(Servers.jedisPool, Servers.htablePool)
    cf.top("chinanews.com", SignatureUtil.signatureTitle("“第一夫人”彭丽媛罕见的54个瞬间-中新社区"), 8) foreach println
    println("####### for user")
    cf.recommend("chinanews.com", "1CHcEK3CC2hZiXpI1fg1", 8) foreach println
  }
}