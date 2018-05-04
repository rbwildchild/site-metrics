package com.rfa

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

package object metrics {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import system.dispatcher
}
