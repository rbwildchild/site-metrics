package com.rfa.metrics.devtools.processor

import com.rfa.metrics.devtools.model._
import com.rfa.metrics.devtools.model.har.{HarEntry, RequestWillBeSent}

object LogProcessor {
  def apply(logs: List[CdpResponse]): LogProcessor = new LogProcessor(logs)
}

class LogProcessor(logs: List[CdpResponse]) {
  def getHAR(): String = {
    val har = "My Har"

    //logs
    // .filter(_.method.get.startsWith(Network.prefix))
    // .foreach(println)

    logs
      .filter(_.method.get.startsWith(Network.prefix))
      .groupBy[String](_.getParam[String]("requestId"))
      //.mapValues[HarEntry](HarEntryProcessor(_).start)
      .foreach((t: (String, List[CdpResponse])) => HarEntryProcessor(t._2).start)
    har
  }

}
