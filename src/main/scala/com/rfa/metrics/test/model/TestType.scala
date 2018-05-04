package com.rfa.metrics.test.model

sealed trait TestType {
  def name: String

  override def toString: String = name
}

object PageLoad extends TestType { val name = "PageLoad" }
object Transaction extends TestType { val name = "Transaction" }
