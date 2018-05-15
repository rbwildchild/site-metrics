package com.rfa.metrics.devtools.processor

import com.rfa.metrics.devtools.model.CdpResponse

object LogProcessor {
  def apply(logs: List[CdpResponse]): LogProcessor = new LogProcessor(logs)
}

class LogProcessor(logs: List[CdpResponse]) {
  def getHAR(): String = {
    val har = ""
    logs.foreach(println)
    har
  }
}
