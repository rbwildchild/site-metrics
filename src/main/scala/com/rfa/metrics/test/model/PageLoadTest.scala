package com.rfa.metrics.test.model

case class PageLoadTest(testConfiguration: TestConfiguration) extends Test {
  val testType: TestType = PageLoad
}
