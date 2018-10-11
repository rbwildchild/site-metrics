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
    ),
    new CdpCommand(
      3,
      "Target.setDiscoverTargets",
      Some(Map(("discover", true)))
    ),
    new CdpCommand(
      4,
      "Network.setUserAgentOverride",
      Some(Map(("userAgent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")))
    ),
    new CdpCommand(
      5,
      "Emulation.setDeviceMetricsOverride",
      Some(Map(
        ("width", 375),
        ("height", 812),
        ("deviceScaleFactor", 2),
        ("mobile", true)
      ))
    ),
    new CdpCommand(
      5,
      "Emulation.setTouchEmulationEnabled",
      Some(Map(
        ("enabled", true)
      ))
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
