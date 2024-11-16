package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@Service
class Bot @Autowired constructor(private val requestRepository: RequestRepository) : TelegramLongPollingBot() {
    private val token: String = System.getenv("TELEGRAM_BOT_TOKEN")

    @Value("\${spring.application.name}")
    lateinit var name: String

    override fun getBotUsername(): String? {
        return name
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
            Commands.ADD -> handleAddCommand(messageText, command, message, requestRepository)
            Commands.DELETE -> handleDeleteCommand(messageText, command, message, requestRepository)
            Commands.STOP -> handleStopCommand(message, requestRepository)
            Commands.LIST -> handleListCommand(message, requestRepository)
        }
        execute(message)
    }

    fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage().apply {
            setChatId(chatId)
            setText(text)
        }
        execute(message)
    }
}