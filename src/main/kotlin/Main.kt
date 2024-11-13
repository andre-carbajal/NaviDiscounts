package net.andrecarbajal

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main() {
    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

    try {
        botsApi.registerBot(Bot())
        println("Bot is ready!")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}