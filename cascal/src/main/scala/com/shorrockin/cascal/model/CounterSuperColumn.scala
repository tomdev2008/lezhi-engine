package com.shorrockin.cascal.model
import java.nio.ByteBuffer
import org.apache.cassandra.thrift.ColumnParent
import org.apache.cassandra.thrift.ColumnPath
import org.apache.cassandra.thrift.ColumnOrSuperColumn
import scala.collection.JavaConversions._
import com.shorrockin.cascal.utils.Conversions

class CounterSuperColumn(val value:ByteBuffer, val key:CounterSuperKey) extends Gettable[Seq[CounterColumn[CounterSuperColumn]], ByteBuffer]
    with StandardColumnContainer[CounterColumn[CounterSuperColumn], Seq[CounterColumn[CounterSuperColumn]], Long] {
  
  def \(name:ByteBuffer) = new CounterColumn(name, this)
  def \(name:ByteBuffer, value:Long) = new CounterColumn(name, Some(value), this)

  val family = key.family
  val keyspace = family.keyspace

  lazy val columnParent = new ColumnParent(family.value).setSuper_column(value)
  lazy val columnPath = new ColumnPath(family.value).setSuper_column(value)

  def ::(other:CounterSuperColumn):List[CounterSuperColumn] = other :: this :: Nil

  /**
   * given the returned object from the get request, convert
   * to our return type.
   */
  def convertGetResult(colOrSuperCol:ColumnOrSuperColumn):Seq[CounterColumn[CounterSuperColumn]] = {
    val counterSuperCol = colOrSuperCol.getCounter_super_column
    counterSuperCol.getColumns.map { column =>
      \ (ByteBuffer.wrap(column.getName), column.getValue)
    }.toSeq
  }

  /**
   * given the return object from the list request, convert it to
   * our return type
   */
  def convertListResult(results:Seq[ColumnOrSuperColumn]):Seq[CounterColumn[CounterSuperColumn]] = {
    results.map { result =>
      val counterColumn = result.getCounter_column
      \(ByteBuffer.wrap(counterColumn.getName), counterColumn.getValue)
    }
  }

  override def toString():String = "%s \\ CounterSuperColumn(value = %s)".format(
      key.toString, Conversions.string(value))
}
