package com.rfa.metrics

import java.util.Date

import org.joda.time.DateTimeZone
import org.joda.time.format.ISODateTimeFormat


object FormatUtil {


  val tf = ISODateTimeFormat.dateTime.withZone(DateTimeZone.getDefault)

  def formatDate(millis: Long): String = {
    tf.print(millis)
  }
}
