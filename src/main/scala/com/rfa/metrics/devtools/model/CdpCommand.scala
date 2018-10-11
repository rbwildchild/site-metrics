package com.rfa.metrics.cdp.model

case class CdpCommand(id: Int, method: String, params: Option[Map[String, Any]] = None)
