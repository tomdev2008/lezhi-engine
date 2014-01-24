package com.buzzinate.dm.cassandra

import com.nicta.scoobi.io.DataSink
import java.nio.ByteBuffer
import org.apache.cassandra.thrift.Mutation
import org.apache.hadoop.mapreduce.OutputFormat
import com.nicta.scoobi.application.ScoobiConfiguration
import org.apache.hadoop.mapreduce.Job
import collection.JavaConverters._
import com.nicta.scoobi.io.OutputConverter
import org.apache.cassandra.hadoop.ConfigHelper
import org.apache.cassandra.dht.RandomPartitioner
import scala.collection.mutable.ListBuffer
import org.apache.cassandra.thrift.Column
import org.apache.cassandra.thrift.ColumnOrSuperColumn
import org.apache.cassandra.thrift.CounterColumn
import org.apache.cassandra.thrift.SuperColumn
import me.prettyprint.cassandra.serializers.StringSerializer
import com.shorrockin.cascal.utils.Utils

class CassandraDataSink[B](keyspace: String, columnFamily: String, c: OutputConverter[ByteBuffer, java.util.List[Mutation], B]) extends DataSink[ByteBuffer, java.util.List[Mutation], B] {
  def outputFormat: Class[_ <: OutputFormat[ByteBuffer, java.util.List[Mutation]]] = classOf[ColumnFamilyOutputFormat]
  def outputKeyClass: Class[ByteBuffer] = classOf[ByteBuffer]
  def outputValueClass: Class[java.util.List[Mutation]] = classOf[java.util.List[Mutation]]

  def outputCheck(sc: ScoobiConfiguration) = {    
  }

  def outputConfigure(job: Job) = {
    val conf = job.getConfiguration
	ConfigHelper.setOutputPartitioner(conf, classOf[RandomPartitioner].getCanonicalName)
	ConfigHelper.setOutputRpcPort(conf, "9160")
	ConfigHelper.setOutputColumnFamily(conf, keyspace, columnFamily)
  }

  /** Maps the type consumed by this DataSink to the key-values of its OutputFormat. */
  def outputConverter: OutputConverter[ByteBuffer, java.util.List[Mutation], B] = {
    c
  }
}

object CassandraDataSink {
  def apply(keyspace: String, columnFamily: String) = new CassandraDataSink(keyspace, columnFamily, new CassandraOutputConverter)
  def counter(keyspace: String, columnFamily: String) = new CassandraDataSink(keyspace, columnFamily, new CassandraCounterOutputConverter)
  def superColumn(keyspace: String, columnFamily: String) = new CassandraDataSink(keyspace, columnFamily, new CassandraSuperOutputConverter)
}

class CassandraOutputConverter extends OutputConverter[ByteBuffer, java.util.List[Mutation], (String, List[(String, String)])] {
  def toKeyValue(x: (String, List[(String, String)])): (ByteBuffer, java.util.List[Mutation]) = {
    val (k, cnvs) = x
    val key = StringSerializer.get.toByteBuffer(k)
    
    val now = System.currentTimeMillis() * 1000
    val mutations = cnvs.map { cnv =>
      val (cn, cv) = cnv
      val c = new Column
	  c.setName(StringSerializer.get.toByteBuffer(cn))
	  c.setValue(StringSerializer.get.toByteBuffer(cv))
	  c.setTimestamp(now)
	      	
	  val mu = new Mutation
	  mu.setColumn_or_supercolumn(new ColumnOrSuperColumn().setColumn(c))
    }
    (key, mutations.asJava)
  }
}

class CassandraCounterOutputConverter extends OutputConverter[ByteBuffer, java.util.List[Mutation], (String, List[(String, Long)])] {
  def toKeyValue(x: (String, List[(String, Long)])): (ByteBuffer, java.util.List[Mutation]) = {
    val (k, cnvs) = x
    val key = StringSerializer.get.toByteBuffer(k)
    
    val mutations = cnvs.map { cnv =>
      val (cn, cv) = cnv
      val c = new CounterColumn
	  c.setName(StringSerializer.get.toByteBuffer(cn))
	  c.setValue(cv)
	      	
	  val mu = new Mutation
	  mu.setColumn_or_supercolumn(new ColumnOrSuperColumn().setCounter_column(c))
    }
    (key, mutations.asJava)
  }
}

class CassandraSuperOutputConverter extends OutputConverter[ByteBuffer, java.util.List[Mutation], (String, List[(String, ByteBuffer, String)])] {
  def toKeyValue(x: (String, List[(String, ByteBuffer, String)])): (ByteBuffer, java.util.List[Mutation]) = {
    val (k, cnvs) = x
    val key = StringSerializer.get.toByteBuffer(k)
    
    val now = Utils.now
    val mutations = cnvs.map { cnv =>
      val (s, cn, cv) = cnv
      val sc = new SuperColumn
	  sc.setName(StringSerializer.get.toByteBuffer(s))
	  
	  val c = new Column
	  c.setName(cn)
	  c.setValue(StringSerializer.get.toByteBuffer(cv))
	  c.setTimestamp(now)
	  sc.setColumns(List(c).asJava)
	  
	  val mu = new Mutation
	  mu.setColumn_or_supercolumn(new ColumnOrSuperColumn().setSuper_column(sc))
    }
    (key, mutations.asJava)
  }
}