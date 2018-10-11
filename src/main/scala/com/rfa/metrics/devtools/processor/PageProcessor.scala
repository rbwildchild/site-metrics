package com.rfa.metrics.devtools.processor

import com.rfa.metrics.devtools.model.{CdpResponse, RecordType}
import com.rfa.metrics.devtools.model.har.HarPage
import com.rfa.metrics.devtools.model.har.network.Page

import scala.collection.mutable.ListBuffer

object PageProcessor {

  val INIT = "INIT"
  val FINISH = "FINISH";

  def getPages(events: List[CdpResponse]): Array[Page] = {
    val numPages: Int = events
      .map((c: CdpResponse) => if (c.method.get == RecordType.Page.prefix + "domContentEventFired") 1 else 0)
      .sum

    events.foldLeft((List[Page](), FINISH))((acc: (List[Page], String), c: CdpResponse) => {
      var list = acc._1
      var state = acc._2
      c.method.get match {
        case s if s == RecordType.Page.prefix + "loadEventFired" ||
          s == RecordType.Page.prefix + "domContentEventFired" => {
          list = if (state == FINISH) list ::: new Page(list.size) :: Nil else list
          if (s == RecordType.Page.prefix + "loadEventFired")
            list.last.loadEventFired = c.getParam[Double]("timestamp").get
          if (s == RecordType.Page.prefix + "domContentEventFired")
            list.last.domContentEventFired = c.getParam[Double]("timestamp").get
          state = INIT
        }
        case s if RecordType.Target.prefix + "targetInfoChanged" == s => {
          if (state == INIT) list.last.title = c.getParam[Map[String, Any]]("targetInfo").get.get("title").toString
          state = FINISH
        }
        case _ =>
      }
      (list, state)
    })._1.toArray
  }

  def isPageComplete(p: Page) = p.loadEventFired > 0 && p.domContentEventFired > 0 && p.title != ""


}
