package com.rfa.metrics.cdp

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Attributes, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import com.rfa.metrics.cdp.model.CdpCommand
import com.rfa.metrics.devtools.model.CdpResponse
import spray.json.{DefaultJsonProtocol, JsFalse, JsNumber, JsObject, JsString, JsTrue, JsValue, JsonFormat, JsonParser, JsonReader, RootJsonFormat}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.concurrent.{Await, Future, Promise}

import scala.concurrent.ExecutionContext.Implicits.global

object CdpClient {

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int  => JsNumber(n)
      case s: String => JsString(s)
      case b: Boolean if b == true => JsTrue
      case b: Boolean if b == false => JsFalse
    }
    def read(value: JsValue) = value match {
      case JsNumber(n) => n.intValue()
      case JsString(s) => s
      case JsTrue => true
      case JsFalse => false
      case JsObject(s) => s
      case _ => None
    }
  }

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  implicit val cdpCommandFormat: RootJsonFormat[CdpCommand] = jsonFormat3(CdpCommand)
  implicit val cdpResponseFormat: RootJsonFormat[CdpResponse] = jsonFormat4(CdpResponse)

  def apply(url: String): CdpClient = {
    new CdpClient(connect(url))
  }

  private def connect(url: String): Tuple2[SourceQueueWithComplete[CdpCommand], Future[List[CdpResponse]]] = {
    {

      val incoming = Flow[Message].map {
        case t: TextMessage.Strict => cdpResponseFormat.read(JsonParser(t.text))
      }.toMat(Sink.collection[CdpResponse, List[CdpResponse]])(Keep.right)

      val outgoing: Source[CdpCommand, SourceQueueWithComplete[CdpCommand]] =
        Source.queue[CdpCommand](1, OverflowStrategy.dropHead)

      val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))

      val ((sourceQueue, upgradeResponse), sinkQueue) =
      outgoing
        .map(c => TextMessage(cdpCommandFormat.write(c).toString()))
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

class CdpClient(flow: Tuple2[SourceQueueWithComplete[CdpCommand], Future[List[CdpResponse]]])(implicit actorSystem: ActorSystem) {

  def sendCommand(cdpCommand: CdpCommand) = flow._1.offer(cdpCommand)

  def terminateFlow(timeout: FiniteDuration): Future[List[CdpResponse]] = {
    actorSystem.scheduler.scheduleOnce(timeout) {
      flow._1.complete()
    }
    flow._2
  }

}
