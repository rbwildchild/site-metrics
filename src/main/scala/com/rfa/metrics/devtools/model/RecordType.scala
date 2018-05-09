package com.rfa.metrics.devtools.model

sealed trait RecordType {
  def name: String

  override def toString: String = name
}

object Network extends RecordType { val name = "Network.enable" }
