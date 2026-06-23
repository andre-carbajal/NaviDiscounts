package net.andrecarbajal.telegramdiscountsbot.util

import java.io.File

object DotEnvLoader {
    fun loadSystemProperties(file: File = File(".env")) {
        if (!file.exists() || !file.isFile) {
            return
        }

        file.forEachLine { rawLine ->
            val line = rawLine.trim()
            if (line.isEmpty() || line.startsWith("#")) {
                return@forEachLine
            }

            val normalizedLine = line.removePrefix("export ").trim()
            val separatorIndex = normalizedLine.indexOf('=')
            if (separatorIndex <= 0) {
                return@forEachLine
            }

            val key = normalizedLine.substring(0, separatorIndex).trim()
            if (key.isEmpty() || System.getenv(key) != null || System.getProperty(key) != null) {
                return@forEachLine
            }

            val value = normalizedLine.substring(separatorIndex + 1).trim().unquote()
            System.setProperty(key, value)
        }
    }

    private fun String.unquote(): String {
        if (length < 2) {
            return this
        }

        val quotedWithDouble = startsWith('"') && endsWith('"')
        val quotedWithSingle = startsWith('\'') && endsWith('\'')
        if (!quotedWithDouble && !quotedWithSingle) {
            return this
        }

        return substring(1, length - 1)
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\\"", "\"")
            .replace("\\'", "'")
    }
}
