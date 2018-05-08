package com.rfa.metrics.devtools

import com.rfa.metrics.devtools.model.CdpConnection
import spray.json.DefaultJsonProtocol.jsonFormat6
import spray.json.{DefaultJsonProtocol, JsArray, JsonParser}
import DefaultJsonProtocol._
import com.rfa.metrics.cdp.CdpClient
import com.rfa.metrics.cdp.model.CdpCommand

import scala.concurrent.Await

object DevtoolsClient {
  def apply(cdpPort: Int): DevtoolsClient = {
    new DevtoolsClient(cdpPort)
  }
}

class DevtoolsClient(cdpPort: Int) {
  implicit val cdpFormat = jsonFormat6(CdpConnection)

  val cdpClient = CdpClient(getCDPUrl())

  def getCDPConnections(): Array[CdpConnection] = {
    val cdpUrl = "http://localhost:" + cdpPort + "/json"
    val result = scala.io.Source.fromURL(cdpUrl).mkString
    val jsonAst: JsArray = JsonParser(result).asInstanceOf[JsArray]
    jsonAst.elements.map(_.asJsObject.convertTo[CdpConnection]).toArray
  }

  def getCDPUrl(): String = {
    val cdpConns = getCDPConnections()
    cdpConns(cdpConns.length -1 ).webSocketDebuggerUrl
  }
}
