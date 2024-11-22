package net.andrecarbajal.telegramdiscountsbot.scrapping

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

private val logger: Logger = LoggerFactory.getLogger("Scrapping")

private fun scrapeWebsite(
    url: String,
    nameCssSelector: String,
    priceCssSelector: String,
    offerCssSelector: String
): List<String?>? {
    val waitCssSelector = "div.product-detail-content"
    val imageCssSelector = "img.ngxImageZoomThumbnail"

    val options = FirefoxOptions()
    options.addArguments("--headless")
    options.addArguments("--no-sandbox")

    val driver: WebDriver = try {
        FirefoxDriver(options)
    } catch (e: Exception) {
        logger.error("Error occurred while creating FirefoxDriver", e)
        return null
    }

    return try {
        driver.get(url)
        val wait = WebDriverWait(driver, Duration.ofMinutes(5))
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(waitCssSelector)))

        val doc: Document = Jsoup.parse(driver.pageSource!!)
        val name = doc.select(nameCssSelector).firstOrNull()?.text() ?: "Name not found"
        val price = doc.select(priceCssSelector).firstOrNull()?.text() ?: "Price not found"
        val offer = doc.select(offerCssSelector).getOrNull(1)?.text()
        var img = doc.select(imageCssSelector).firstOrNull()?.attr("src")

        listOf(name, price, offer, img)
    } catch (e: Exception) {
        logger.error("Error occurred during scraping", e)
        null
    } finally {
        driver.quit()
    }
}

fun scrappingMifarma(url: String): List<String?>? {
    val result = scrapeWebsite(
        url,
        "h1[class\$='mb-0']",
        "div.col-xs-4.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount",
        "div.col-xs-4.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount"
    ) ?: return null
    return listOf("MiFarma", result[0], result[1], result[2], result[3])
}

fun scrappingInkaFarma(url: String): List<String?>? {
    val result = scrapeWebsite(
        url,
        "div[class\$='product-detail-information']",
        "div[class*='col-lg-4']",
        "div.col-xs-5.col-sm-2.col-md-6.col-lg-4.text-right.d-flex.align-items-center.justify-content-end.price-amount"
    ) ?: return null
    return listOf("InkaFarma", result[0], result[1], result[2], result[3])
}