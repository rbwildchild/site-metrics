package com.rfa.metrics.devtools

import java.util.concurrent.TimeUnit

import com.rfa.metrics.devtools.model.{CdpConnection, CdpResponse}
import spray.json.DefaultJsonProtocol.jsonFormat6
import spray.json.{DefaultJsonProtocol, JsArray, JsonParser}
import DefaultJsonProtocol._
import akka.Done
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
      "Page.enable"
    ),
    new CdpCommand(
      2,
      "Network.enable"
    )
  )

  def apply(cdpPort: Int): Devtools = {
    new Devtools(cdpPort)
  }

  class Client(cdpClient: CdpClient) {

    private var responseList = List[CdpResponse]()

    def startRecord(): Future[Done] = {
      cdpClient.sendCommands(commands)
    }

    def stopRecord() = {
      cdpClient.terminateFlow(Duration(3, TimeUnit.SECONDS)).flatMap {
        case s: List[CdpResponse] => {
          responseList = s
          Future.successful(Done)
        }
        case _ => Future.failed(new Exception("Error"))
      }
    }

    def getHar(): String = {
      LogProcessor(responseList).getHAR()
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
