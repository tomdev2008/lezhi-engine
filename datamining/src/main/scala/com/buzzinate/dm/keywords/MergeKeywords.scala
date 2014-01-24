package com.buzzinate.dm.keywords

import com.buzzinate.dm.cassandra.CassandraDataSource
import com.nicta.scoobi.Scoobi._
import com.nicta.scoobi.core.DList
import com.buzzinate.dm.cassandra.CassandraDataSink

object MergeKeywords extends ScoobiApp {
  
  def run() {
    val backupKeywords = fromTextFile("/keywords/backup").map { json =>
      val row = Row.fromJson(json)
      row.key -> row.columns.toList
    }
    
    val newKeywords = fromTextFile("/keywords/new").map { json =>
      val row = Row.fromJson(json)
      row.key -> row.columns.toList
    }
    
    val keywords = newKeywords.joinFullOuter(backupKeywords).map { case (url, (nws, bws)) =>
      url -> nws.getOrElse(bws.getOrElse(List()))
    }
    
//    persist(toTextFile(keywords, "/keywords/merge", true))
    persist(new DListPersister(keywords, CassandraDataSink("search", "keywords")))
  }
}