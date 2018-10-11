package com.rfa.metrics.webdriver.instrumentation

import java.util.concurrent.TimeUnit

import akka.Done
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.rfa.metrics.devtools.Devtools
import com.rfa.metrics.test.model.{Command, TransactionTest}
import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.operation.TimedOperation
import com.thoughtworks.selenium.webdriven.WebDriverCommandProcessor
import org.apache.commons.lang3.StringUtils
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

  private def execute (url: String, timeout: Long, commands: Array[Command]): Future[String] = {
    val devtoolsClient: Devtools.Client = devtools.attachTab()
    devtoolsClient
      .startRecord()
      .flatMap {
        case Done => {
          val processor = new WebDriverCommandProcessor(url, driver);
          driver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS)
          var i = 0
          commands.foreach(c => {
            val args: Array[String] = getArgs(c)
            println(s"EXECUTING #${i = i + 1;i;}: '${c.command}' with args [" +
              s"${if(args.length > 0) args(0)}, ${if(args.length > 1) args(1)}]")
            if (c.command.equalsIgnoreCase("sleep")) {
              sleep(args(0).toLong)
            } else {
              processor.doCommand(c.command, args);
            }
          })
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

  private def getArgs(command: Command): Array[String] = {
    val args: Array[String] = Array(command.target, command.value)
    args.filter(s => !StringUtils.isEmpty(s))
  }

  def execute(transactionTest: TransactionTest): Future[String] ={
    execute(transactionTest.testConfiguration.url, transactionTest.testConfiguration.timeout, transactionTest.commands)
  }

  def loadPage (url: String, timeout: Long): Future[String] = {
    load(url, timeout)
  }

  def sleep (millis: Long): (Unit, Time) = {
    TimedOperation.doTimed(driver.wait(millis))
  }
}
