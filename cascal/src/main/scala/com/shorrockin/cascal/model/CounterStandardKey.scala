package com.shorrockin.cascal.model
import java.nio.ByteBuffer
import org.apache.cassandra.thrift.ColumnOrSuperColumn

case class CounterStandardKey(val value:String, val family:CounterStandardColumnFamily)
	extends Key[CounterColumn[CounterStandardKey], Seq[CounterColumn[CounterStandardKey]]]
	with StandardColumnContainer[CounterColumn[CounterStandardKey], Seq[CounterColumn[CounterStandardKey]], Long] {

  def \(name:ByteBuffer) = new CounterColumn(name, None, this)
  def \(name:ByteBuffer, value:Long) = new CounterColumn(name, Some(value), this)

  def convertListResult(results:Seq[ColumnOrSuperColumn]):Seq[CounterColumn[CounterStandardKey]] = {
    results.map { (result) =>
      val column = result.getCounter_column
      \(ByteBuffer.wrap(column.getName), column.getValue)
    }
  }

  override def toString = "%s \\ StandardCounterKey(value = %s)".format(family.toString, value)
}
