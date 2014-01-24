package com.buzzinate.lezhi.util

import java.lang.{Long => JLong}
import org.apache.commons.lang.StringUtils

object Constants {
  val ELASTIC_SEARCH_CLUSTER_KEY = "elastic.search.cluster"
  val ELASTIC_SEARCH_HOSTS_KEY = "elastic.search.hosts"
  
  val HBASE_ZKQ_KEY = "hbase.zookeeper.quorum"
  val HBASE_MAX_POOL_KEY = "hbase.pool.size"
    
  val REDIS_HOSTS_KEY = "redis.hosts"
    
  val CACHE_ONLY = "cache.cacheonly"
  val RESULT_CACHE_SECONDS = "result.cache.seconds"
    
  val IMAGE_BLACK_LIST = "image.blacklist"
  
  val ONE_MINUTE_SECS = 60
  val ONE_HOUR_SECS = 3600
  val ONE_DAY_SECS = 3600 * 24
  
  // redis cache key pattern
  @inline def keyInsite(url: String) = "i-" + url
  @inline def keyTop(siteprefix: String) = "t-" + siteprefix

  @inline def keyItem(site: String, docid: Long) = "item-" + site + "-" + JLong.toString(docid, Character.MAX_RADIX)
  @inline def docid(key: String) = JLong.parseLong(StringUtils.substringAfterLast(key, "-"), Character.MAX_RADIX)
  @inline def keyPerson(userid: String, siteprefix: String) = "p-" + userid + "-" + siteprefix

  @inline def keyRecImg(url: String) = "ri-" + url

  @inline def keyRecentUserProfile(site: String, userid: String) = "rup-" + site + "-" + userid
  @inline def keyUserProfile(site: String, userid: String) = "up-" + site + "-" + userid
  @inline def keyRecentItemProfile(site: String, docid: String) = "rip-" + site + "-" + docid
  @inline def keyItemProfile(site: String, docid: String) = "ip-" + site + "-" + docid
}