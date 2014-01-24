package com.buzzinate.lezhi.config

import scala.collection.mutable.HashMap

object DelayedResource {
  val resources = new HashMap[String, AnyRef]
  
  def getOrElse[T: ClassManifest](name: String)(init: => T): T = {
    val key = classManifest[T].erasure.getName + "#" + name
    resources.getOrElseUpdate(key, init.asInstanceOf[AnyRef]).asInstanceOf[T]
  }
  
  def getOrElse[T: ClassManifest](init: => T): T = {
    getOrElse("")(init)
  }
}