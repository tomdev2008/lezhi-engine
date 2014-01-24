package com.shorrockin.cascal

import org.junit.{Assert, Test}
import com.shorrockin.cascal.utils.UUID

/**
 * tests session removal
 */
class TestRemoval extends EmbeddedCassandra {
  import com.shorrockin.cascal.utils.Conversions._
  import Assert._

  @Test def testKeyRemoval = borrow { (s) =>
    val std = s.insert("Test" \ "Standard" \ UUID() \ ("Column", "Value"))
    val sup = s.insert("Test" \\ "Super" \ "SuperKey" \ UUID() \ ("Column", "Value"))

    s.remove(std.key)
    s.remove(sup.key)

    assertEquals(None, s.get(std))
    assertEquals(None, s.get(sup))
  }

  @Test def testColumnRemoval = borrow { (s) =>
    val std = s.insert("Test" \ "Standard" \ UUID() \ ("Column", "Value"))
    val sup = s.insert("Test" \\ "Super" \ "SuperKey" \ UUID() \ ("Column", "Value"))

    s.remove(std)
    s.remove(sup)

    assertEquals(None, s.get(std))
    assertEquals(None, s.get(sup))
  }
  
  @Test def testTruncate = borrow { (s) =>
    val col  = "Test" \ "Standard" \ UUID() \ "Column"
    val std = s.insert(col \ "Value")
    assertEquals("Value", new String(s.get(col).get.value.array))
    s.truncate("Standard")
    
    assertEquals(None, s.get(col))
  }
}
