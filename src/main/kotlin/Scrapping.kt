package net.andrecarbajal

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

private fun scrapeWebsite(url: String, cssSelector: String) {
    val driver = EdgeDriver()
    try {
        driver.get(url)

        val wait = WebDriverWait(driver, Duration.ofSeconds(30))
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)))

        val doc: Document = Jsoup.parse(driver.pageSource!!)

        val elementText = doc.select(cssSelector).getOrNull(1)?.text() ?: "There is not an offer"

        if (elementText == "There is not an offer") {
            println(elementText)
        } else {
            println("The price in the offer is: $elementText")
        }
    } finally {
        driver.quit()
    }
}

fun scrappingMifarma(url: String) {
    scrapeWebsite(
        url,
        "div.col-xs-4.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount"
    )
}

fun scrappingInkaFarma(url: String) {
    scrapeWebsite(
        url,
        "div.col-xs-5.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount"
    )
}