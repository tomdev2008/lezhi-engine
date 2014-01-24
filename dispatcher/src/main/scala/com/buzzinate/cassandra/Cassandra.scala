package com.buzzinate.cassandra

import me.prettyprint.cassandra.service.CassandraHostConfigurator
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.hector.api.Cluster
import me.prettyprint.cassandra.serializers.StringSerializer
import com.buzzinate.dispatcher.Column

class Cassandra(clusterName: String, hosts: String, maxActive: Int = 10) {
  private val cluster = connect
  private val strserializer = StringSerializer.get

  def update(keyspace: String, columns: Seq[Column]): Unit = {
    val ks = HFactory.createKeyspace(keyspace, cluster)
    val batches = HFactory.createMutator(ks, strserializer)
    columns foreach { col =>
      val c = HFactory.createColumn(col.name, col.value, strserializer, strserializer)
      batches.addInsertion(col.row, col.columnFamily, c)
    }
    batches.execute
  }

  def close(): Unit = {
    HFactory.shutdownCluster(cluster)
  }

  private def connect(): Cluster = {
    val hostconfig = new CassandraHostConfigurator(hosts)
    hostconfig.setAutoDiscoverHosts(true)
    hostconfig.setMaxActive(maxActive)

    HFactory.getOrCreateCluster(clusterName, hostconfig)
  }
}
