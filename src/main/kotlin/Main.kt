package net.andrecarbajal

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main() {
    Scrapping.scrap("https://www.mifarma.com.pe/producto/pack-bioderma-limpieza-y-proteccion/PACKDB51")


    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

    try {
        botsApi.registerBot(Bot())
        println("Bot is ready!")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}