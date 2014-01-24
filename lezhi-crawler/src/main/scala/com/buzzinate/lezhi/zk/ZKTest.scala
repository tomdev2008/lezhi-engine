package com.buzzinate.lezhi.zk

import com.netflix.curator.framework.CuratorFrameworkFactory
import com.netflix.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.CreateMode
import com.netflix.curator.utils.EnsurePath

object ZKTest {
  def main(args: Array[String]): Unit = {
    val path = "/test/test2"
    val client = CuratorFrameworkFactory.newClient("192.168.1.234:2181", new ExponentialBackoffRetry(1000, 3))
    client.start
    new EnsurePath("/test").ensure(client.getZookeeperClient)
    client.create().withMode(CreateMode.EPHEMERAL).forPath(path)
    for (i <- 0 until Int.MaxValue) {
      println(client.checkExists().forPath(path))
      Thread.sleep(10000)
    }
  }
}
