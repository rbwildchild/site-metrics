package com.rfa.metrics.devtools.processor

import com.rfa.metrics.devtools.model.{CdpResponse, CdpResponseUtil}
import com.rfa.metrics.devtools.model.har.{HarEntry, HarPage, RequestWillBeSent}

object HarEntryProcessor {
  def apply(events: List[CdpResponse]): HarEntryProcessor = new HarEntryProcessor(events)
}

class HarEntryProcessor(events: List[CdpResponse]) {

  def start(): HarEntry = {
    val pages = List[HarPage]()
    events
      .sortWith(_.getParam[Integer]("timestamp") > _.getParam[Integer]("timestamp"))
        .foldRight(new HarEntry())(processEvent(pages))
  }

  def processEvent(pages: List[HarPage])(cdpResponse: CdpResponse, harEntry: HarEntry): HarEntry = {
    cdpResponse.method.get match {
      case RequestWillBeSent.name => doRequestWillBeSent(cdpResponse, harEntry, pages)
      case _ => Unit
    }
    harEntry
  }

  def doRequestWillBeSent(cdpResponse: CdpResponse, harEntry: HarEntry, pages: List[HarPage]) = {
    println(cdpResponse.getParam[String]("type"))
  }

}