package com.shorrockin.cascal

import session._
import utils.{UUID, Conversions}
import org.junit.{Assert, Test}
import com.shorrockin.cascal.test.EmbeddedCassandraServer

class TestSessionPool extends EmbeddedCassandra {
  import Conversions._
  import Assert._

//  @Test def testSessionPool = {
//    EmbeddedCassandraServer.init
//    
//    val hosts  = Host(host, port, 250) :: Host(host, port+1, 250)
//    val params = new PoolParams(10, ExhaustionPolicy.Fail, 500L, 6, 2)
//    val pool   = new SessionPool(hosts, params, Consistency.One)
//
//    // as long as no exceptions were thrown we passed
//    (0 until 10).foreach { index =>
//      pool.borrow { _.count("Test" \ "Standard" \ UUID()) }
//    }
//
//    assertEquals(1, pool.idle)
//    pool.close
//    assertEquals(0, pool.idle)
//  }
//
//  @Test def testErrorCatchingAndLogging = {
//    EmbeddedCassandraServer.init
//
//    val hosts  = Host(host, port, 250) :: Nil
//    val params = new PoolParams(10, ExhaustionPolicy.Fail, 500L, 6, 2)
//    val pool   = new SessionPool(hosts, params, Consistency.One)
//
//    pool.borrow { session =>
//      try {
//        session.count("Non Existant" \ "Nope" \ "Nice Try")
//      } catch {
//        case e:Throwable => /* ignore */
//      }
//
//      assertTrue(session.hasError)
//    }
//
//    assertEquals(0, pool.idle)
//  }
}
