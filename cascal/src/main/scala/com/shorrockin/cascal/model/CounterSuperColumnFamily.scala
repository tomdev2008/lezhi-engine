package com.shorrockin.cascal.model

class CounterSuperColumnFamily(val value:String, val keyspace:Keyspace) extends ColumnFamily[CounterSuperKey] {
  def \(value:String) = new CounterSuperKey(value, this)
  override def toString = "%s \\\\ CounterSuperColumnFamily(value = %s)".format(keyspace.toString, value)
}