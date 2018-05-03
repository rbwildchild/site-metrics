package com.rfa.metrics


import com.rfa.metrics.webdriver.driver.ChromeWebDriver
import com.rfa.metrics.webdriver.helper.WebDriverHelper


object Main extends App {

  val driver = ChromeWebDriver()
  val helper = WebDriverHelper(driver)

  val url = "https://itunes.apple.com"
  val timeout = 30

  var result = helper.loadPage(url, timeout)
  helper.sleep(5000)

  println(result)

  //driver.quit()
}

