package com.buzzinate.testbed

import scala.io.Source
import com.buzzinate.api.Rating
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.HashMap
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

object Loader {
  def load(fileName: String): List[Rating] = {
    val dataset = new ListBuffer[Rating]

    for (line <- Source.fromFile(fileName).getLines) {
      dataset += Rating.parse(line)
    }
    dataset.result
  }
}