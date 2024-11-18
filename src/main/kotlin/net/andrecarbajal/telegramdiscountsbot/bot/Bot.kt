package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.util.Util
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@Service
class Bot @Autowired constructor(
    private val environment: Environment,
    private val requestRepository: RequestRepository,
    private val scheduler: Scheduler
) : TelegramLongPollingBot() {

    @Value("\${spring.telegram.bot.token}")
    private lateinit var token: String

    private fun getToken(): String {
        return if (token.isEmpty()) System.getenv("TELEGRAM_BOT_TOKEN") else token
    }

    @Value("\${spring.application.name}")
    private lateinit var name: String

    override fun getBotUsername(): String? {
        return name
    }

    override fun getBotToken(): String? {
        return getToken()
    }

    val userStates = mutableMapOf<Long, UserState>()

    enum class UserState {
        AWAITING_URL,
        AWAITING_DELETE_INDEX,
        AWAITING_STOP_CONFIRMATION,
        NONE
    }

    override fun onUpdateReceived(update: Update?) {
        if (update?.hasMessage() == true && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            val message = SendMessage().apply { setChatId(chatId) }

            val userState = userStates[chatId] ?: UserState.NONE

            when (userState) {
                UserState.AWAITING_URL -> handleAddCommand(this, update, message, requestRepository)
                UserState.AWAITING_DELETE_INDEX -> handleDeleteCommand(this, update, message, requestRepository)
                UserState.AWAITING_STOP_CONFIRMATION -> handleStopCommand(this, update, message, requestRepository)
                else -> {
                    val command = Commands.fromString(messageText)
                    if (command != null) {
                        handleCommand(update, command, message)
                    }
                }
            }
        }
    }

    private fun handleCommand(update: Update, command: Commands, message: SendMessage) {
        when (command) {
            Commands.START -> handleStartCommand(this, message, environment)
            Commands.ADD -> handleAddCommand(this, update, message, requestRepository)
            Commands.DELETE -> handleDeleteCommand(this, update, message, requestRepository)
            Commands.STOP -> handleStopCommand(this, update, message, requestRepository)
            Commands.LIST -> handleListCommand(this, message, requestRepository)
            Commands.EXE -> if (Util.isDevelopment(environment)) handleExeCommand(
                this, message, scheduler
            ) else sendMessage(message.chatId.toLong(), "Command not found")
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage().apply {
            setChatId(chatId)
            setText(text)
            enableMarkdown(true)
        }
        execute(message)
    }
}