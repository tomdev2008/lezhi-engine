package com.buzzinate.dm.keywords

import com.buzzinate.dm.cassandra.CassandraDataSource
import com.nicta.scoobi.Scoobi.ScoobiApp
import com.nicta.scoobi.Scoobi.StringFmt
import com.nicta.scoobi.Scoobi.Tuple2Fmt
import com.nicta.scoobi.Scoobi.persist
import com.nicta.scoobi.Scoobi.toTextFile
import com.nicta.scoobi.core.DList

object BackupKeywords extends ScoobiApp {
  
  def run() {
    val keywords = DList.fromSource(new CassandraDataSource("search", "keywords")).map { case (url, cols) =>
      val row = Row(url, cols)
      Row.toJson(row)
    }
    
    persist(toTextFile(keywords, "/keywords/backup", true))
  }
}