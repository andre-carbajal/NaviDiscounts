package net.andrecarbajal.telegramdiscountsbot

import net.andrecarbajal.telegramdiscountsbot.bot.Bot
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication

@SpringBootApplication
@EnableConfigurationProperties(
    ApplicationConfiguration::class,
    TelegramBotConfiguration::class,
    SchedulerConfiguration::class
)
class NaviDiscountsBotApplication

fun main(args: Array<String>) {
    val logger: Logger = LoggerFactory.getLogger(NaviDiscountsBotApplication::class.java)
    val context: ApplicationContext = runApplication<NaviDiscountsBotApplication>(*args)

    val botsApplication = TelegramBotsLongPollingApplication()

    try {
        val bot = context.getBean(Bot::class.java)
        botsApplication.registerBot(bot.botToken, bot)
        logger.info("Bot ${bot.botUsername} is running!")

    } catch (e: Exception) {
        logger.error("Error while starting the bot", e)
    }
}

@RestController
class BotController{
    @GetMapping("/health")
    fun getHealth(): ResponseEntity<String> {
        return ResponseEntity.ok("Bot is running!")
    }
}