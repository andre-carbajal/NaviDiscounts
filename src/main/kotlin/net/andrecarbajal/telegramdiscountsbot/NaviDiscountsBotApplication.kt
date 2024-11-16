package net.andrecarbajal.telegramdiscountsbot

import net.andrecarbajal.telegramdiscountsbot.bot.Bot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
class NaviDiscountsBotApplication

fun main(args: Array<String>) {
    val context: ApplicationContext = runApplication<NaviDiscountsBotApplication>(*args)

    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

    try {
        val bot = context.getBean(Bot::class.java)
        botsApi.registerBot(bot)
        println("Bot is ready!")

    } catch (e: Exception) {
        e.printStackTrace()
    }
}