package com.rfa.metrics.devtools.processor

import com.rfa.metrics.FormatUtil
import com.rfa.metrics.devtools.model.CdpResponse
import com.rfa.metrics.devtools.model.har.{Document, HarEntry, HarPage, RequestWillBeSent}

import scala.collection.mutable.ListBuffer

object HarEntryProcessor {
  def apply(events: List[CdpResponse], pageCount: Int): HarEntryProcessor = new HarEntryProcessor(events, pageCount)
}

class HarEntryProcessor(events: List[CdpResponse], pageCount: Int) {

  import FormatUtil._

  def start(): HarEntry = {
    events
      .sortWith(_.getParam[Double]("timestamp") > _.getParam[Double]("timestamp"))
        .foldRight(new HarEntry())(processEvent)
  }

  def processEvent(cdpResponse: CdpResponse, harEntry: HarEntry): HarEntry = {
    cdpResponse.method.get match {
      case RequestWillBeSent.name => doRequestWillBeSent(cdpResponse, harEntry)
      case _ => Unit
    }
    harEntry
  }

  def doRequestWillBeSent(cdpResponse: CdpResponse, harEntry: HarEntry) = {
    if (cdpResponse.getParam[String]("type") == Document.name) {
      val page = createPage(cdpResponse, pageCount)
      harEntry.pageref = page.id
      harEntry.page = Some(page)
      println(page)
    }
  }

  def createPage(cdpResponse: CdpResponse, pageCount: Int): HarPage = {
    HarPage(
      startedDateTime = formatDate((cdpResponse.getParam[Double]("wallTime") * 1000).toLong),
      id = "page_" + pageCount
    )
  }

}