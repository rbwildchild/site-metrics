package com.rfa.metrics.webdriver.instrumentation

import java.util.concurrent.TimeUnit

import com.rfa.metrics.devtools.DevtoolsClient
import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.operation.TimedOperation
import org.openqa.selenium.{By, WebDriver}

object ChromeInstrumenter {

  def apply(driver: WebDriver): ChromeInstrumenter = {
    val port = getCDPPort(driver)
    val devtoolsClient = DevtoolsClient(port)
    openBlank(driver)
    new ChromeInstrumenter(driver, devtoolsClient)
  }

  def openBlank(driver: WebDriver): Unit = {
    driver.get("about:blank")
  }

  def getCDPPort (webDriver: WebDriver): Int = {
    webDriver.navigate().to("chrome://version")
    webDriver.getPageSource
    val cmdLine = webDriver.findElement(By.id("command_line")).getText
    Option(cmdLine.split(" ").filter(_.contains("--remote-debugging-port"))(0)).getOrElse("=").split("=")(1).toInt
  }
}

class ChromeInstrumenter(webDriver: WebDriver, devtoolsClient: DevtoolsClient) {

  implicit val driver = webDriver

  private def load (url: String, timeout: Long): String = {
    driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS)
    driver.navigate().to(url)
    driver.getTitle
  }

  def loadPage (url: String, timeout: Long): (String, Time) = {
    TimedOperation.doTimed(load(url, timeout))
  }

  def sleep (millis: Long): (Unit, Time) = {
    TimedOperation.doTimed(driver.wait(millis))
  }
}
