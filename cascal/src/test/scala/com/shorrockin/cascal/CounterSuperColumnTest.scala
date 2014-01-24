package com.shorrockin.cascal

import org.junit.{Assert, Test}
import com.shorrockin.cascal.session.KeyRange
import com.shorrockin.cascal.session.RangePredicate
import com.shorrockin.cascal.session.ColumnPredicate
import com.shorrockin.cascal.session.Add
import com.shorrockin.cascal.session.Delete

class CounterSuperColumnTest extends EmbeddedCassandra {
  import com.shorrockin.cascal.utils.Conversions._
  import Assert._

  @Test def addAndGetCounter = borrow { session =>

    val col = "Test" \\# "SuperCounter" \ "Add and get key" \ "super" \ "col name"
    assertTrue(session.get(col).isEmpty)
    
    session.add(col + 1)
    assertEquals(Some(1), session.get(col).get.value)
    
    session.add(col + 10)
    assertEquals(Some(11), session.get(col).get.value)
    
    session.add(col - 1)
    assertEquals(Some(10), session.get(col).get.value)
  }
  
  @Test def getCounterRowsUsingKeyRange = borrow { session =>
    val cf = "Test" \\# "SuperCounter"
    session.add(cf \ "range1" \ "super" \ "col1" + 1)
    session.add(cf \ "range2" \ "super" \ "col1" - 100)
    session.add(cf \ "range3" \ "super" \ "col1" + 23)
    
    val results = session.list(cf, KeyRange("range1", "range3", 100))
    assertEquals(3, results.size)
  }
  
  @Test def getCounterRowsUsingMultiGet = borrow { session =>
    val key1 = "Test" \\# "SuperCounter" \ "container1"
    val key2 = "Test" \\# "SuperCounter" \ "container2"
    val key3 = "Test" \\# "SuperCounter" \ "container3"
    
    session.add(key1 \ "super" \ "col1" + 1)
    session.add(key2 \ "super" \ "col1" - 100)
    session.add(key3 \ "super" \ "col1" + 23)
    
    val results = session.list(key1 :: key2 :: key3 :: Nil)
    assertEquals(3, results.size)
  }
  
  @Test def getKeyCounters = borrow { session =>
    val key = "Test" \\# "SuperCounter" \ "key counters"
    session.add(key \ "super1" \ "col1" + 1)
    session.add(key \ "super2" \ "col2" - 2)
    session.add(key \ "super3" \ "col3" + 3)
    
    val rangeResults = session.list(key, RangePredicate("super1", "super3"))
    assertEquals(3, rangeResults.size)
    
    val predicateResults = session.list(key, ColumnPredicate(List("super1", "super2")))
    assertEquals(2, predicateResults.size)
  }
  
  @Test def removeCounterColumn = borrow { session =>
    val col = "Test" \\# "SuperCounter" \ "testremove" \ "super" \ "col1" + 1
    session.add(col)
    assertEquals(Some(1), session.get(col).get.value)
    
    session.remove(col)
    assertEquals(None, session.get(col))
  }
  
  @Test def counterBatchAddAndDelete = borrow { session =>
    val key  = "Test" \\# "SuperCounter" \ "test batch"
    val col1 = key \ "super" \ ("Column-1", + 10)
    val col2 = key \ "super" \ ("Column-2", + 2)
    val col3 = key \ "super" \ ("Column-3",  - 500)

    session.batch(Add(col1) :: Add(col2) :: Add(col3))
    assertEquals(3, session.list(key \ "super").size)
    
    session.batch(Delete(key, ColumnPredicate(col2.name :: col3.name :: Nil)) :: Nil)
    assertEquals(1, session.list(key).size)
  }
  
  @Test def countCounterColumns = borrow { session =>
    val key1 = "Test" \\# "SuperCounter" \ "count columns key1"
    val key1Super = key1 \ "super"
    session.add(key1Super \ "col1" + 1)
    session.add(key1Super \ "col2" - 100)
    session.add(key1Super \ "col3" + 23)
    
    val key2 = "Test" \\# "SuperCounter" \ "count columns key2"
    val key2Super = key2 \ "super"
    session.add(key2Super \ "col1" + 1)
    
    val keyResults = session.count(key1 :: key2 :: Nil)
    assertEquals(1, keyResults(key1))
    assertEquals(1, keyResults(key2))
    
    val superResults = session.count(key1Super :: key2Super :: Nil)
    assertEquals(3, superResults(key1Super))
    assertEquals(1, superResults(key2Super))
  }
}