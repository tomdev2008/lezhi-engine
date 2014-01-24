package com.buzzinate.lezhi.util
import org.apache.commons.io.IOUtils
import java.io.IOException

object ResourceLoader {
  
  private val lineSep = System.getProperty("line.separator")  
  
  def loadResourceFile[A](filename: String, encoding: String = "UTF-8"): List[String] = {
    try {
      val is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)
      IOUtils.toString(is, encoding).split(lineSep).map(_.trim).toList
    } catch {
      case e: IOException => {
        e.printStackTrace
        List[String]()
      }
    }
  }
}