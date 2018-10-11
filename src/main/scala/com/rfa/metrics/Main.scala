package com.rfa.metrics



import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.rfa.metrics.actor.Tester
import com.rfa.metrics.test.model.{Command, PageLoadTest, TestConfiguration, TransactionTest}
import java.util.concurrent.TimeUnit

import com.rfa.metrics.actor.Tester.{BrowserReady, ExecuteTest, TestFinished}

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val testerActor = system.actorOf(Props(classOf[Tester]), "Tester")
  implicit val timeout = Timeout(30, TimeUnit.SECONDS)
  val start = testerActor ? Tester.StartBrowser()
  start.onComplete {
    case s: Success[BrowserReady] => executeTest()
    case f: Failure[AnyRef] => println(f)
  }

  /*private def executeTest(): Unit = {
    val execute = testerActor ? ExecuteTest(new PageLoadTest(new TestConfiguration("https://twitter.com", 30)))
    execute.onComplete {
      case s: Success[TestFinished] => println("RESULT: " + s.get.result)
      case f: Failure[AnyRef] => println(f)
    }
  }*/

  private def executeTest(): Unit = {
    val execute = testerActor ? ExecuteTest(new TransactionTest(
      TestConfiguration(
        "https://github.com/",
        10
      ),
      Array(
        Command("open", "/", "")
        ,Command("waitForPageToLoad", "", "6000")
        ,Command("sleep", "", "2000")
        //,Command("runScript", "window.scrollTo(0, document.body.scrollHeight - 2 * 812)", "")
        //,Command("click", "//a[@href='https://help.github.com/terms' and @class='text-white']", "")
        ,Command("click", "//a[@href='/open-source']", "")
        ,Command("sleep", "", "2000")
      )
    ))
    execute.onComplete {
      case s: Success[TestFinished] => println("RESULT: " + s.get.result)
      case f: Failure[AnyRef] => println(f)
    }
  }

}

