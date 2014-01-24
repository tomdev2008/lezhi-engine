package com.shorrockin.cascal

import org.junit.{Assert, Test}
import com.shorrockin.cascal.utils.Conversions._
import Assert._

class TestSeconderyIndex extends EmbeddedCassandra {

  val family = "Test" \ "StandardIndexed"
  val key1 = family \ "key1"
  val key2 = family \ "key2"
  
  @Test def eqExpression = borrow { (session) =>
    
    session.insert(key1 \ "column1" \ "b")
    session.insert(key1 \ "column2" \ "c")
    
    session.insert(key2 \ "column1" \ "b")
        
    val query = family where "column1" Eq "b" startAt "a"
    
    val rows = session.list(query)
    assertEquals(2, rows.size)
  }
  
  @Test def eqExpressionForLongType = borrow { (session) =>
    
    session.insert(key1 \ "longColumn" \ 1L)
    session.insert(key2 \ "longColumn" \ 2L)
        
    val query = family where "longColumn" Eq 1L startAt 0
    
    val rows = session.list(query)
    assertEquals(1, rows.size)
  }
  
  @Test def eqAndGtExpression = borrow { (session) =>
    
    session.insert(key1 \ "column1" \ "b")
    session.insert(key1 \ "column2" \ "c")
    
    session.insert(key2 \ "column1" \ "b")
    session.insert(key2 \ "column2" \ "b")
        
    val query = family where "column1" Eq "b" and "column2" Gt "b" startAt "a"
    val rows = session.list(query)
    assertEquals(1, rows.size)
  }
  
  @Test def eqAndGteExpression = borrow { (session) =>
    
    session.insert(key1 \ "column1" \ "b")
    session.insert(key1 \ "column2" \ "c")
    
    session.insert(key2 \ "column1" \ "b")
    session.insert(key2 \ "column2" \ "b")
        
    val query = family where "column1" Eq "b" and "column2" Gte "b" startAt "a"
    val rows = session.list(query)
    assertEquals(2, rows.size)
  }
  
  @Test def eqAndLtExpression = borrow { (session) =>
    
    session.insert(key1 \ "column1" \ "b")
    session.insert(key1 \ "column2" \ "a")
    
    session.insert(key2 \ "column1" \ "b")
    session.insert(key2 \ "column2" \ "b")
        
    val query = family where "column1" Eq "b" and "column2" Lt "b" startAt "a"
    val rows = session.list(query)
    assertEquals(1, rows.size)
  }
  
  @Test def eqAndLteExpression = borrow { (session) =>
    
    session.insert(key1 \ "column1" \ "b")
    session.insert(key1 \ "column2" \ "a")
    
    session.insert(key2 \ "column1" \ "b")
    session.insert(key2 \ "column2" \ "b")
        
    val query = family where "column1" Eq "b" and "column2" Lte "b" startAt "a"
    val rows = session.list(query)
    assertEquals(2, rows.size)
  }
  
  @Test def limitNumberOfResult = borrow { (session) =>
    
    session.insert(key1 \ "column1" \ "b")
    session.insert(key1 \ "column2" \ "a")
    
    session.insert(key2 \ "column1" \ "b")
    session.insert(key2 \ "column2" \ "b")
        
    val query = family where "column1" Eq "b" and "column2" Lte "b" startAt "a" limit 1
    val rows = session.list(query)
    assertEquals(1, rows.size)
  }
  
}