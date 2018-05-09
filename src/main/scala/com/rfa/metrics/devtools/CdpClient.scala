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

import scala.concurrent.{Await, Future, Promise}

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

  implicit val cdpCommandFormat: RootJsonFormat[CdpCommand] = jsonFormat3(CdpCommand)
  implicit val cdpResponseFormat: RootJsonFormat[CdpResponse] = jsonFormat4(CdpResponse)

  def connect(url: String, commands: List[CdpCommand]): Unit = {
    {

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()
      import system.dispatcher

      val processor: Sink[CdpResponse, Future[Done]] = Flow[CdpResponse].toMat(Sink.foreach(println))(Keep.right)

      val incoming: Sink[Message, Future[Done]] = Flow[Message].map {
        case t: TextMessage.Strict => cdpResponseFormat.read(JsonParser(t.text))
      }.toMat(processor)(Keep.right)

      val outgoing: Source[CdpCommand, Promise[Option[CdpCommand]]] =
        Source(commands).concatMat(Source.maybe[CdpCommand])(Keep.right)

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

    }
  }
}
