package com.rfa.metrics.devtools.model

import scala.collection.immutable.Map

trait ParamResponse {
  def params: Option[Map[String, Any]]

  def getParam[T](key: String): T = {
    try {
      params.get.get(key).getOrElse(None).asInstanceOf[T]
    } catch {
      case cce: ClassCastException => {
        println("Error while casting: " + cce.getMessage)
        throw cce
      }
    }
  }
}

case class CdpResponse(id: Option[Int] = None, result: Option[Map[String, Any]] = None, method: Option[String] = None, params: Option[Map[String, Any]] = None) extends ParamResponse
