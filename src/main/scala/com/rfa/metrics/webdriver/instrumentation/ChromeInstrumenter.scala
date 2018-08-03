package com.rfa.metrics.webdriver.instrumentation

import java.util.concurrent.TimeUnit

import akka.Done
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.rfa.metrics.devtools.Devtools
import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.operation.TimedOperation
import org.openqa.selenium.{By, WebDriver}

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.Success

import scala.concurrent.ExecutionContext.Implicits.global

object ChromeInstrumenter {

  def apply(driver: WebDriver): ChromeInstrumenter = {
    val devtoolsClient = Devtools(9222)
    openBlank(driver)
    new ChromeInstrumenter(driver, devtoolsClient)
  }

  def openBlank(driver: WebDriver): Unit = {
    driver.get("about:blank")
  }

}

class ChromeInstrumenter(webDriver: WebDriver, devtools: Devtools) {

  implicit val driver = webDriver

  private def load (url: String, timeout: Long): Future[String] = {
    val devtoolsClient: Devtools.Client = devtools.attachTab()
    devtoolsClient
      .startRecord()
      .flatMap {
        case Done => {
          driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS)
          driver.navigate().to(url)
          Future.successful(Done)
        }
        case _ => Future.failed(new Exception("Could not start record"))
      }
      .flatMap {
        case Done => devtoolsClient.stopRecord()
        case _ => Future.failed(new Exception("Error"))
      }
      .flatMap {
        case Done => Future.successful(devtoolsClient.getHar())
        case _ => Future.failed(new Exception("Error"))
      }
  }

  def loadPage (url: String, timeout: Long): Future[String] = {
    load(url, timeout)
  }

  def sleep (millis: Long): (Unit, Time) = {
    TimedOperation.doTimed(driver.wait(millis))
  }
}
