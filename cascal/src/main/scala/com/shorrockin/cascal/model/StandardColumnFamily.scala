package com.shorrockin.cascal.model
import java.nio.ByteBuffer

/**
 * abstraction for the standard column family. a standard column family
 * contains a collection of keys each mapped to a collection of columns.
 *
 * @author Chris Shorrock
 */
case class StandardColumnFamily(val value:String, val keyspace:Keyspace) extends ColumnFamily[StandardKey] {
  def \(value:String) = new StandardKey(value, this)
  
  override def toString = "%s \\ StandardColumnFamily(value = %s)".format(keyspace.toString, value)
  
  import com.shorrockin.cascal.model.IndexQuery._
  def where(columnName: ByteBuffer) = indexQueryHelper(this, columnName)
}