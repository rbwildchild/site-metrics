package com.rfa.metrics.devtools.model.har

import spray.json.{JsObject, JsValue, RootJsonFormat}
import spray.json.DefaultJsonProtocol._

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map

class Har() {

  private val pages = ListBuffer[HarPage]()
  private val entries = Map[String, HarEntry]()

  def addPage(): HarPage = {
    pages.append(new HarPage(id = "page_" + pages.size))
    pages.last
  }

  def putEntry(requestId: String): HarEntry = {
    if (!entries.contains(requestId))
      entries.put(requestId, new HarEntry)
    entries.get(requestId).get
  }

  def currentPage = pages.last

  def buildLog(): Log = new Log(pages.result().toArray, entries.values.toArray.sortWith(_.startedDateTime > _.startedDateTime))
}

case class Log(pages: Array[HarPage], entries: Array[HarEntry]) {

  object  LogJsonFormat extends RootJsonFormat[Log] {
    implicit val harPageFormat: RootJsonFormat[HarPage] = jsonFormat5(HarPage)
    implicit val harEntryFormat: RootJsonFormat[HarEntry] = jsonFormat11(HarEntry)

    private implicit val pageTimingsFormat: RootJsonFormat[PageTimings] = jsonFormat3(PageTimings)
    private implicit val requestFormat: RootJsonFormat[Request] = jsonFormat0(Request)
    private implicit val responseFormat: RootJsonFormat[Response] = jsonFormat0(Response)
    private implicit val cacheFormat: RootJsonFormat[Cache] = jsonFormat0(Cache)
    private implicit val timingsFormat: RootJsonFormat[Timings] = jsonFormat0(Timings)
    private implicit val auxFormat: RootJsonFormat[Aux] = jsonFormat2(Aux)
    //private implicit val pagesFormats: RootJsonFormat[Array[HarPage]] = arrayFormat[HarPage]
    //private implicit val entriesFormats: RootJsonFormat[Array[HarEntry]] = arrayFormat[HarEntry]
    private implicit val logFormat: RootJsonFormat[Log] = jsonFormat2(Log)

    override def read(json: JsValue): Log = logFormat.read(json)

    override def write(log: Log): JsValue = JsObject(
      "pages" -> arrayFormat[HarPage].write(log.pages),
      "entries" -> arrayFormat[HarEntry].write(log.entries)
    )
  }

  def toJson: JsValue = LogJsonFormat.write(this)
}
