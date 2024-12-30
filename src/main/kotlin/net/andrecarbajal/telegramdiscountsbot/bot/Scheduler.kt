package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.SchedulerConfiguration
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.scrapping.Websites
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingInkaFarma
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingMifarma
import net.andrecarbajal.telegramdiscountsbot.util.Util
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("UNUSED")
@Component
class Scheduler @Autowired constructor(
    @Lazy private var bot: Bot,
    private val schedulerConfiguration: SchedulerConfiguration,
    private val requestRepository: RequestRepository
) {
    private val logger: Logger = LoggerFactory.getLogger(Scheduler::class.java)
    private val scheduler = Executors.newScheduledThreadPool(1)

    private val dataTimePattern = "yyyy-MM-dd HH:mm:ss z"

    init {
        scheduleDailyMessage()
    }

    private fun scheduleDailyMessage() {
        val now = ZonedDateTime.now(ZoneId.of(schedulerConfiguration.timeZone))
        val (hour, minute) = parseTime(schedulerConfiguration.executionTime)
        var nextRun = now.withHour(hour).withMinute(minute).withSecond(0)

        if (now > nextRun) nextRun = nextRun.plusDays(1)

        val formatter = DateTimeFormatter.ofPattern(dataTimePattern)
        logger.info("Scheduler will run at: ${nextRun.format(formatter)}")

        val duration = Duration.between(now, nextRun)
        val initialDelay = duration.toSeconds()

        scheduler.scheduleAtFixedRate(
            Runnable {
                run {
                    try {
                        logger.info("Send messages to all users task is running")
                        sendMessagesToAllUsers()
                    } catch (e: Exception) {
                        logger.error("Error occurred while sending messages", e)
                    }
                }
            }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS
        )
    }

    fun sendMessagesToAllUsers() {
        val now = LocalDate.now()
        val requests = requestRepository.findAll().filter { it.postponeDate == null || it.postponeDate!!.isBefore(now) || it.postponeDate!!.isEqual(now) }

        requests.forEach { request ->
            if (request.postponeDate != null && (request.postponeDate!!.isBefore(now) || request.postponeDate!!.isEqual(now))) {
                request.postponeDate = null
                requestRepository.save(request)
            }

            val url = request.url
            val chatId = request.chatId
            val data = when {
                url.contains(Websites.MIFARMA.url) -> scrappingMifarma(url)
                url.contains(Websites.INKA_FARMA.url) -> scrappingInkaFarma(url)
                else -> null
            }
            if (data != null) {
                if (data[3] != null) {
                    if (data[4] != null) {
                        bot.sendPhotoMessage(chatId, parseMessageText(data, url), data[4]!!)
                    } else {
                        bot.sendMessage(chatId, parseMessageText(data, url))
                    }
                }
            } else {
                logger.warn("Scraping result is null")
            }
        }
    }

    private fun parseTime(time: String): Pair<Int, Int> {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()
        return Pair(hour, minute)
    }

    private fun parseMessageText(data: List<String?>, url: String): String {
        val pharmacy = data[0]
        val name = data[1]
        val price = data[2]
        val offer = data[3]

        return """
            |Pharmacy: $pharmacy
            |Product Name: $name
            |Price: $price
            |${Util.boldString("Offer: $offer")}
            |URL: $url
        """.trimMargin()
    }
}