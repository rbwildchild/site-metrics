package com.rfa.metrics.webdriver.processor

import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.operation.TimedOperation
import org.openqa.selenium.{WebDriver}

class TransactionProcessor(baseUrl: String, implicit val driver: WebDriver) {
  def doCommand(commandName: String, args: Array[String]): String = {
    //super.doCommand(commandName, args)
    ""
  }

  def doTimedCommand(commandName: String, args: Array[String]): (String, Time) = {
    TimedOperation.doTimed(doCommand(commandName, args))
  }
}
