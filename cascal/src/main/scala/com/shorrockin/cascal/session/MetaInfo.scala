package com.shorrockin.cascal.session

trait MetaInfo {
  val clusterName: String
  val version: String
  val keyspaces: Seq[String]
  val keyspaceDescriptors: Set[Tuple3[String, String, String]]
}