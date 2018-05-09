package com.rfa.metrics.devtools.model

import scala.collection.immutable.Map

case class CdpResponse(id: Option[Int] = None, result: Option[Map[String, Any]] = None, method: Option[String] = None, params: Option[Map[String, Any]] = None)
