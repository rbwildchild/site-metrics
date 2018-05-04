package com.rfa.metrics.test.model

case class TransactionTest(val testConfiguration: TestConfiguration, val testType: TestType = Transaction) extends Test
