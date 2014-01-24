package com.buzzinate.lezhi

import com.buzzinate.crawl.Item
import com.buzzinate.lezhi.util.Config
import com.buzzinate.lezhi.util.Constants._
import com.buzzinate.lezhi.util.Loggable
import _root_.redis.clients.jedis.JedisShardInfo
import org.apache.commons.pool.impl.GenericObjectPool
import _root_.redis.clients.jedis.ShardedJedisPool
import collection.JavaConverters._
import com.buzzinate.lezhi.util.TitleExtractor
import com.buzzinate.lezhi.redis.RedisCache
import com.buzzinate.lezhi.elastic.{RichClient, RecDoc}
import com.buzzinate.crawl.FrontierClient
import com.twitter.finagle.Service
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.flume.thrift.ThriftSourceProtocol
import org.apache.thrift.protocol.TBinaryProtocol
import java.util.concurrent.Executors
import com.buzzinate.lezhi.store.HbaseTable
import com.buzzinate.lezhi.store.HTableUtil

object Servers extends Loggable {
  val prop = Config.getConfig("config.properties")
  val cacheOnly = prop.getBoolean(CACHE_ONLY, false)

  lazy val htablePool = HTableUtil.createHTablePool(prop.getString(HBASE_ZKQ_KEY), prop.getInt(HBASE_MAX_POOL_KEY, 100))
  lazy val jedisPool = createJedisPool(prop.getList(REDIS_HOSTS_KEY), 500)
  lazy val redisCache = new RedisCache(jedisPool)

  lazy val asyncPool = Executors.newFixedThreadPool(32)
  lazy val batchPool = Executors.newFixedThreadPool(32)

  lazy val client = new RichClient(prop.getString(ELASTIC_SEARCH_CLUSTER_KEY), prop.getList(ELASTIC_SEARCH_HOSTS_KEY))
  
  lazy val frontierClient = new FrontierClient(prop.getProperty("frontier.host", "localhost"))
  lazy val frontierSender = new FrontierSender(batchPool, frontierClient.client)

  lazy val thumbnailFrontierClient = new FrontierClient(prop.getProperty("thumbnail.frontier.host", "localhost"))
  
  lazy val flumeClient = createFlumeClient(prop.getProperty("flume.host", "localhost:41414"))

  val imageBlacklist = prop.getList(IMAGE_BLACK_LIST).toSet

  private def createJedisPool(hosts: List[String], maxActive: Int) = {
    val shards = hosts map { host =>
      new JedisShardInfo(host)
    }
    val poolConfig = new GenericObjectPool.Config
    poolConfig.testWhileIdle = true
    poolConfig.maxActive = maxActive
    poolConfig.timeBetweenEvictionRunsMillis = 1000L * 120
    new ShardedJedisPool(poolConfig, shards.asJava)
  }
  
  private def createFlumeClient(hosts: String): ThriftSourceProtocol.FinagledClient = {
    val service: Service[ThriftClientRequest, Array[Byte]] = ClientBuilder()
            .hosts(hosts)
            .codec(ThriftClientFramedCodec())
            .hostConnectionLimit(20)
            .build()

    new ThriftSourceProtocol.FinagledClient(service, new TBinaryProtocol.Factory())
  }
  
  def shutdown(): Unit = {
    info("System is shutting down")
    batchPool.shutdownNow
    asyncPool.shutdownNow

    htablePool.close
    jedisPool.destroy
    client.close
    frontierClient.close
    thumbnailFrontierClient.close
  }

  // 暂时强制重新抓取,等稳定了改变
  def sendQueue(item: Item): Unit = {
    frontierSender ! item
  }

  val topHbaseStore = new HbaseTable(htablePool, "search", "top")
  val mdHbaseStore = new HbaseTable(htablePool, "crawl", "metadata")
  
  def getTrending(siteprefix: String): List[RecDoc] = {
    val idurls = topHbaseStore.getRowByCount(siteprefix, 50).toList.map { case (id, url) =>
      (id.toInt, url)
    }
    // deduplicate the trending results
    val url2meta =  mdHbaseStore.getRows(idurls.map(_._2),List("rawTitle", "thumbnail", "lastModified", "statusCode")) filterNot { case (url, cols) =>
      cols.getOrElse("statusCode", "200").toInt >= 300
    } filterNot { case (url, cols) =>  // 过滤中新网403 Forbidden
      val rawTitle = cols.getOrElse("rawTitle", "")
      rawTitle.contains("403") || rawTitle.contains("Forbidden") || url == "http://bbs.chinanews.com/"
    }

    val titles = url2meta flatMap { case (url, cols) => cols.get("rawTitle") }
    val te = TitleExtractor.prepare(titles.toList)
    
    idurls flatMap { case (id, url) =>
      for {
        cols <- url2meta.get(url)
        rawTitle <- cols.get("rawTitle")
        thumbnail = cols.getOrElse("thumbnail", "")
        lastModified <- cols.get("lastModified")
      } yield (te.extract(rawTitle), id, url, rawTitle, thumbnail, lastModified.toLong)
    } groupBy(_._1) map { case (title, datas) =>
      datas.minBy { case (_, id, _, _, _, _) => id}
    } map { case (title, id, url, rawTitle, thumbnail, lastModified) =>
      RecDoc(url, rawTitle, thumbnail, lastModified, 0L, 1f)
    } toList
  }
}
