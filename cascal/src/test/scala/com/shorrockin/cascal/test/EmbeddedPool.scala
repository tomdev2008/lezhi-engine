package com.shorrockin.cascal.test

import org.apache.cassandra.thrift.CassandraDaemon
import org.apache.cassandra.config.DatabaseDescriptor
import java.io.File
import scala.collection.JavaConversions._
import java.util.{Collection => JCollection}
import java.net.ConnectException
import org.apache.thrift.transport.{TTransportException, TSocket}
import com.shorrockin.cascal.session._
import com.shorrockin.cascal.utils.{Utils, Logging}
import org.apache.cassandra.config.{CFMetaData, KSMetaData}
import org.junit.AfterClass
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import org.apache.cassandra.config.Schema
import me.prettyprint.cassandra.service.OperationType

/**
 * trait which mixes in the functionality necessary to embed
 * cassandra into a unit test
 */
trait EmbeddedPool extends Logging with Schema {
  
  def borrow(f:(Session) => Unit) = {
    init
    EmbeddedCassandraServer.pool.borrow(OperationType.READ)(f)
  }
  
  def init = {
    EmbeddedCassandraServer.init(timeout, ksMetaData)
  }
  
  def shutdown = {
    EmbeddedCassandraServer.shutdown
  }
}

/**
 * maintains the single instance of the Cassandra server
 */
object EmbeddedCassandraServer extends Logging {
  import Utils._
  var initialized = false

  var pool: SessionPool = null
  var daemon = new CassandraDaemonThread
  
  def init(timeout: Int, ksMetaData: KSMetaData) = synchronized {
    if (!initialized) {
      val homeDirectory = new File("target/cassandra.home.unit-tests")
      delete(homeDirectory)
      homeDirectory.mkdirs

      log.debug("creating cassandra instance at: " + homeDirectory.getCanonicalPath)
      log.debug("copying cassandra configuration files to root directory")

      val fileSep = System.getProperty("file.separator")
      val storageFile = new File(homeDirectory, "cassandra.yaml")
      val logFile = new File(homeDirectory, "log4j.properties")

      replace(copy(resource("/cassandra.yaml"), storageFile), ("%temp-dir%" -> (homeDirectory.getCanonicalPath + fileSep)))
      copy(resource("/log4j.properties"), logFile)

      System.setProperty("cassandra.config", toURI(homeDirectory.getCanonicalPath + fileSep + "cassandra.yaml").toString)
      System.setProperty("log4j.configuration", toURI(homeDirectory.getCanonicalPath + fileSep + "log4j.properties").toString);
      System.setProperty("cassandra-foreground","true");

      log.debug("creating data file and log location directories")
      DatabaseDescriptor.getAllDataFileLocations.foreach { (file) => new File(file).mkdirs }
      
      loadSchema(ksMetaData)
      
      daemon.start

      // try to make sockets until the server opens up - there has to be a better
      // way - just not sure what it is.
      val socket = new TSocket("localhost", DatabaseDescriptor.getRpcPort)
      var opened = false
      while (!opened) {
        try {
          socket.open()
          opened = true
          socket.close()
        } catch {
          case e:TTransportException => /* ignore */
          case e:ConnectException => /* ignore */
        }
      }

      pool = new SessionPool("localhost", 10, Consistency.One)
      
      initialized = true
    }
  }
    
  private def resource(str:String) = classOf[EmbeddedPool].getResourceAsStream(str)
  
  private def loadSchema(ksMetaData: KSMetaData) = {
    val ksList = new java.util.ArrayList[KSMetaData]()
    ksList.add(ksMetaData)
    Schema.instance.load(ksList)
  }
  
  def shutdown {
    daemon.close
  }
}

/**
 * daemon thread used to start and stop cassandra
 */
class CassandraDaemonThread extends Thread("CassandraDaemonThread") with Logging {
  private val daemon = new CassandraDaemon

  /**
   * starts the server and blocks until it has
   * completed booting up.
   */
  override def run:Unit = {
    log.debug("starting cassandra daemon")
    daemon.activate
    log.debug("Cassandra daemon started")
  }

  def close():Unit = {
    log.debug("instructing cassandra deamon to shut down")
    daemon.deactivate
    log.debug("blocking on cassandra shutdown")
    this.join
    log.debug("cassandra shut down")
  }
}

