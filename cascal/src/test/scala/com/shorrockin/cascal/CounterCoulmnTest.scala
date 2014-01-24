package com.shorrockin.cascal

import org.junit.{Assert, Test}
import com.shorrockin.cascal.session.KeyRange
import com.shorrockin.cascal.session.EmptyPredicate
import com.shorrockin.cascal.session.RangePredicate
import com.shorrockin.cascal.session.ColumnPredicate
import com.shorrockin.cascal.session.Add
import com.shorrockin.cascal.session.Delete

class CounterCoulmnTest extends EmbeddedCassandra {
  import com.shorrockin.cascal.utils.Conversions._
  import Assert._

  @Test def addAndGetCounter = borrow { session =>

    val col = "Test" \# "StandardCounter" \ "Test" \ "col name"
    assertTrue(session.get(col).isEmpty)
    
    session.add(col + 1)
    assertEquals(Some(1), session.get(col).get.value)
    
    session.add(col + 10)
    assertEquals(Some(11), session.get(col).get.value)
    
    session.add(col - 1)
    assertEquals(Some(10), session.get(col).get.value)
  }

  @Test def getCounterRowsUsingKeyRange = borrow { session =>
    val cf = "Test" \# "StandardCounter"
    session.add(cf \ "range1" \ "col1" + 1)
    session.add(cf \ "range2" \ "col1" - 100)
    session.add(cf \ "range3" \ "col1" + 23)
    
    val results = session.list(cf, KeyRange("range1", "range3", 100))
    assertEquals(3, results.size)
  }
  
  @Test def getCounterRowsUsingMultiGet = borrow { session =>
    val key1 = "Test" \# "StandardCounter" \ "container1"
    val key2 = "Test" \# "StandardCounter" \ "container2"
    val key3 = "Test" \# "StandardCounter" \ "container3"
    
    session.add(key1 \ "col1" + 1)
    session.add(key2 \ "col1" - 100)
    session.add(key3 \ "col1" + 23)
    
    val results = session.list(key1 :: key2 :: key3 :: Nil)
    assertEquals(3, results.size)
  }
  
  @Test def getKeyCounters = borrow { session =>
    val key = "Test" \# "StandardCounter" \ "key counters"
    session.add(key \ "col1" + 1)
    session.add(key \ "col2" - 2)
    session.add(key \ "col3" + 3)
    
    val rangeResults = session.list(key, RangePredicate("col1", "col3"))
    assertEquals(3, rangeResults.size)
    
    val predicateResults = session.list(key, ColumnPredicate(List("col1", "col3")))
    assertEquals(2, predicateResults.size)
  }
  
  @Test def removeCounterColumn = borrow { session =>
    val col = "Test" \# "StandardCounter" \ "testremove" \ "col1" + 1
    session.add(col)
    assertEquals(Some(1), session.get(col).get.value)
    
    session.remove(col)
    assertEquals(None, session.get(col))
  }

  @Test def counterBatchAddAndDelete = borrow { session =>
    val key  = "Test" \# "StandardCounter" \ "test batch"
    val col1 = key \ ("Column-1", + 10)
    val col2 = key \ ("Column-2", + 2)
    val col3 = key \ ("Column-3",  - 500)

    session.batch(Add(col1) :: Add(col2) :: Add(col3))
    assertEquals(3, session.list(key).size)
    
    session.batch(Delete(key, ColumnPredicate(col2.name :: col3.name :: Nil)) :: Nil)
    assertEquals(1, session.list(key).size)
  }
  
  @Test def countCounterColumns = borrow { session =>
    val key1 = "Test" \# "StandardCounter" \ "count columns key1"
    session.add(key1 \ "col1" + 1)
    session.add(key1 \ "col2" - 100)
    session.add(key1 \ "col3" + 23)
    
    val key2 = "Test" \# "StandardCounter" \ "count columns key2"
    session.add(key2 \ "col1" + 1)
    
    val results = session.count(key1 :: key2 :: Nil)
    assertEquals(3, results(key1))
    assertEquals(1, results(key2))
  }
} 	
