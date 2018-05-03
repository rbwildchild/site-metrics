package com.rfa.metrics.webdriver.operation

import com.rfa.metrics.timing.Time
import org.openqa.selenium.WebDriver

object TimedOperation {
  def doTimed[R](cmd: => R)(implicit lock: WebDriver): (R, Time) = {
    lock.synchronized {
      val start = System.nanoTime() / 1000000.0
      val result = cmd
      val end = System.nanoTime() / 1000000.0
      (result, new Time(start, end))
    }
  }
}
