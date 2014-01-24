package com.buzzinate.lezhi.crawl

import com.buzzinate.lezhi.config.Constants._

class Vars(conf: Map[String, String]) extends Serializable {
  val elasticsearchHosts = conf.getOrElse(ELASTIC_SEARCH_HOSTS_KEY, "localhost").split(",").map(_.trim).toList
  
  val hbaseZookeeperQuorum = conf.getOrElse(HBASE_ZOOKEEPER_QUORUM, "localhost")
  
  val crawlTable = conf.getOrElse(HBASE_CRAWL_TABLE, "crawl")
  val searchTable = conf.getOrElse(HBASE_SEARCH_TABLE, "search")
  val articlePatterns = conf.getOrElse(ARTICLE_PATTERNS_KEY, "").split(",").map(_.trim()).toList

  val DEFAULT_KEYWORD_WEIGHT = 1.0
  val MAX_META_KEYWORD_LENGTH = 5
}