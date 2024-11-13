package net.andrecarbajal

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class Scrapping {

    companion object {
        fun scrap(url: String) {
//            val driver: WebDriver = EdgeDriver()
//            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20))
//            driver.get(url)
//
//            println(driver.pageSource)
//
//            driver.quit()

            val doc: Document = Jsoup.connect(url).get()

            val elements: Elements = doc.getElementsByTag("span")

            for (element in elements) {
                println(element)
            }
        }
    }
}