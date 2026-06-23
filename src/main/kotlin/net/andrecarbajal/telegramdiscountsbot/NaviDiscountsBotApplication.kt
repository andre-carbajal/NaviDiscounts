package net.andrecarbajal.telegramdiscountsbot

import net.andrecarbajal.telegramdiscountsbot.bot.Bot
import net.andrecarbajal.telegramdiscountsbot.util.DotEnvLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import kotlin.system.exitProcess

@SpringBootApplication
@EnableConfigurationProperties(
    ApplicationConfiguration::class,
    TelegramBotConfiguration::class,
    SchedulerConfiguration::class
)
class NaviDiscountsBotApplication

fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger(NaviDiscountsBotApplication::class.java)
    DotEnvLoader.loadSystemProperties()
    val context = runApplication<NaviDiscountsBotApplication>(*args)

    val botsApplication = TelegramBotsLongPollingApplication()

    try {
        val bot = context.getBean<Bot>()
        require(isValidTelegramBotToken(bot.botToken)) {
            "TELEGRAM_BOT_TOKEN is missing or invalid. Set a real Telegram BotFather token in .env or the container environment."
        }
        botsApplication.registerBot(bot.botToken, bot)
        logger.info("Bot ${bot.botUsername} is running!")

    } catch (e: Exception) {
        logger.error("Fatal error while starting the Telegram bot. Stopping application.", e)
        context.close()
        exitProcess(1)
    }
}

private fun isValidTelegramBotToken(token: String): Boolean =
    token.isNotBlank() &&
            token != "your_token" &&
            Regex("""\d+:[A-Za-z0-9_-]+""").matches(token)

@RestController
class BotController{
    @GetMapping("/health")
    fun getHealth(): ResponseEntity<String> {
        return ResponseEntity.ok("Bot is running!")
    }
}
