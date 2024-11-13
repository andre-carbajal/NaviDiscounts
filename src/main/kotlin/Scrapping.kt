package net.andrecarbajal

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

fun scrappingMifarma(url: String) {
    val driver = EdgeDriver()
    driver.get(url)

    val wait = WebDriverWait(driver, Duration.ofSeconds(30))
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.col-xs-4.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount")))

    val doc: Document = Jsoup.parse(driver.pageSource!!)

    driver.quit()

    try {
        val element = doc.select("div.col-xs-4.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount")[1]
        println(element.text())
    } catch (e: IndexOutOfBoundsException) {
        println("Element not found")
    }
}

fun scrappingInkaFarma(url: String) {
    val driver = EdgeDriver()
    driver.get(url)

    val wait = WebDriverWait(driver, Duration.ofSeconds(30))
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector()))

    val doc: Document = Jsoup.parse(driver.pageSource!!)

    driver.quit()

    try {
        val element = doc.select()[0]
        println(element.text())
    } catch (e: IndexOutOfBoundsException) {
        println("Element not found")
    }
}