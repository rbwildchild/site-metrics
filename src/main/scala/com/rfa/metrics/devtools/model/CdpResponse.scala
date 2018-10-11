package com.rfa.metrics.devtools.model

import scala.collection.immutable.Map

trait ParamResponse {
  def params: Option[Map[String, Any]]

  def getParam[T](key: String): Option[T] = {
    try {
      val value = if (params.nonEmpty) params.get.get(key) else None
      if (value.nonEmpty) value.asInstanceOf[Option[T]] else None
    } catch {
      case cce: ClassCastException => {
        println("Error while casting: " + cce.getMessage)
        throw cce
      }
      case e: Exception => {
        println("Error while casting: " + e.getMessage)
        throw e
      }
    }
  }
}

case class CdpResponse(id: Option[Int] = None, result: Option[Map[String, Any]] = None, method: Option[String] = None, params: Option[Map[String, Any]] = None) extends ParamResponse
