package com.buzzinate.api

import com.buzzinate.dispatcher.{Column, JsonDoc, Dispatch}
import com.twitter.util.{Throw, FuturePool, Future}
import java.util.concurrent.Executors
import com.buzzinate.cassandra.Cassandra
import com.buzzinate.es.ESClient
import com.buzzinate.util.Loggable

class DispatchImpl(es: ESClient, cass: Cassandra) extends Dispatch.FutureIface with Loggable {
  val futurePool = FuturePool(Executors.newFixedThreadPool(128))

  def bulkDocs(docs: Seq[JsonDoc] = Seq[JsonDoc]()): Future[Unit] = {
    futurePool {
      es.bulkJson(docs)
      info("bulk elasticsearc docs: " + docs.size)
    } respond {
      case Throw(t) => error(t)
      case _ =>
    }
  }

  def bulkColumns(keyspace: String, columns: Seq[Column] = Seq[Column]()): Future[Unit] = {
    futurePool {
      cass.update(keyspace, columns)
      info("bulk cassandra#" + keyspace + " columns: " + columns.size)
    } respond {
      case Throw(t) => error(t)
      case _ =>
    }
  }
}
