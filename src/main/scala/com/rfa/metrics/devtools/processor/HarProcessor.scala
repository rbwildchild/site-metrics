package com.rfa.metrics.devtools.processor

import com.rfa.metrics.devtools.model.CdpResponse
import com.rfa.metrics.devtools.model.har.network._
import com.rfa.metrics.devtools.model.har
import com.rfa.metrics.devtools.model.har.{EventType, Har, HarEntry, HarPage}
import spray.json.DefaultJsonProtocol._
import spray.json.{JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

import scala.collection.mutable.ListBuffer

object HarProcessor {
  def apply(log: List[CdpResponse], pages: Array[Page]): HarProcessor =
    new HarProcessor(log, pages)
}

class HarProcessor(log: List[CdpResponse], pages: Array[Page]) {

  implicit object MapJsonFormat extends JsonFormat[Map[String, Double]] {
    def write(m: Map[String, Double]) = JsObject(m.mapValues {JsNumber(_)})

    def read(value: JsValue) = value.asJsObject.fields.mapValues[Double] {
      case JsNumber(n) => n.doubleValue()
      case _ => 0.0
    }
  }

  import MapJsonFormat._

  def start: Har = {
    val har = log
      .sortWith(_.getParam[Double]("timestamp").get < _.getParam[Double]("timestamp").get)
      .foldLeft(new Har)(processEvent)

    val harlog = har.buildLog()
    harlog.pages.foreach(println)
    harlog.entries.foreach(println)

    har
  }

  def processEvent(har: Har, event: CdpResponse): Har = {
    event.method.get match {
      case rws if rws == EventType.RequestWillBeSent.name => doRequestWillBeSent(har, event)
      case rws if rws == EventType.ResponseReceived.name => doResponseReceived(har, event)
      case _ =>
    }
    har
  }

  def doRequestWillBeSent(har: Har, event: CdpResponse) = {
    if (event.getParam[String]("type").get == "Document") {
      har.addPage()
    }
    val page = har.currentPage
    val entry = har.putEntry(event.getParam[String]("requestId").get)
    entry.pageref = page.id
    entry.aux.walltime = (event.getParam[Double]("wallTime").get * 1000).toLong
    entry.aux.referenceTime = (event.getParam[Double]("wallTime").get * 1000 - event.getParam[Double]("timestamp").get).toLong
    entry.startedDateTime = entry.aux.walltime
  }

  def doResponseReceived(har: Har, event: CdpResponse) = {
    val page = har.currentPage
    val entry = har.putEntry(event.getParam[String]("requestId").get)
    val timing = read(event.getParam[Map[String, JsObject]]("response").get.get("timing").get)
    entry.startedDateTime = entry.aux.referenceTime + timing.get("requestTime").get.toLong
  }

}