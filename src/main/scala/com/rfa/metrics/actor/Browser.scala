package com.rfa.metrics.actor

import akka.Done
import akka.actor.{Actor, ActorRef}
import com.rfa.metrics.actor.Browser._
import com.rfa.metrics.actor.Tester.{BrowserReady, ExecuteTest, TestFinished}
import com.rfa.metrics.test.model._
import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.driver.ChromeWebDriver
import com.rfa.metrics.webdriver.instrumentation.ChromeInstrumenter

import scala.concurrent.Future
import scala.util.Success

import scala.concurrent.ExecutionContext.Implicits.global

object Browser {
  case class StartBrowser()
  case class CreateInstrumenter()
}

class Browser(tester: ActorRef) extends Actor {


  def receive = {
    case StartBrowser => {
        context.become(started(ChromeWebDriver()))
        self ! CreateInstrumenter
    }
  }

  def started(driver: ChromeWebDriver): Receive = {
    case CreateInstrumenter => {
      context.become(ready(ChromeInstrumenter(driver)))
      tester ! BrowserReady()
    }
  }

  def ready(instrumenter: ChromeInstrumenter): Receive = {
    case execute: ExecuteTest => {
      execute.test match {
        case pageLoadTest: PageLoadTest => {
          doPageLoadTest(instrumenter, pageLoadTest.testConfiguration)
        }
        case transactionTest: TransactionTest => println("Do transaction: " + transactionTest.testConfiguration)
      }
    }
  }

  def doPageLoadTest(instrumenter: ChromeInstrumenter, testConfiguration: TestConfiguration) = {
    instrumenter.loadPage(testConfiguration.url, testConfiguration.timeout).onComplete {
      case s: Success[String] => tester ! TestFinished(s.get)
    }
  }
}
