package net.andrecarbajal.telegramdiscountsbot.scrapping

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ScrappingTest {
    @Test
    fun extractProductId_withMifarmaUrl_returnsLastNumericPathSegment() {
        val url = "https://www.mifarma.com.pe/producto/fotoprotector-isdin-fusion-water-magic-spf50/069106"

        assertEquals("069106", extractProductId(url))
    }

    @Test
    fun extractProductId_withInkafarmaUrl_returnsLastNumericPathSegment() {
        val url = "https://inkafarma.pe/producto/protector-solar-gel-crema-anthelios-xl-spf-50-colo/010816"

        assertEquals("010816", extractProductId(url))
    }

    @Test
    fun extractProductId_withTrailingSlash_returnsProductId() {
        val url = "https://www.mifarma.com.pe/producto/anthelios-toque-seco-antibrillo-s-color-la-roche-p/010853/"

        assertEquals("010853", extractProductId(url))
    }

    @Test
    fun extractProductId_withAlphanumericPackUrl_returnsProductId() {
        val url = "https://inkafarma.pe/producto/pack-02-systane-ultra-solucion-oftalmica/PACKFC58"

        assertEquals("PACKFC58", extractProductId(url))
    }

    @Test
    fun extractProductId_withInvalidUrl_returnsNull() {
        assertNull(extractProductId("https://www.mifarma.com.pe/producto/no-id"))
    }

    @Test
    fun parseProductJson_withOffer_mapsProductFields() {
        val json = """
            {
              "name": "Fotoprotector ISDIN Fusion Water Magic SPF50",
              "price": 114.90,
              "priceHighDiscount": 99.90,
              "priceWithpaymentMethod": 0,
              "imageList": [
                {"url": "https://dcuk1cxrnzjkh.cloudfront.net/imagesproducto/069106X.jpg"}
              ]
            }
        """.trimIndent()

        val product = parseProductJson(json, "Mifarma")

        assertEquals("Mifarma", product?.pharmacy)
        assertEquals("Fotoprotector ISDIN Fusion Water Magic SPF50", product?.name)
        assertEquals("S/ 114.9", product?.price)
        assertEquals("S/ 99.9", product?.offer)
        assertEquals("https://dcuk1cxrnzjkh.cloudfront.net/imagesproducto/069106X.jpg", product?.imageUrl)
        assertEquals(listOf("Mifarma", product?.name, "S/ 114.9", "S/ 99.9", product?.imageUrl), product?.toLegacyList())
    }

    @Test
    fun parseProductJson_withPaymentMethodOffer_mapsOfferFromPriceAllPaymentMethod() {
        val json = """
            {
              "name": "Pack 02 Systane Ultra Solución Oftálmica",
              "price": 129.60,
              "priceHighDiscount": 0,
              "priceWithpaymentMethod": 0,
              "priceAllPaymentMethod": 110.20,
              "imageList": [
                {"url": "https://dcuk1cxrnzjkh.cloudfront.net/imagesproducto/PACKFC58X.jpg"}
              ]
            }
        """.trimIndent()

        val product = parseProductJson(json, "Inkafarma")

        assertEquals("Inkafarma", product?.pharmacy)
        assertEquals("Pack 02 Systane Ultra Solución Oftálmica", product?.name)
        assertEquals("S/ 129.6", product?.price)
        assertEquals("S/ 110.2", product?.offer)
        assertEquals("https://dcuk1cxrnzjkh.cloudfront.net/imagesproducto/PACKFC58X.jpg", product?.imageUrl)
        assertEquals(listOf("Inkafarma", product?.name, "S/ 129.6", "S/ 110.2", product?.imageUrl), product?.toLegacyList())
    }

    @Test
    fun parseProductJson_withPaymentMethodPriceHigherThanBasePrice_keepsOfferNull() {
        val json = """
            {
              "name": "Product without discount",
              "price": 100.00,
              "priceHighDiscount": 0,
              "priceWithpaymentMethod": 0,
              "priceAllPaymentMethod": 120.00,
              "imageList": []
            }
        """.trimIndent()

        val product = parseProductJson(json, "Inkafarma")

        assertEquals("S/ 100", product?.price)
        assertNull(product?.offer)
    }

    @Test
    fun parseProductJson_withoutOffer_keepsOfferNull() {
        val json = """
            {
              "name": "Protector Solar La Roche Posay Anthelios",
              "price": 125.90,
              "priceHighDiscount": 0,
              "priceWithpaymentMethod": 0,
              "imageList": [
                {"url": "https://dcuk1cxrnzjkh.cloudfront.net/imagesproducto/010816X.jpg"}
              ]
            }
        """.trimIndent()

        val product = parseProductJson(json, "Inkafarma")

        assertEquals("S/ 125.9", product?.price)
        assertNull(product?.offer)
    }
}
