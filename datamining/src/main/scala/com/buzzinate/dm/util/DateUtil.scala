package com.buzzinate.dm.util

import java.text.SimpleDateFormat
import java.util.Date

object DateUtil {
  val df = new SimpleDateFormat("yyyy-MM-dd")
  
  val ONE_DAY = 1000L * 3600 * 24
  
  def truncateDate(time: Long): Long = {
    df.parse(df.format(new Date(time))).getTime
  }
  
  def format(time: Long) = df.format(new Date(time))
}