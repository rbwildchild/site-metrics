package com.rfa.metrics.cdp.model

case class CdpMessage(id: Int, method: String, params: Option[Map[String, Any]] = None)
