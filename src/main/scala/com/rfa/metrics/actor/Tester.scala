package com.rfa.metrics.actor

import akka.actor.{Actor, ActorRef, Props}
import com.rfa.metrics.actor.Tester._
import com.rfa.metrics.test.model._

object Tester {

  case class StartBrowser()
  case class BrowserReady()
  case class ExecuteTest(test: Test)
  case class TestFinished(result: String)

  private var agentActor: ActorRef = _
}

class Tester extends Actor {

  val browserActor = context.system.actorOf(Props(new Browser(self)), "Browser")

  override def receive = {
    case startTest: StartBrowser => {
      browserActor ! Browser.StartBrowser
      Tester.agentActor = sender
    }
    case browserReady: BrowserReady => {
      context.become(ready)
      Tester.agentActor ! BrowserReady
    }
    case _ => println("Invalid state in receive")
  }

  def ready: Receive = {
    case execute: ExecuteTest => {
      browserActor.forward(execute)
      Tester.agentActor = sender
    }
    case t: TestFinished => Tester.agentActor.forward(t)
    case _ => println("Invalid state in ready")
  }
}
