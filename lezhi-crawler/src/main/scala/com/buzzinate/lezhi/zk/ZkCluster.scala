package com.buzzinate.lezhi.zk

import java.net.InetAddress
import org.apache.zookeeper.CreateMode
import com.buzzinate.lezhi.util.Loggable
import com.netflix.curator.framework.state.ConnectionState
import com.netflix.curator.framework.state.ConnectionStateListener
import com.netflix.curator.framework.CuratorFramework
import com.netflix.curator.framework.CuratorFrameworkFactory
import com.netflix.curator.retry.ExponentialBackoffRetry
import org.apache.zookeeper.KeeperException

class ZkCluster(zkconn: String, path: String) extends Cluster with Loggable {

  def id(): String = ZkCluster.nodeName()

  def join(): Unit = {
    val connectionStateListener = new ConnectionStateListener() {
      override def stateChanged(client: CuratorFramework, newState: ConnectionState) {
        if (newState == ConnectionState.RECONNECTED) {
          try {
            warn("Re-registering due to reconnection");
            ZkCluster.join(zkconn, path, client)
          } catch {
            case e => error("Could not re-register instances after reconnection", e)
          }
        }
      }
    }

    val client = CuratorFrameworkFactory.newClient(zkconn, new ExponentialBackoffRetry(1000, 3))
    client.getConnectionStateListenable().addListener(connectionStateListener)
    client.start
    ZkCluster.join(zkconn, path, client)
  }
}

object ZkCluster extends Loggable {
  def nodeName(): String = InetAddress.getLocalHost.getHostName

  def join(zkconn: String, path: String, client: CuratorFramework): Unit = {

    try {
      info("create " + path)
      client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);

    } catch {
      case e: KeeperException.NodeExistsException => {
        warn(path + " exists!")
        try {
          client.delete().forPath(path); // must delete then re-create so that watchers fire
          client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch {
          case e => error("second create fail!")
        }
      }
    }

  }
}
