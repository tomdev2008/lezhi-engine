package com.shorrockin.cascal.model

case class CounterStandardColumnFamily(val value:String, val keyspace:Keyspace) extends ColumnFamily[CounterStandardKey] {
  def \(value:String) = new CounterStandardKey(value, this)
  
  override def toString = "%s \\ StandardCounterColumnFamily(value = %s)".format(keyspace.toString, value)
}