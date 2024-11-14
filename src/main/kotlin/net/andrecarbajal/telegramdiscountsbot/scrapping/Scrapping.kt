package net.andrecarbajal.telegramdiscountsbot.scrapping

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

private fun scrapeWebsite(url: String, cssSelector: String): String {
    val options = FirefoxOptions()
    // Execute the browser without UI
    options.addArguments("--headless")
    // Execute the browser without security
    options.addArguments("--no-sandbox")
    // Execute the browser without shared memory
    options.addArguments("--disable-dev-shm-usage")

    val driver: WebDriver = FirefoxDriver(options)

    return try {
        driver.get(url)

        val wait = WebDriverWait(driver, Duration.ofSeconds(30))
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(cssSelector)))

        val doc: Document = Jsoup.parse(driver.pageSource!!)

        val elementText = doc.select(cssSelector).getOrNull(1)?.text() ?: "There is not an offer"

        if (elementText == "There is not an offer") {
            elementText
        } else {
            "The price in the offer is: $elementText"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error occurred during scraping"
    } finally {
        driver.quit()
    }
}

fun scrappingMifarma(url: String): String {
    return scrapeWebsite(
        url,
        "div.col-xs-4.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount"
    )
}

fun scrappingInkaFarma(url: String): String {
    return scrapeWebsite(
        url,
        "div.col-xs-5.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount"
    )
}