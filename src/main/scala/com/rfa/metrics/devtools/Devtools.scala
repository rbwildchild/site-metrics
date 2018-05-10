package com.rfa.metrics.devtools

import java.util.concurrent.TimeUnit

import com.rfa.metrics.devtools.model.{CdpConnection, CdpResponse}
import spray.json.DefaultJsonProtocol.jsonFormat6
import spray.json.{DefaultJsonProtocol, JsArray, JsonParser}
import DefaultJsonProtocol._
import com.rfa.metrics.cdp.CdpClient
import com.rfa.metrics.cdp.model.CdpCommand
import com.rfa.metrics.devtools.processor.LogProcessor

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Devtools {
  val commands = List(
    new CdpCommand(
      1,
      "Network.enable"
    )
  )

  def apply(cdpPort: Int): Devtools = {
    new Devtools(cdpPort)
  }

  class Client(cdpClient: CdpClient) {
    def startRecord() = {
      commands.foreach(cdpClient.sendCommand)
    }

    def stopRecord() = {
      cdpClient.terminateFlow(Duration(3, TimeUnit.SECONDS)).onComplete {
        case s: Success[List[CdpResponse]] => {
          val har = LogProcessor(s.get).getHAR()
          println("HAR: " + har)
        }
        case f: Failure[List[CdpResponse]] => println("Fail!: " + f)
      }
    }
  }
}

class Devtools(cdpPort: Int) {
  private implicit val cdpFormat = jsonFormat6(CdpConnection)

  def attachTab(): Devtools.Client = {
    new Devtools.Client(CdpClient(getCurrentUrl()))
  }

  private def getCurrentUrl(): String = {
    val cdpConns = getCDPConnections()
    cdpConns(cdpConns.length -1 ).webSocketDebuggerUrl
  }

  private def getCDPConnections(): Array[CdpConnection] = {
    val cdpUrl = "http://localhost:" + cdpPort + "/json"
    val result = scala.io.Source.fromURL(cdpUrl).mkString
    val jsonAst: JsArray = JsonParser(result).asInstanceOf[JsArray]
    jsonAst.elements.map(_.asJsObject.convertTo[CdpConnection]).toArray
  }
}
