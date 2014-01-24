package com.shorrockin.cascal.session

import me.prettyprint.cassandra.service.OperationType
import org.apache.cassandra.thrift.Cassandra
import me.prettyprint.cassandra.service.ExceptionsTranslatorImpl

class HOperation[T](operationType: OperationType, f: Cassandra.Client => T) extends me.prettyprint.cassandra.service.Operation[T](operationType) {
  val xtrans = new ExceptionsTranslatorImpl
  
  override def execute(cassandra: Cassandra.Client): T  ={
    try {
      f(cassandra)
    } catch {
      case e => throw xtrans.translate(e)
    }
  }
}