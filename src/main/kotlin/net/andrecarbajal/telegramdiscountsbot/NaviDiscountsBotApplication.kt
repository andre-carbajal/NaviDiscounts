package net.andrecarbajal.telegramdiscountsbot

import net.andrecarbajal.telegramdiscountsbot.bot.Bot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
class NaviDiscountsBotApplication

fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger(NaviDiscountsBotApplication::class.java)
    val context: ApplicationContext = runApplication<NaviDiscountsBotApplication>(*args)

    val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

    try {
        val bot = context.getBean(Bot::class.java)
        botsApi.registerBot(bot)
        logger.info("Bot is running!")

    } catch (e: Exception) {
        logger.error("Error while starting the bot", e)
    }
}