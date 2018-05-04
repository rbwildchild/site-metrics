package com.rfa.metrics.test.model

case class PageLoadTest(val testConfiguration: TestConfiguration, val testType: TestType = PageLoad) extends Test
