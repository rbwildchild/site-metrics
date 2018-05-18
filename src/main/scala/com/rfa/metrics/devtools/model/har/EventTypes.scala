package com.rfa.metrics.devtools.model.har

sealed trait EventType {
  def name: String
  def order: Int

  override def toString: String = name
}

object EventType {
  object RequestWillBeSent extends EventType {
    val name = "Network.requestWillBeSent"
    val order = 0
  }

  object ResponseReceived extends EventType {
    val name = "Network.responseReceived"
    val order = 1
  }

  object DataReceived extends EventType {
    val name = "Network.dataReceived"
    val order = 2
  }

  object LoadingFinished extends EventType {
    val name = "Network.loadingFinished"
    val order = 3
  }
}

sealed trait RequestType {
  def name: String

  override def toString: String = name
}

object RequestType {
  object Document extends RequestType { val name = "Document" }
}
