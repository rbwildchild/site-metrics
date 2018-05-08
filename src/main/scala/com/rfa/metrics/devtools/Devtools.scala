package com.rfa.metrics.devtools

import com.rfa.metrics.devtools.model.CdpConnection
import spray.json.DefaultJsonProtocol.jsonFormat6
import spray.json.{DefaultJsonProtocol, JsArray, JsonParser}
import DefaultJsonProtocol._
import com.rfa.metrics.cdp.CdpClient

object Devtools {
  def apply(cdpPort: Int): Devtools = {
    new Devtools(cdpPort)
  }

  class Client(cdpClient: CdpClient) {

  }
}

class Devtools(cdpPort: Int) {
  private implicit val cdpFormat = jsonFormat6(CdpConnection)

  def attachTab(): Devtools.Client = {
    new Devtools.Client(CdpClient(getCurrentUrl()))
  }

  private def getCDPConnections(): Array[CdpConnection] = {
    val cdpUrl = "http://localhost:" + cdpPort + "/json"
    val result = scala.io.Source.fromURL(cdpUrl).mkString
    val jsonAst: JsArray = JsonParser(result).asInstanceOf[JsArray]
    jsonAst.elements.map(_.asJsObject.convertTo[CdpConnection]).toArray
  }

  private def getCurrentUrl(): String = {
    val cdpConns = getCDPConnections()
    cdpConns(cdpConns.length -1 ).webSocketDebuggerUrl
  }
}
