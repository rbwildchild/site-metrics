package com.rfa.metrics.cdp

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.Done
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Attributes, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import com.rfa.metrics.cdp.model.CdpCommand
import spray.json.{DefaultJsonProtocol, JsFalse, JsNumber, JsString, JsTrue, JsValue, JsonFormat, JsonParser, RootJsonFormat}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object CdpClient {

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
    }
    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
    }
  }

  implicit val cdpMessageFormat: RootJsonFormat[CdpCommand] = jsonFormat3(CdpCommand)

  val inst = List(
    new CdpCommand(
      2,
      "Network.enable"
    )
    ,
    new CdpCommand(
      3,
      "Page.navigate",
      Option(Map(
        ("url", "https://twitter.com")
      ))
    )
  )

  def apply(url: String): CdpClient = new CdpClient(connect(url))

  private def connect(url: String): Tuple2[SourceQueue[CdpCommand], SinkQueue[String]] = {
    {

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()
      import system.dispatcher

      val incoming = Flow[Message].map {
        case textStrict: TextMessage.Strict => textStrict.text
      }.toMat(Sink.queue())(Keep.right)

      val outgoing: Source[CdpCommand, SourceQueueWithComplete[CdpCommand]] =
        Source.queue[CdpCommand](1, OverflowStrategy.dropHead)

      val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))

      val ((sourceQueue, upgradeResponse), sinkQueue) =
      outgoing
        .map(c => TextMessage(cdpMessageFormat.write(c).toString()))
        .viaMat(webSocketFlow)(Keep.both)
        .toMat(incoming)(Keep.both)
        .run()

      val connected = upgradeResponse.flatMap { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Future.successful(Done)
        } else {
          throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
        }
      }

      connected.onComplete(println)

      (sourceQueue, sinkQueue)
    }
  }
}

class CdpClient(flow: Tuple2[SourceQueue[CdpCommand], SinkQueue[String]]) {
  def sendCommand(cdpCommand: CdpCommand) = flow._1.offer(cdpCommand)

  def next(): String = Await.result(flow._2.pull(),Duration(1, TimeUnit.SECONDS)).get
}
