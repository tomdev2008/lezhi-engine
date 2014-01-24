package com.shorrockin.cascal.test

import scala.collection.JavaConversions._
import org.apache.cassandra.config.{CFMetaData, DatabaseDescriptor, KSMetaData}
import org.apache.cassandra.db.marshal.{AbstractType, BytesType, TimeUUIDType}
import org.apache.cassandra.db.ColumnFamilyType
import org.apache.cassandra.locator.SimpleStrategy
import java.nio.ByteBuffer
import org.apache.cassandra.config.ColumnDefinition
import org.apache.cassandra.thrift.IndexType
import com.shorrockin.cascal.utils.Conversions.byteBuffer
import org.apache.cassandra.db.marshal.LongType
import org.apache.cassandra.locator.AbstractReplicationStrategy

trait Schema {
  val timeout: Int
  val keyspace: String
  val strategyClass: Class[_ <: AbstractReplicationStrategy]
  val cfMetaDatas: Seq[CFMetaData]
  
  lazy val ksMetaData = KSMetaData.testMetadataNotDurable(keyspace, strategyClass, Map("replication_factor" -> "1"), cfMetaDatas:_*)
  
  def cfMetaData(name: String, cfType: ColumnFamilyType, colType: AbstractType[_]) = {
    new CFMetaData(keyspace, name, cfType, colType, null);
  }
}