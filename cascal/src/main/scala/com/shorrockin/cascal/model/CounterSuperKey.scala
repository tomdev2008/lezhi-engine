package com.shorrockin.cascal.model

import java.nio.ByteBuffer
import org.apache.cassandra.thrift.ColumnOrSuperColumn
import scala.collection.JavaConversions._

class CounterSuperKey(val value:String, val family:CounterSuperColumnFamily)
	extends Key[CounterSuperColumn, Seq[(CounterSuperColumn, Seq[CounterColumn[CounterSuperColumn]])]] {

  def \(value:ByteBuffer) = new CounterSuperColumn(value, this)

  /**
   *  converts a list of super columns to the specified return type
   */
  def convertListResult(results: Seq[ColumnOrSuperColumn]): Seq[(CounterSuperColumn, Seq[CounterColumn[CounterSuperColumn]])] = {
    results.map { result =>
      val nativeCounterSuperCol = result.getCounter_super_column
      val counterSuperColumn = this \ ByteBuffer.wrap(nativeCounterSuperCol.getName)
      val columns = nativeCounterSuperCol.getColumns.map { column =>
        counterSuperColumn \ (ByteBuffer.wrap(column.getName), column.getValue)
      }
      (counterSuperColumn -> columns)
    }
  }

  override def toString = "%s \\ CounterSuperKey(value = %s)".format(family.toString, value)
}
