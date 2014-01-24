package com.nicta.scoobi.examples

import com.nicta.scoobi.Scoobi._
import com.buzzinate.dm.cassandra.CassandraDataSource
import com.buzzinate.dm.fs.CLocalFileSystem
import org.apache.hadoop.fs.FileSystem
import com.buzzinate.dm.cassandra.CassandraDataSink
import com.nicta.scoobi.application.ScoobiConfiguration

object Test extends ScoobiApp {
  
  def run() {
    val metadatas = DList.fromSource(new CassandraDataSource("crawl", "recurls"))
    val titles = metadatas.map { row =>
      val (url, cols) = row
      println(url + " ==> " + cols)
      url -> cols.toList
    }
    
    persist(new DListPersister(titles, CassandraDataSink("search", "tmp")))
  }
}