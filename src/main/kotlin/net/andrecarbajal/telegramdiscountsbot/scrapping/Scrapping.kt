package net.andrecarbajal.telegramdiscountsbot.scrapping

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private val logger: Logger = LoggerFactory.getLogger("Scrapping")

private const val MIFARMA_API_BASE = "https://5doa19p9r7.execute-api.us-east-1.amazonaws.com/MMMFPRD/product"
private const val INKAFARMA_API_BASE = "https://5doa19p9r7.execute-api.us-east-1.amazonaws.com/MMPROD/product"
private val httpTimeout: Duration = Duration.ofSeconds(20)

private val httpClient: HttpClient = HttpClient.newBuilder()
    .connectTimeout(httpTimeout)
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

private val objectMapper = ObjectMapper()

data class ScrapedProduct(
    val pharmacy: String,
    val name: String,
    val price: String,
    val offer: String?,
    val imageUrl: String?
) {
    fun toLegacyList(): List<String?> = listOf(pharmacy, name, price, offer, imageUrl)
}

private enum class Pharmacy(
    val displayName: String,
    val apiBaseUrl: String
) {
    MIFARMA("Mifarma", MIFARMA_API_BASE),
    INKAFARMA("Inkafarma", INKAFARMA_API_BASE)
}

private fun scrapeProduct(url: String, pharmacy: Pharmacy): List<String?>? {
    val productId = extractProductId(url)
    if (productId == null) {
        logger.warn("Could not extract product id for {} url={}", pharmacy.displayName, url)
        return null
    }

    val endpoint = "${pharmacy.apiBaseUrl}/$productId"
    return try {
        logger.info(
            "Fetching product data pharmacy={} productId={} endpoint={}",
            pharmacy.displayName,
            productId,
            endpoint
        )
        val request = HttpRequest.newBuilder(URI.create(endpoint))
            .timeout(httpTimeout)
            .header("Accept", "application/json")
            .header("User-Agent", "NaviDiscountsBot/1.0")
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            logger.warn(
                "Product API returned non-success status pharmacy={} productId={} status={} endpoint={}",
                pharmacy.displayName,
                productId,
                response.statusCode(),
                endpoint
            )
            return null
        }

        parseProductJson(response.body(), pharmacy.displayName)?.also { product ->
            if (product.offer == null) {
                logger.info(
                    "Product has no offer pharmacy={} productId={} price={} url={}",
                    pharmacy.displayName,
                    productId,
                    product.price,
                    url
                )
            } else {
                logger.info(
                    "Product offer found pharmacy={} productId={} price={} offer={} url={}",
                    pharmacy.displayName,
                    productId,
                    product.price,
                    product.offer,
                    url
                )
            }
        }?.toLegacyList()
    } catch (e: Exception) {
        logger.error(
            "Error fetching product data pharmacy={} productId={} endpoint={} url={}",
            pharmacy.displayName,
            productId,
            endpoint,
            url,
            e
        )
        null
    }
}

internal fun parseProductJson(json: String, pharmacy: String): ScrapedProduct? {
    val root = objectMapper.readTree(json)
    val name = root.textValue("name") ?: return null.also {
        logger.warn("Product API response missing name pharmacy={}", pharmacy)
    }
    val price = root.decimalValue("price") ?: return null.also {
        logger.warn("Product API response missing price pharmacy={} productName={}", pharmacy, name)
    }
    val offerPrice = root.firstDiscountPrice(price, "priceHighDiscount", "priceWithpaymentMethod", "priceAllPaymentMethod")
    val imageUrl = root.path("imageList")
        .takeIf { it.isArray && it.size() > 0 }
        ?.get(0)
        ?.textValue("url")

    return ScrapedProduct(
        pharmacy = pharmacy,
        name = name,
        price = formatCurrency(price),
        offer = offerPrice?.let { formatCurrency(it) },
        imageUrl = imageUrl
    )
}

internal fun extractProductId(url: String): String? {
    val path = runCatching { URI.create(url).path }.getOrNull() ?: return null
    return path.trimEnd('/')
        .substringAfterLast('/', missingDelimiterValue = "")
        .takeIf { it.matches(Regex("[A-Za-z0-9]+")) }
}

private fun JsonNode.firstDiscountPrice(price: BigDecimal, vararg fieldNames: String): BigDecimal? = fieldNames
    .asSequence()
    .mapNotNull { decimalValue(it) }
    .firstOrNull { it > BigDecimal.ZERO && it < price }

private fun JsonNode.decimalValue(fieldName: String): BigDecimal? = path(fieldName)
    .takeIf { it.isNumber }
    ?.decimalValue()

private fun JsonNode.textValue(fieldName: String): String? = path(fieldName)
    .takeUnless { it.isMissingNode || it.isNull }
    ?.asText()
    ?.takeIf { it.isNotBlank() }

private fun formatCurrency(value: BigDecimal): String = "S/ ${value.stripTrailingZeros().toPlainString()}"

fun scrappingMifarma(url: String): List<String?>? = scrapeProduct(url, Pharmacy.MIFARMA)

fun scrappingInkaFarma(url: String): List<String?>? = scrapeProduct(url, Pharmacy.INKAFARMA)
