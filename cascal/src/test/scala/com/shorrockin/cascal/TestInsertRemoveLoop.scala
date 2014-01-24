package com.shorrockin.cascal

import org.junit.{Assert, Test}
import com.shorrockin.cascal.utils.Utils

/**
 * tests a looping insert remove.  Stresses out the precision of
 * system time.
 */
class TestInsertRemoveLoop extends EmbeddedCassandra {
  import com.shorrockin.cascal.utils.Conversions._
  import Assert._

  @Test def testInsertRemoveLoop = borrow { session =>

    def checkLowResolution = {
      var onLowPrecisionSystem = false
      for( i <- 1L to 100L ) {
        val colName = "col" + i
        session.remove("Test" \ "Standard" \ "Test")
        session.insert("Test" \ "Standard" \ "Test" \ (colName, "value:"+i))
        if( session.get("Test" \ "Standard" \ "Test" \ colName) == None ) {
          onLowPrecisionSystem = true
        }
      }
      onLowPrecisionSystem
    }

    if( checkLowResolution ) {
      println("You have low resolution timer on this system")
      Utils.COMPENSATE_FOR_LOW_PRECISION_SYSTEM_TIME = true
      assertFalse("setting Utils.COMPENSATE_FOR_LOW_PRECISION_SYSTEM_TIME = true did not work around the low resolution timer problems.", checkLowResolution);
    } else {
      println("You have high resolution timer on this system")
    }
  }

}