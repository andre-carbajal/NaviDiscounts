package net.andrecarbajal.telegramdiscountsbot.util

import net.andrecarbajal.telegramdiscountsbot.scrapping.Websites
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException

object Util {
    fun isNotValidUrl(url: String): Boolean {
        val urlRegex = "^(https?|ftp)://([a-zA-Z0-9.-]+)(:[0-9]+)?(/.*)?$"
        val urlPattern = Regex(urlRegex)

        if (!urlPattern.matches(url)) {
            return true
        }

        return try {
            val urlObj = URI(url).toURL()
            val protocol = urlObj.protocol
            val host = urlObj.host

            if (protocol == null || host == null || host.isEmpty()) {
                return true
            }

            val hostParts = host.split(".")
            if (hostParts.size < 2 || hostParts[0].isEmpty() || hostParts[hostParts.size - 1].isEmpty()) {
                true
            } else {
                Websites.entries.none { url.contains(it.url) }
            }

        } catch (_: MalformedURLException) {
            true
        } catch (_: URISyntaxException) {
            true
        }
    }

    fun boldString(text: String): String {
        return "*$text*"
    }
}