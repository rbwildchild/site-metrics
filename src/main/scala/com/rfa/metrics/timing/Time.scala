package com.rfa.metrics.timing

class Time(start: Double, end: Double) {
  def elapse = end - start

  override def toString: String = elapse.toString
}
