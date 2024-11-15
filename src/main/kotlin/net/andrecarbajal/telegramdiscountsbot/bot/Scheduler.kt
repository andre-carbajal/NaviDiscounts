package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingInkaFarma
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingMifarma
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Component
class Scheduler @Autowired constructor(private val bot: Bot, private val requestRepository: RequestRepository) {

    private val scheduler = Executors.newScheduledThreadPool(1)
    private var isScheduled = false

    init {
        scheduleDailyMessage()
    }

    private fun scheduleDailyMessage() {
        if (isScheduled) return

        val now = LocalTime.now()
        val targetTime = LocalTime.of(19, 15)
        val initialDelay = if (now.isBefore(targetTime)) {
            Duration.between(now, targetTime).seconds
        } else {
            Duration.between(now, targetTime.plusHours(24)).seconds
        }

        scheduler.scheduleAtFixedRate({
            sendMessagesToAllUsers()
        }, initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS)

        isScheduled = true
    }

    private fun sendMessagesToAllUsers() {
        val requests = requestRepository.findAll()
        requests.forEach { request ->
            val url = request.url
            val chatId = request.chatId
            val messageText = when {
                url.contains("mifarma.com.pe") -> scrappingMifarma(url)
                url.contains("inkafarma.pe") -> scrappingInkaFarma(url)
                else -> return@forEach
            }
            bot.sendMessage(chatId, messageText)
        }
    }

}
