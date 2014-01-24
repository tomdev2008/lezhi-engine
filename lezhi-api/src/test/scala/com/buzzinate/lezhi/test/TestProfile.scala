package com.buzzinate.lezhi.test

import com.buzzinate.lezhi.behavior.{ViewEntry, UserProfile}
import com.buzzinate.lezhi.store.HTableUtil

object TestProfile {
  def main(args: Array[String]): Unit = {
    val htablePool = HTableUtil.createHTablePool("192.168.1.93", 10)
    val up = new UserProfile(htablePool, 3)
    println("before: " + up.get("chinatest.com", "user2"))
    for (i <- 100 until 500) {
      up.add(List(ViewEntry("chinatest.com", "user2", i.toLong, i.toLong)))
    }
    println("after: " + up.get("chinatest.com", "user2"))

//    val docdb = new DocDB(htablePool)
//    for (i <- 0 until 100) {
//      val docs = 0 until 10 map { j =>
//        Doc("chinanews.com", i * 100 + j, "test url", "test title", "test keywords", System.currentTimeMillis)
//      }
//      docdb.add(docs.toList)
//    }
//    docdb.get("chinanews.com", (0 until 500).toList map (x => x.toLong)) foreach { case (docid, doc) =>
//      println(doc)
//    }
  }
}
