package com.rfa.metrics.devtools.processor

import com.rfa.metrics.devtools.model.RecordType.Network
import com.rfa.metrics.devtools.model._
import com.rfa.metrics.devtools.model.har.{HarEntry}

object LogProcessor {
  def apply(logs: List[CdpResponse]): LogProcessor = new LogProcessor(logs)
}

class LogProcessor(logs: List[CdpResponse]) {
  def getHAR(): String = {
    val har = "My Har"

    val pages = PageProcessor.getPages(
      logs
      .filter((c: CdpResponse) => c.method.get.startsWith(RecordType.Page.prefix) || c.method.get.startsWith(RecordType.Target.prefix))
    )

    logs
      .filter(_.method.get.startsWith(Network.prefix))
      .groupBy[String](_.getParam[String]("requestId").getOrElse(""))
      //.mapValues[HarEntry](HarEntryProcessor(_).start)
      .foreach((t: (String, List[CdpResponse])) => HarEntryProcessor(t._2, pages(0)).start)
    har
  }

}
