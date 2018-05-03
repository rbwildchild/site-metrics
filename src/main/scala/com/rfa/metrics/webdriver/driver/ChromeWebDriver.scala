package com.rfa.metrics.webdriver.driver

import java.io.File

import org.openqa.selenium.chrome.{ChromeDriver, ChromeDriverService, ChromeOptions}
import org.openqa.selenium.remote.{DesiredCapabilities}

object ChromeWebDriver {
  def apply(): ChromeWebDriver = new ChromeWebDriver()

  private def createService : ChromeDriverService = {
    new ChromeDriverService.Builder()
      .usingDriverExecutable(new File(System.getProperty("user.home") + "/Documents/projects/browserbot/support/chromedriver"))
      .usingAnyFreePort()
      .build()
  }

  private def tweakProfile: DesiredCapabilities = {
    val chrome = new File("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome")
    val caps: DesiredCapabilities = DesiredCapabilities.chrome()
    val options: ChromeOptions = new ChromeOptions
    options.setBinary(chrome)
    //options.addArguments("--headless")
    //options.addArguments("--disable-gpu")
    caps.setCapability(ChromeOptions.CAPABILITY, options)
    caps
  }
}

class ChromeWebDriver extends ChromeDriver(ChromeWebDriver.createService, ChromeWebDriver.tweakProfile) {

}
