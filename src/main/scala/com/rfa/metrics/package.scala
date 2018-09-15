package com.rfa

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import spray.json.{JsFalse, JsNumber, JsObject, JsString, JsTrue, JsValue, JsonFormat}

package object metrics {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  implicit object AnyJsonFormat extends JsonFormat[Any] {
    def write(x: Any) = x match {
      case n: Int  => JsNumber(n)
      case n: Double  => JsNumber(n)
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
}
