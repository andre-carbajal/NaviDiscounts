package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.Request
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingInkaFarma
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingMifarma
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException


@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@Service
class Bot() : TelegramLongPollingBot() {
    @Autowired
    lateinit var requestRepository: RequestRepository

    private val token: String = System.getenv("TELEGRAM_BOT_TOKEN")

    override fun getBotUsername(): String? {
        return "TelegramDiscountsBot"
    }

    override fun getBotToken(): String? {
        return token
    }

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            val message = SendMessage()
            message.setChatId(chatId)

            var command = Commands.Companion.fromString(messageText.split(" ")[0])
            if (command != null) {
                when (command) {
                    Commands.START -> {
                        message.text =
                            "Welcome to Discounts Bot! Execute the command /request followed by the URL of the product you want to know the offer price"
                    }

                    Commands.REQUEST -> {
                        val url = messageText.removePrefix(command.command).trim()
                        if (isNotValidUrl(url)) {
                            message.text = "Invalid URL"
                            execute(message)
                            return
                        }
                        message.text = when {
                            url.contains("mifarma.com") -> scrappingMifarma(url)
                            url.contains("inkafarma.pe") -> scrappingInkaFarma(url)
                            else -> "The URL is not from Mifarma or Inkafarma"
                        }
                    }

                    Commands.ADD -> {
                        val url = messageText.removePrefix(command.command).trim()
                        if (isNotValidUrl(url)) {
                            message.text = "Invalid URL"
                            execute(message)
                            return
                        }
                        var request: Request = Request(chatId = chatId, url = url)
                        requestRepository.save(request)
                        message.text = "Request added successfully"
                    }

                    else -> {
                        message.text = "Command not found"
                    }
                }
                execute(message)
                return
            }
        }
    }

    private fun isNotValidUrl(url: String): Boolean {
        val urlRegex = "^(https?|ftp)://([a-zA-Z0-9.-]+)(:[0-9]+)?(/.*)?$"
        val urlPattern = Regex(urlRegex)

        if (!urlPattern.matches(url)) {
            return true
        }

        return try {
            val urlObj = URI(url).toURL()
            val protocol = urlObj.protocol
            val host = urlObj.host

            if (protocol == null || host == null || host.isEmpty()) {
                return true
            }

            val hostParts = host.split(".")
            hostParts.size < 2 || hostParts[0].isEmpty() || hostParts[hostParts.size - 1].isEmpty()
        } catch (_: MalformedURLException) {
            true
        } catch (_: URISyntaxException) {
            true
        }
    }
}