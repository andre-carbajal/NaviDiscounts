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
        if (update?.hasMessage() == true && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            val message = SendMessage().apply { setChatId(chatId) }

            val command = Commands.fromString(messageText.split(" ")[0])
            if (command != null) {
                handleCommand(command, messageText, message)
            } else {
                message.text = "Command not found"
                execute(message)
            }
        }
    }

    private fun handleCommand(command: Commands, messageText: String, message: SendMessage) {
        when (command) {
            Commands.START -> handleStartCommand(message)
            Commands.REQUEST -> handleRequestCommand(messageText, command, message)
            Commands.ADD -> handleAddCommand(messageText, command, message)
        }
        execute(message)
    }

    private fun handleStartCommand(message: SendMessage) {
        message.text =
            "Welcome to Discounts Bot! You can use the following commands:\n" + "/request <url> - Get the discounts from the given URL\n" + "/add <url> - Add a URL to the list of requests"
    }

    private fun handleRequestCommand(messageText: String, command: Commands, message: SendMessage) {
        val url = messageText.removePrefix(command.command).trim()
        if (isNotValidUrl(url)) {
            message.text = "Invalid URL"
        } else {
            message.text = when {
                url.contains("mifarma.com") -> scrappingMifarma(url)
                url.contains("inkafarma.pe") -> scrappingInkaFarma(url)
                else -> "The URL is not from Mifarma or Inkafarma"
            }
        }
    }

    private fun handleAddCommand(messageText: String, command: Commands, message: SendMessage) {
        val url = messageText.removePrefix(command.command).trim()
        if (isNotValidUrl(url)) {
            message.text = "Invalid URL"
        } else {
            val existingRequest = requestRepository.findByChatIdAndUrl(chatId = message.chatId.toLong(), url = url)
            if (existingRequest != null) {
                message.text = "Request already exists"
                return
            } else {
                val request = Request(chatId = message.chatId.toLong(), url = url)
                requestRepository.save(request)
                message.text = "Request added successfully"
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
            if (hostParts.size < 2 || hostParts[0].isEmpty() || hostParts[hostParts.size - 1].isEmpty()) {
                url.contains("mifarma.com.pe") || url.contains("inkafarma.pe")
            } else {
                false
            }

        } catch (_: MalformedURLException) {
            true
        } catch (_: URISyntaxException) {
            true
        }
    }
}