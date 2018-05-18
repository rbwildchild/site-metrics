package com.rfa.metrics.devtools.processor

import com.rfa.metrics.FormatUtil
import com.rfa.metrics.cdp.model.CdpCommand
import com.rfa.metrics.devtools.model.CdpResponse
import com.rfa.metrics.devtools.model.har.network._
import com.rfa.metrics.devtools.model.har
import com.rfa.metrics.devtools.model.har.{EventType, HarEntry, HarPage}
import spray.json.DefaultJsonProtocol.{jsonFormat16, mapFormat}
import spray.json.{JsNumber, JsObject, JsString, JsValue, JsonFormat, RootJsonFormat}

import scala.collection.mutable.ListBuffer

object HarEntryProcessor {
  def apply(events: List[CdpResponse], pages: Page): HarEntryProcessor =
    new HarEntryProcessor(events, pages)
}

class HarEntryProcessor(events: List[CdpResponse], page: Page) {

  implicit object MapJsonFormat extends JsonFormat[Map[String, Double]] {
    def write(m: Map[String, Double]) = JsObject(m.mapValues {JsNumber(_)})

    def read(value: JsValue) = ???
  }

  import MapJsonFormat._

  //implicit val timingFormat: RootJsonFormat[Timing] = jsonFormat16(Timing)
  //implicit val timingMapFormat: RootJsonFormat[Map[JsString, JsNumber]] = mapFormat[JsString, JsNumber]

  import FormatUtil._

  def start(): HarEntry = {
    events
      .sortWith(_.getParam[Double]("timestamp").get > _.getParam[Double]("timestamp").get)
        .foldLeft(new Resource())(processEvent)
    new HarEntry()
  }

  def processEvent(resource: Resource, cdpResponse: CdpResponse): Resource = {
    cdpResponse.method.get match {
      case EventType.RequestWillBeSent.name => doRequestWillBeSent(resource, cdpResponse)
      case EventType.ResponseReceived.name => doResponseReceived(resource, cdpResponse)
      case _ => Unit
    }
    //println(resource)
    resource
  }

  def doRequestWillBeSent(resource: Resource, cdpResponse: CdpResponse) = {
    resource.requestWillBeSent = new RequestWillBeSent(
      cdpResponse.getParam[Double]("wallTime").get,
      cdpResponse.getParam[Double]("timestamp").get,
      cdpResponse.getParam[Map[String, AnyRef]]("request").get)
    resource.`type` = cdpResponse.getParam[String]("type").get
    None
  }

  def doResponseReceived(resource: Resource, cdpResponse: CdpResponse) = {
    resource.responseReceived = new ResponseReceived(
      cdpResponse.getParam[Double]("timestamp").get,
      cdpResponse.getParam[Map[String, Map[String, AnyRef]]]("response").get)
    None
  }

  def createHarPage() = {
    new HarPage(
      id = "page_" +page.id,
      title = page.title
    )
  }

}