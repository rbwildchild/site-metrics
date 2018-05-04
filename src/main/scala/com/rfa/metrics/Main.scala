package com.rfa.metrics



import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.rfa.metrics.actor.Tester
import com.rfa.metrics.test.model.{PageLoadTest, TestConfiguration}
import java.util.concurrent.TimeUnit

import com.rfa.metrics.actor.Tester.{BrowserReady, ExecuteTest}

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  val testerActor = system.actorOf(Props(classOf[Tester]), "Tester")
  implicit val timeout = Timeout(5, TimeUnit.SECONDS)
  val start = testerActor ? Tester.StartBrowser()
  start.onComplete {
    case s: Success[BrowserReady] => executeTest()
    case f: Failure[AnyRef] => println(f)
  }

  private def executeTest(): Unit = {
    testerActor ! ExecuteTest(new PageLoadTest(new TestConfiguration("https://itunes.apple.com", 30)))
  }

}

