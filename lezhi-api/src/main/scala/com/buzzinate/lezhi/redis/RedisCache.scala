package com.buzzinate.lezhi.redis

import collection.JavaConverters._
import redis.clients.jedis.ShardedJedisPool
import com.buzzinate.lezhi.util.Loggable
import redis.clients.jedis.ShardedJedis
import com.buzzinate.redis.JEntry
import com.buzzinate.redis.JEntryList
import com.buzzinate.lezhi.async.Async
import com.buzzinate.lezhi.util.Constants
import com.buzzinate.lezhi.Servers

class RedisCache(pool: ShardedJedisPool, name: String = "default") extends Loggable {
  val asnyc = new Async(Servers.asyncPool)
  
  def getOrElse[T](key: String, expireInSecs: Int)(fun: => T)(implicit mt: Manifest[T]): T = {
    getCache(key).map { case (value, time) =>
      // TODO: 更好的过期更新机制
      if (System.currentTimeMillis - time > expireInSecs * 1000) {
        asnyc.asnyc(key) {
          val newvalue = fun
          putCache(key, newvalue)
        }
      }
      value
    }.getOrElse {
      val value = fun
      putCache(key, value)
      value
    }
  }
  
  def getListOrElse[T](key: String, expireInSecs: Int)(fun: => List[T])(implicit mt: Manifest[T]): List[T] = {
    getCacheList(key).map { case (values, time) =>
      // TODO: 更好的过期更新机制
      if (System.currentTimeMillis - time > expireInSecs * 1000) {
        asnyc.asnyc(key) {
          val newvalues = fun
          putCacheList(key, newvalues.asJava)
        }
      }
      values.asScala.toList
    }.getOrElse {
      val values = fun
      putCacheList(key, values.asJava)
      values
    }
  }

  def getListOrElseAsnyc[T](key: String, expireInSecs: Int)(fun: => List[T])(implicit mt: Manifest[T]): List[T] = {
    getCacheList(key).map { case (values, time) =>
      if (System.currentTimeMillis - time > expireInSecs * 1000) {
        asnyc.asnyc(key) {
          val newvalues = fun
          putCacheList(key, newvalues.asJava)
        }
      }
      values.asScala.toList
    }.getOrElse {
      asnyc.asnyc(key) {
        val values = fun
        putCacheList(key, values.asJava)
      }
      List()
    }
  }
  
  def getCache[T](key: String)(implicit mt: Manifest[T]): Option[(T, Long)] = {
    val json = RedisCache.use(pool) { jedis => jedis.get(key) }
    if (json == null || json == "nil") None
    else {
      val e = JEntry.parseJson(json, mt.erasure).asInstanceOf[JEntry[T]]
      Some(e.value -> e.cacheTime)
    }
  }
  
  def putCache[T](key: String, value: T)(implicit mt: Manifest[T]): Unit = {
    val e = new JEntry(value)
    val json = e.toJson
    RedisCache.use(pool) { jedis => 
      jedis.set(key, json)
      jedis.expire(key, Constants.ONE_DAY_SECS * 5)
    }
  }
  
   def putCache[T](key: String, value: T,seconds:Int)(implicit mt: Manifest[T]): Unit = {
    val e = new JEntry(value)
    val json = e.toJson
    RedisCache.use(pool) { jedis => 
      jedis.set(key, json)
      jedis.expire(key,seconds)
    }
  }
  
  
  def getCacheList[T](key: String)(implicit mt: Manifest[T]): Option[(java.util.List[T], Long)] = {
    val json = RedisCache.use(pool) { jedis => jedis.get(key) }
    if (json == null || "nil".equals(json)) None
    else {
      val e = JEntryList.parseJson(json, mt.erasure).asInstanceOf[JEntryList[T]]
      Some(e.values -> e.cacheTime)
    }
  }
  
  def putCacheList[T](key: String, values: java.util.List[T])(implicit mt: Manifest[T]): Unit = {
    val e = new JEntryList(values)
    val json = e.toJson
    RedisCache.use(pool) { jedis => 
      jedis.set(key, json)
      jedis.expire(key, Constants.ONE_DAY_SECS * 5)
    }
  }
  
  def batchGetCacheList[T](keys: List[String])(implicit mt: Manifest[T]): List[(String, List[T], Long)] = {
    val key2jsons = RedisCache.use(pool) { jedis =>
      val p = jedis.pipelined
      val rs = keys map { key =>
        key -> p.get(key)
      }
      p.sync
      
      rs map { case (key, res) =>
        key -> res.get
      }
    }
    key2jsons map { case (key, json) =>
      if (json == null || json == "nil") (key, List[T](), 0L)
      else {
        val e = JEntryList.parseJson(json, mt.erasure).asInstanceOf[JEntryList[T]]
        (key, e.values.asScala.toList, e.cacheTime)
      }
    }
  }
}

object RedisCache {
  def use[T](pool: ShardedJedisPool)(f: ShardedJedis => T): T = {
    var returned = false
    val jedis = pool.getResource
    try {
      f(jedis)
    } catch {
      case e => {
        pool.returnBrokenResource(jedis)
        returned = true
        throw e
      }
    } finally {
      if (!returned) pool.returnResource(jedis)
    }
  }
}