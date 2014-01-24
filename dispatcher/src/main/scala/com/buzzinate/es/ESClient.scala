package com.buzzinate.es

import org.elasticsearch.client.Client
import org.elasticsearch.node.NodeBuilder
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequest
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
import java.net.{UnknownHostException, InetAddress}
import org.elasticsearch.common.UUID
import com.buzzinate.dispatcher.JsonDoc

class ESClient(cluster: String, hosts: List[String] = List()) {
  val client = buildClient(cluster, hosts)

  def state(): String = {
    client.admin().cluster().state(new ClusterStateRequest()).actionGet().state().toString
  }

  def updateTemplate(name: String, template: String): Boolean = {
    val request = new PutIndexTemplateRequest(name).source(template)
    client.admin().indices().putTemplate(request).actionGet().acknowledged
  }

  def bulkJson(docs: Seq[JsonDoc]): Unit = {
    if (docs.isEmpty) return

    val bulkReq = client.prepareBulk()
    docs foreach { doc =>
      bulkReq.add(client.prepareIndex(doc.index, doc.typo, doc.id).setSource(doc.json))
    }
    val bulkResp = bulkReq.execute().actionGet()
    if (bulkResp.hasFailures) throw new RuntimeException(bulkResp.buildFailureMessage())
  }

  def close(): Unit = client.close

  private def buildClient(cluster: String, hostlist: List[String]): Client = {
    val settings = ImmutableSettings.settingsBuilder()
          .put("node.name", nodename)
          .put("http.enabled", "false")
          .put("transport.tcp.port", "9300-9400")
          .put("discovery.zen.ping.unicast.hosts", hostlist.mkString(",")).build
    NodeBuilder.nodeBuilder().clusterName(cluster).client(true).settings(settings).node().client
  }

  private def nodename(): String = {
    val uuid = UUID.randomBase64UUID
    try { InetAddress.getLocalHost().getHostName() + "-" + uuid }
    catch {
      case e: UnknownHostException => uuid
    }
  }
}