package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.scrapping.Websites
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingInkaFarma
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingMifarma
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("UNUSED")
@Component
class Scheduler @Autowired constructor(@Lazy private var bot: Bot, private val requestRepository: RequestRepository) {
    private val logger: Logger = LoggerFactory.getLogger(Scheduler::class.java)
    private val scheduler = Executors.newScheduledThreadPool(1)

    init {
        scheduleDailyMessage()
    }

    private fun scheduleDailyMessage() {
        val now = ZonedDateTime.now(ZoneId.of("-05:00"))
        var nextRun = now.withHour(3).withMinute(0)

        if (now > nextRun) nextRun = nextRun.plusDays(1)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")
        logger.info("Scheduler will run at: ${nextRun.format(formatter)}")

        val duration = Duration.between(now, nextRun)
        val initialDelay = duration.toSeconds()

        scheduler.scheduleAtFixedRate(
            Runnable {
                run {
                    logger.info("The send messages to all users task is running")
                    sendMessagesToAllUsers()
                }
            }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS
        )
    }

    fun sendMessagesToAllUsers() {
        val requests = requestRepository.findAll()
        requests.forEach { request ->
            val url = request.url
            val chatId = request.chatId
            val messageText = when {
                url.contains(Websites.MIFARMA.url) -> scrappingMifarma(url)
                url.contains(Websites.INKA_FARMA.url) -> scrappingInkaFarma(url)
                else -> null
            }
            if (messageText != null) {
                bot.sendMessage(chatId, messageText)
            } else {
                logger.warn("Scraping result is null")
            }
        }
    }
}
