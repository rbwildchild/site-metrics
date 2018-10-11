package com.rfa.metrics.test.model

case class TransactionTest(testConfiguration: TestConfiguration, commands: Array[Command]) extends Test {
  val testType: TestType = Transaction
}

case class Command(command: String, target: String, value: String);
