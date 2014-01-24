package com.shorrockin.cascal.session

import org.apache.commons.pool.PoolableObjectFactory
import org.apache.commons.pool.impl.{GenericObjectPoolFactory, GenericObjectPool}
import com.shorrockin.cascal.utils.Logging
import com.shorrockin.cascal.model._
import scala.collection.Map
import me.prettyprint.cassandra.service.CassandraHostConfigurator
import collection.JavaConverters._
import me.prettyprint.cassandra.service.OperationType
import org.apache.cassandra.thrift.Cassandra
import me.prettyprint.cassandra.service.ExceptionsTranslatorImpl
import scala.collection.mutable.HashSet
import me.prettyprint.cassandra.connection.factory.HClientFactoryProvider
import me.prettyprint.cassandra.connection.HConnectionManager
import me.prettyprint.hector.api.factory.HFactory
import me.prettyprint.cassandra.connection.DynamicLoadBalancingPolicy
import me.prettyprint.cassandra.service.FailoverPolicy
import me.prettyprint.cassandra.connection.client.HThriftClient

/**
 * a session pool which maintains a collection of open sessions so that
 * we can avoid the overhead of creating a new tcp connection every time
 * something is need.
 *
 * session pool is also an instance of a session template - when used in
 * this fashion each invocation to the sessiontemplate method will invoke
 * a borrow method and an execution of the requested method against the
 * session returned.
 *
 * @author Chris Shorrock
 */
class SessionPool(hosts: String, maxActive: Int, val defaultConsistency:Consistency) extends SessionTemplate with MetaInfo {
   private val hostconfig = new CassandraHostConfigurator(hosts)
   hostconfig.setAutoDiscoverHosts(true)
   hostconfig.setMaxActive(maxActive)
//   hostconfig.setLoadBalancingPolicy(new DynamicLoadBalancingPolicy)
   
   private val cluster = HFactory.getOrCreateCluster(describeClusterName, hostconfig)
  
  /**
   * closes this pool and releases any resources available to it.
   */
  def close() {
    HFactory.shutdownCluster(cluster)
  }
   
  private def describeClusterName(): String = {
    val client = new HThriftClient(new CassandraHostConfigurator(hosts).buildCassandraHosts.head)
    try {
      client.open
      client.getCassandra.describe_cluster_name
    } catch {
      case e => ""
    } finally {
      client.close
    }
  }

  def status(): List[String] = cluster.getConnectionManager.getStatusPerPool.asScala.toList
  
  lazy val clusterName = exec(OperationType.META_READ) { client => client.describe_cluster_name }
  
  lazy val version = exec(OperationType.META_READ) { client => client.describe_version }
  
  lazy val keyspaces: Seq[String] = exec(OperationType.META_READ) { client => 
    client.describe_keyspaces.asScala map { _.name } 
  }
  
  lazy val keyspaceDescriptors: Set[Tuple3[String, String, String]] = exec(OperationType.META_READ) { client => 
    var keyspaceDesc: HashSet[Tuple3[String, String, String]] = new HashSet[Tuple3[String, String, String]]
    client.describe_keyspaces.asScala foreach {
      space =>
        val familyMap = space.cf_defs
        familyMap.asScala foreach {
          family =>
            keyspaceDesc = keyspaceDesc + ((space.name, family.name, family.column_type))
            ()
        }
    }
    keyspaceDesc.toSet
  }

  /**
   * used to retrieve a session and perform a function using that
   * function. This function will clean up the borrowed object after
   * it has finished. You do not need to manually call "return"
   */
  def borrow[E](opType: OperationType)(f:(Session) => E):E = {
    exec(opType) { client =>
      f(new Session(client, this, defaultConsistency))
    }
  }

  def truncate(cfname: String) = borrow(OperationType.META_WRITE){ _.truncate(cfname) }
  
  def get[ResultType, ValueType](col:Gettable[ResultType, ValueType], consistency:Consistency):Option[ResultType] = borrow(OperationType.READ) { _.get(col, consistency) }

  def get[ResultType, ValueType](col:Gettable[ResultType, ValueType]):Option[ResultType] = borrow(OperationType.READ) { _.get(col) }

  def insert[E](col:Column[E], consistency:Consistency):Column[E] = borrow(OperationType.WRITE) { _.insert(col, consistency) }

  def insert[E](col:Column[E]):Column[E] = borrow(OperationType.WRITE) { _.insert(col) }

  def add[E](col: CounterColumn[E], consistency: Consistency = defaultConsistency) = borrow(OperationType.WRITE) { _.add(col, consistency) }
  
  def count(container:ColumnContainer[_ ,_], consistency:Consistency):Int = borrow(OperationType.READ) { _.count(container, consistency) }

  def count(container:ColumnContainer[_, _]):Int = borrow(OperationType.READ) { _.count(container) }

  def count[ColumnType, ResultType](containers: Seq[ColumnContainer[ColumnType, ResultType]], predicate:Predicate = EmptyPredicate, consistency: Consistency = defaultConsistency): Map[ColumnContainer[ColumnType, ResultType], Int] = borrow((OperationType.READ)) { _.count(containers, predicate, consistency) }
  
  def remove(container:ColumnContainer[_, _], consistency:Consistency):Unit = borrow(OperationType.WRITE) { _.remove(container, consistency) }

  def remove(container:ColumnContainer[_, _]):Unit = borrow(OperationType.WRITE) { _.remove(container) }

  def remove(column:Column[_], consistency:Consistency):Unit = borrow(OperationType.WRITE) { _.remove(column, consistency) }

  def remove(column:Column[_]):Unit = borrow(OperationType.WRITE) { _.remove(column) }

  /**
   * removes the specified column container
   */
  def remove(column: CounterColumn[_], consistency: Consistency = defaultConsistency): Unit = borrow(OperationType.WRITE) { _.remove(column, consistency) }

  
  def list[ResultType](container:ColumnContainer[_, ResultType], predicate:Predicate, consistency:Consistency):ResultType = borrow(OperationType.READ) { _.list(container, predicate, consistency) }

  def list[ResultType](container:ColumnContainer[_, ResultType]):ResultType = borrow(OperationType.READ) { _.list(container) }

  def list[ResultType](container:ColumnContainer[_, ResultType], predicate:Predicate):ResultType = borrow(OperationType.READ) { _.list(container, predicate) }

  def list[ColumnType, ResultType](containers:Seq[ColumnContainer[ColumnType, ResultType]], predicate:Predicate, consistency:Consistency):Seq[(ColumnContainer[ColumnType, ResultType], ResultType)] = borrow(OperationType.READ) { _.list(containers, predicate, consistency) }

  def list[ColumnType, ResultType](containers:Seq[ColumnContainer[ColumnType, ResultType]]):Seq[(ColumnContainer[ColumnType, ResultType], ResultType)] = borrow(OperationType.READ) { _.list(containers) }

  def list[ColumnType, ResultType](containers:Seq[ColumnContainer[ColumnType, ResultType]], predicate:Predicate):Seq[(ColumnContainer[ColumnType, ResultType], ResultType)] = borrow(OperationType.READ) { _.list(containers, predicate) }

  def list[ColumnType, ListType](family:ColumnFamily[Key[ColumnType, ListType]], range:KeyRange, predicate:Predicate, consistency:Consistency):Map[Key[ColumnType, ListType], ListType] = borrow(OperationType.READ) { _.list(family, range, predicate, consistency) }

  def list[ColumnType, ListType](family:ColumnFamily[Key[ColumnType, ListType]], range:KeyRange, consistency:Consistency):Map[Key[ColumnType, ListType], ListType] = borrow(OperationType.READ) { _.list(family, range, consistency) }

  def list[ColumnType, ListType](family:ColumnFamily[Key[ColumnType, ListType]], range:KeyRange):Map[Key[ColumnType, ListType], ListType] = borrow(OperationType.READ) { _.list(family, range) }

  def list[ColumnType, ListType](query: IndexQuery): Map[StandardKey, Seq[Column[StandardKey]]] = borrow(OperationType.READ) { _.list(query) }
  
  def list[ColumnType, ListType](query: IndexQuery, predicate: Predicate, consistency: Consistency): Map[StandardKey, Seq[Column[StandardKey]]] = borrow(OperationType.READ) { _.list(query, predicate, consistency) }
    
  def batch(ops:Seq[Operation], consistency:Consistency):Unit = borrow(OperationType.WRITE) { _.batch(ops, consistency) }

  def batch(ops:Seq[Operation]):Unit = borrow(OperationType.WRITE) { _.batch(ops) }

  def batchWithRetry(ops: Seq[Operation], consistency: Consistency): Unit = borrow(OperationType.WRITE) { _.batchWithRetry(ops, consistency) }

  def batchWithRetry(ops: Seq[Operation]): Unit = borrow(OperationType.WRITE) { _.batchWithRetry(ops) }
  
  def exec[T](opType: OperationType)(f: Cassandra.Client => T): T = {
    val op = new HOperation(opType, f)
    if (opType == OperationType.READ) op.failoverPolicy = FailoverPolicy.ON_FAIL_TRY_ONE_NEXT_AVAILABLE
    cluster.getConnectionManager.operateWithFailover(op)
    op.getResult
  }
}