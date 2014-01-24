package com.buzzinate.keywords.util

import scala.io.Source
import java.io.File

object IO {
  def printToFile(filename: String)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(new File(filename))
    try { op(p) } finally { p.close() }
  }
  
  def loadFromFile[T](filename: String)(op: Iterator[String] => T): T = {
    val src = Source.fromFile(filename)
    try { op(src.getLines) } finally { src.close() }
  }
  
  def loadFromFile[T](filename: String, enc: String)(op: Iterator[String] => T): T = {
    val src = Source.fromFile(filename)(enc)
    try { op(src.getLines) } finally { src.close() }
  }
}