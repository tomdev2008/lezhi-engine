package com.buzzinate.dm.cassandra

import com.nicta.scoobi.io.DataSource
import java.util.SortedMap
import java.nio.ByteBuffer
import org.apache.cassandra.db.IColumn
import org.apache.hadoop.mapreduce.InputFormat
import org.apache.hadoop.mapreduce.Job
import com.nicta.scoobi.io.InputConverter
import collection.JavaConverters._
import org.apache.cassandra.hadoop.ConfigHelper
import org.apache.cassandra.dht.RandomPartitioner
import org.apache.cassandra.thrift.SliceRange
import org.apache.cassandra.thrift.SlicePredicate
import java.util.ArrayList
import com.nicta.scoobi.application.ScoobiConfiguration
import me.prettyprint.cassandra.serializers.AbstractSerializer
import me.prettyprint.cassandra.serializers.StringSerializer
import me.prettyprint.cassandra.serializers.LongSerializer
import com.buzzinate.lezhi.cascal.ScalaLongSerializer

trait Converter[B] extends InputConverter[ByteBuffer, SortedMap[ByteBuffer, IColumn], B]

class CassandraDataSource[B](keyspace: String, columnFamily: String, columns: List[String] = List(), 
    converter: Converter[B] = CassandraInputConverter.string, 
    batchSize: Int = 512
    ) extends DataSource[ByteBuffer, SortedMap[ByteBuffer, IColumn], B] {
  val emptyBytes = ByteBuffer.wrap(new Array[Byte](0))
  
  def inputFormat: Class[_ <: InputFormat[ByteBuffer, SortedMap[ByteBuffer, IColumn]]] = classOf[ColumnFamilyInputFormat]

  def inputCheck(sc: ScoobiConfiguration) {
  }

  def inputConfigure(job: Job) = {
    val conf = job.getConfiguration()
	ConfigHelper.setInputRpcPort(conf, "9160")
	ConfigHelper.setInputColumnFamily(conf, keyspace, columnFamily)
	ConfigHelper.setInputPartitioner(conf, classOf[RandomPartitioner].getCanonicalName)
	if (columns.isEmpty) {
		val sliceRange = new SliceRange().setStart(emptyBytes).setFinish(emptyBytes)
		val predicate = new SlicePredicate().setSlice_range(sliceRange)
		ConfigHelper.setInputSlicePredicate(conf, predicate)
	} else {
		val columnnames = new ArrayList[ByteBuffer]
		for (column <- columns) columnnames.add(ByteBuffer.wrap(column.getBytes))
		val predicate = new SlicePredicate().setColumn_names(columnnames)
		ConfigHelper.setInputSlicePredicate(conf, predicate)
	}
	if (batchSize > 0) ConfigHelper.setRangeBatchSize(conf, batchSize)
  }

  def inputSize: Long = {
    // TODO something smarter here.
	1000L * 1000L * 1000L * 3
  }

  def inputConverter: InputConverter[ByteBuffer, SortedMap[ByteBuffer, IColumn], B] = {
    converter
  }
}

object CassandraDataSource {
  def apply(keyspace: String, columnFamily: String, columns: List[String], batchSize: Int) = new CassandraDataSource(keyspace, columnFamily, columns, CassandraInputConverter.string, batchSize)
}

class MaxTimestampCassandraInputConverter[CN, CV](nc: AbstractSerializer[CN] = StringSerializer.get, vc: AbstractSerializer[CV] = StringSerializer.get) extends Converter[(String, Map[CN, CV], Long)] {
  def fromKeyValue(context: InputContext, key: ByteBuffer, value: SortedMap[ByteBuffer, IColumn]): (String, Map[CN, CV], Long) = {
    var maxTimestamp = 0L
    val kvs = for ((ck, cv) <- value.asScala) yield {
      if (maxTimestamp < cv.timestamp) maxTimestamp = cv.timestamp
      nc.fromByteBuffer(ck) -> vc.fromByteBuffer(cv.value)
    }
    val row = StringSerializer.get.fromByteBuffer(key.asReadOnlyBuffer)
    (row, kvs.toMap, maxTimestamp)
  }
}

object MaxTimestampCassandraInputConverter {
  val string = new MaxTimestampCassandraInputConverter(StringSerializer.get, StringSerializer.get)
}

class CassandraInputConverter[CN, CV](nc: AbstractSerializer[CN] = StringSerializer.get, vc: AbstractSerializer[CV] = StringSerializer.get) extends Converter[(String, Map[CN, CV])] {
  def fromKeyValue(context: InputContext, key: ByteBuffer, value: SortedMap[ByteBuffer, IColumn]): (String, Map[CN, CV]) = {
    val kvs = for ((ck, cv) <- value.asScala) yield {
      nc.fromByteBuffer(ck) -> vc.fromByteBuffer(cv.value)
    }
    val row = StringSerializer.get.fromByteBuffer(key.asReadOnlyBuffer)
    row -> kvs.toMap
  }
}

object CassandraInputConverter {
  val string = new CassandraInputConverter(StringSerializer.get, StringSerializer.get)
  val counter = new CassandraInputConverter(StringSerializer.get, ScalaLongSerializer)
}