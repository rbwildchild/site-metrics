package com.rfa.metrics.webdriver.instrumentation

import java.util.concurrent.TimeUnit

import com.rfa.metrics.cdp.CdpClient
import com.rfa.metrics.cdp.model.CdpConnection
import com.rfa.metrics.timing.Time
import com.rfa.metrics.webdriver.operation.TimedOperation
import org.openqa.selenium.{By, WebDriver}
import spray.json.{DefaultJsonProtocol, JsArray, JsonParser}
import DefaultJsonProtocol._

object ChromeInstrumenter {

  def apply(webDriver: WebDriver): ChromeInstrumenter = {
    new ChromeInstrumenter(webDriver)
  }
}

class ChromeInstrumenter(webDriver: WebDriver) {

  object MyJsonProtocol extends DefaultJsonProtocol {
    implicit val cdpFormat = jsonFormat6(CdpConnection)

  }

  import MyJsonProtocol._

  implicit val driver = webDriver
  val cdpPort = getCDPPort()

  private def load (url: String, timeout: Long): String = {
    webDriver.manage().timeouts().implicitlyWait(timeout, TimeUnit.SECONDS)
    webDriver.navigate().to(url)
    //val processor = new TransactionProcessor(url, webDriver)
    //processor.doTimedCommand("click", Array("link=Music"))
    webDriver.getTitle
  }

  def loadPage (url: String, timeout: Long): (String, Time) = {
    val res = TimedOperation.doTimed(load(url, timeout))
    CdpClient.connect(getCDPUrl())
    res
  }

  def getCDPPort (): String = {
    webDriver.navigate().to("chrome://version")
    webDriver.getPageSource
    val cmdLine = webDriver.findElement(By.id("command_line")).getText
    Option(cmdLine.split(" ").filter(_.contains("--remote-debugging-port"))(0)).getOrElse("=").split("=")(1)
  }

  def getCDPConnections(): Array[CdpConnection] = {
    val cdpUrl = "http://localhost:" + cdpPort + "/json"
    val result = scala.io.Source.fromURL(cdpUrl).mkString
    val jsonAst: JsArray = JsonParser(result).asInstanceOf[JsArray]
    jsonAst.elements.map(_.asJsObject.convertTo[CdpConnection]).toArray
  }

  def getCDPUrl(): String = {
    val cdpConns = getCDPConnections()
    cdpConns(cdpConns.length -1 ).webSocketDebuggerUrl
  }

  def sleep (millis: Long): (Unit, Time) = {
    TimedOperation.doTimed(webDriver.wait(millis))
  }
}
