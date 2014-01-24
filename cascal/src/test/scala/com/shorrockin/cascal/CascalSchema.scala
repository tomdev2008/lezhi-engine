package com.shorrockin.cascal

import scala.collection.JavaConversions._
import org.apache.cassandra.config.{CFMetaData, DatabaseDescriptor, KSMetaData}
import org.apache.cassandra.db.marshal.{AbstractType, BytesType, CompositeType, CounterColumnType, IntegerType, TimeUUIDType}
import org.apache.cassandra.db.ColumnFamilyType
import org.apache.cassandra.locator.SimpleStrategy
import java.nio.ByteBuffer
import org.apache.cassandra.config.ColumnDefinition
import org.apache.cassandra.thrift.IndexType
import com.shorrockin.cascal.utils.Conversions.byteBuffer
import org.apache.cassandra.db.marshal.LongType
import com.shorrockin.cascal.test.Schema

trait CascalSchema extends Schema {
  val keyspace = "Test"
  val replicationFactor = 1
  val timeout = 2000
  val strategyClass = classOf[SimpleStrategy]
  
  val colMetaData = Map[ByteBuffer, ColumnDefinition](
    byteBuffer("column1") -> new ColumnDefinition("column1", BytesType.instance, IndexType.KEYS, null, "column1Indx", 1),
    byteBuffer("longColumn") -> new ColumnDefinition("longColumn", LongType.instance, IndexType.KEYS, null, "longColumnIndx", 2))
    
  val standardIndexedCf = cfMetaData("StandardIndexed", ColumnFamilyType.Standard, BytesType.instance)
  standardIndexedCf.columnMetadata(colMetaData)
  
  val cfMetaDatas = Seq(
      cfMetaData("Standard", ColumnFamilyType.Standard, BytesType.instance),
      cfMetaData("Super", ColumnFamilyType.Super, TimeUUIDType.instance),
      cfMetaData("SuperBytes", ColumnFamilyType.Super, BytesType.instance),
      cfMetaData("StandardCounter", ColumnFamilyType.Standard, BytesType.instance).replicateOnWrite(true).defaultValidator(CounterColumnType.instance),
      cfMetaData("SuperCounter", ColumnFamilyType.Super, BytesType.instance).replicateOnWrite(true).defaultValidator(CounterColumnType.instance),
      cfMetaData("Composite2", ColumnFamilyType.Standard, CompositeType.getInstance(List(BytesType.instance, IntegerType.instance))),
      cfMetaData("Composite3", ColumnFamilyType.Standard, CompositeType.getInstance(List(BytesType.instance, LongType.instance, BytesType.instance))),
      standardIndexedCf)
}