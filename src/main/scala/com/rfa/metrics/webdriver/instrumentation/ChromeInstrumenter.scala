package com.rfa.metrics.webdriver.instrumentation

import java.util.concurrent.TimeUnit

import com.rfa.metrics.devtools.Devtools
import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.operation.TimedOperation
import org.openqa.selenium.{By, WebDriver}

object ChromeInstrumenter {

  def apply(driver: WebDriver): ChromeInstrumenter = {
    val port = getCDPPort(driver)
    val devtoolsClient = Devtools(port)
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

class ChromeInstrumenter(webDriver: WebDriver, devtools: Devtools) {

  implicit val driver = webDriver

  private def load (url: String, timeout: Long): String = {
    val devtoolsClient: Devtools.Client = devtools.attachTab()
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
