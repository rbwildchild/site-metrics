package com.rfa.metrics.cdp

import akka.actor.ActorSystem
import akka.actor.Status.Success
import akka.http.javadsl.model.HttpEntity.Strict
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ws._
import com.rfa.metrics.cdp.model.{CdpConnection, CdpMessage}
import spray.json.{DefaultJsonProtocol, JsFalse, JsNumber, JsString, JsTrue, JsValue, JsonFormat, JsonParser, RootJsonFormat}
import spray.json.DefaultJsonProtocol._

import scala.concurrent.{Future, Promise}

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

  implicit val cdpMessageFormat: RootJsonFormat[CdpMessage] = jsonFormat3(CdpMessage)

  val inst = List(
    TextMessage(
      cdpMessageFormat.write(new CdpMessage(
        2,
        "Network.enable"
      )).toString
    ),
    TextMessage(
      cdpMessageFormat.write(new CdpMessage(
        3,
        "Page.navigate",
        Option(Map(
          ("url", "https://twitter.com")
        ))
      )).toString
    )
  )

  def connect(url: String) = {
    {
      //println(url)
      //println(inst)

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()
      import system.dispatcher

      val incoming: Sink[Message, Future[Done]] =
        Flow[Message].mapAsync(4) {
          case message: TextMessage.Strict =>
            println(message.text)
            Future.successful(Done)
          case message: TextMessage.Streamed =>
            message.textStream.runForeach(println)
          case message: BinaryMessage =>
            message.dataStream.runWith(Sink.ignore)
        }.toMat(Sink.last)(Keep.right)

      val outgoing: Source[TextMessage.Strict, Promise[Option[Nothing]]] =
        Source(inst).concatMat(Source.maybe)(Keep.right)


      val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))

      val ((completionPromise, upgradeResponse), closed) =
      outgoing
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
      closed.foreach(_ => println("closed"))
    }
  }
}
