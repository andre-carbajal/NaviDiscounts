package net.andrecarbajal.telegramdiscountsbot

import net.andrecarbajal.telegramdiscountsbot.bot.Bot
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

@SpringBootApplication
class TelegramDiscountsBotApplication

fun main(args: Array<String>) {
	runApplication<TelegramDiscountsBotApplication>(*args)

	val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

	try {
		botsApi.registerBot(Bot())
		println("Bot is ready!")
	} catch (e: Exception) {
		e.printStackTrace()
	}
}
