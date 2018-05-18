package com.rfa.metrics.devtools.model

sealed trait RecordType {
  def prefix: String

  override def toString: String = prefix
}
object RecordType {

  object Network extends RecordType {
    val prefix = "Network."
  }

  object Page extends RecordType {
    val prefix = "Page."
  }

  object Target extends RecordType {
    val prefix = "Target."
  }

}
