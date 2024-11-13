package net.andrecarbajal

import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

fun main() {
    println("Mifarma's products")
    scrappingMifarma("https://www.mifarma.com.pe/producto/pack-bioderma-limpieza-y-proteccion/PACKDB51")
    scrappingMifarma("https://www.mifarma.com.pe/producto/balsamo-la-roche-posay-cicaplast-baume-b5/067386")

    println("Inkafarma's products")
    scrappingInkaFarma("https://inkafarma.pe/producto/gel-hidratante-facial-hydro-boost-neutrogena/011592")
    scrappingInkaFarma("https://inkafarma.pe/producto/gel-limpiador-espumoso-236ml-cerave/023736")

    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

    try {
        botsApi.registerBot(Bot())
        println("Bot is ready!")
    } catch (e: Exception) {
        e.printStackTrace()
    }
}