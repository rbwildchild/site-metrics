package com.rfa.metrics.cdp

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Attributes, OverflowStrategy, QueueOfferResult}
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
      case JsNumber(n) => {
        if (n.isValidInt)
            n.intValue()
        else if (n.isValidLong)
            n.longValue()
        else n.doubleValue()
      }
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

  private def connect(url: String):
  Tuple3[SourceQueueWithComplete[CdpCommand], SinkQueueWithCancel[CdpResponse], Future[List[CdpResponse]]] = {
    {

      val collection = Flow[CdpResponse]
        .filter(_.id.isEmpty)
        .toMat(Sink.collection[CdpResponse, List[CdpResponse]])(Keep.right)

      val queue = Flow[CdpResponse]
        .filter(_.id.nonEmpty)
        .toMat(Sink.queue[CdpResponse])(Keep.right)

      val outgoing: Source[CdpCommand, SourceQueueWithComplete[CdpCommand]] =
        Source.queue[CdpCommand](1, OverflowStrategy.dropHead)

      val webSocketFlow = Http().webSocketClientFlow(WebSocketRequest(url))
        .map {
          case t: TextMessage.Strict => {
            cdpResponseFormat.read(JsonParser(t.text))
          }
        }

      val (((sourceQueue, upgradeResponse), sinkQueue), sinkCollection) =
      outgoing
        .map(c => TextMessage(cdpCommandFormat.write(c).toString()))
        .viaMat(webSocketFlow)(Keep.both)
        .alsoToMat(queue)(Keep.both)
        .toMat(collection)(Keep.both)
        .run()

      val connected = upgradeResponse.flatMap { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Future.successful(Done)
        } else {
          throw new RuntimeException(s"Connection failed: ${upgrade.response.status}")
        }
      }

      connected.onComplete(println)

      (sourceQueue, sinkQueue, sinkCollection)

    }
  }
}

class CdpClient(flow: Tuple3[SourceQueueWithComplete[CdpCommand], SinkQueueWithCancel[CdpResponse], Future[List[CdpResponse]]])
               (implicit actorSystem: ActorSystem, materializer: ActorMaterializer) {

  def sendCommands(cdpCommands: List[CdpCommand]): Future[Done] = {
    Source(cdpCommands)
      .mapAsync(parallelism = 1)(sendCommand)
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }

  def sendCommand(cdpCommand: CdpCommand): Future[Done] = flow._1.offer(cdpCommand).flatMap {
    case QueueOfferResult.Enqueued => flow._2.pull()
    case _ => Future.failed(new Exception("Error when offering command"))
  }.flatMap {
    case s: Some[CdpResponse] => {
      if (s.nonEmpty && s.get.id.get == cdpCommand.id) Future.successful(Done)
      else Future.failed(new Exception("Error id's don't match"))
    }
    case _ => Future.failed(new Exception("Error when pulling response"))
  }

  def terminateFlow(timeout: FiniteDuration): Future[List[CdpResponse]] = {
    actorSystem.scheduler.scheduleOnce(timeout) {
      flow._1.complete()
      flow._2.cancel()
    }
    flow._3
  }

}
