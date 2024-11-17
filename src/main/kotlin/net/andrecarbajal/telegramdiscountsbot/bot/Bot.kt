package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.Request
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
    private val token: String = System.getenv("TELEGRAM_BOT_TOKEN")

    @Value("\${spring.application.name}")
    private lateinit var name: String

    override fun getBotUsername(): String? {
        return name
    }

    override fun getBotToken(): String? {
        return token
    }

    val userStates = mutableMapOf<Long, UserState>()

    enum class UserState {
        AWAITING_URL,
        AWAITING_DELETE_URL,
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
                UserState.AWAITING_DELETE_URL -> handleDeleteCommand(this, update, message, requestRepository)
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

    internal fun handleAddCommand(
        bot: Bot,
        update: Update,
        message: SendMessage,
        requestRepository: RequestRepository
    ) {
        val chatId = message.chatId.toLong()
        val userState = userStates[chatId]

        if (userState == UserState.AWAITING_URL) {
            val url = update.message.text
            if (Util.isNotValidUrl(url)) {
                bot.sendMessage(chatId, "Invalid URL. Please enter a valid URL.")
            } else {
                val existingRequest = requestRepository.findByChatIdAndUrl(chatId = chatId, url = url)
                if (existingRequest != null) {
                    bot.sendMessage(chatId, "Request already exists")
                } else {
                    val request = Request(chatId = chatId, url = url)
                    requestRepository.save(request)
                    bot.sendMessage(chatId, "Request added successfully")
                }
                userStates.remove(chatId)
            }
        } else {
            bot.sendMessage(chatId, "Please enter the URL you want to add:")
            userStates[chatId] = UserState.AWAITING_URL
        }
    }

    internal fun handleDeleteCommand(
        bot: Bot,
        update: Update,
        message: SendMessage,
        requestRepository: RequestRepository
    ) {
        val chatId = message.chatId.toLong()
        val userState = userStates[chatId]

        if (userState == UserState.AWAITING_DELETE_URL) {
            val url = update.message.text
            if (Util.isNotValidUrl(url)) {
                bot.sendMessage(chatId, "Invalid URL. Please enter a valid URL.")
            } else {
                val existingRequest = requestRepository.findByChatIdAndUrl(chatId = chatId, url = url)
                if (existingRequest != null) {
                    requestRepository.delete(existingRequest)
                    bot.sendMessage(chatId, "Request deleted successfully")
                } else {
                    bot.sendMessage(chatId, "Request not found")
                }
                userStates.remove(chatId)
            }
        } else {
            bot.sendMessage(chatId, "Please enter the URL you want to delete:")
            userStates[chatId] = UserState.AWAITING_DELETE_URL
        }
    }

    internal fun handleStopCommand(
        bot: Bot,
        update: Update,
        message: SendMessage,
        requestRepository: RequestRepository
    ) {
        val chatId = message.chatId.toLong()
        val userState = userStates[chatId]

        if (userState == UserState.AWAITING_STOP_CONFIRMATION) {
            val confirmation = update.message.text
            if (confirmation.equals("OK", ignoreCase = true)) {
                val allRequest: List<Request> = requestRepository.findAllByChatId(chatId)
                if (allRequest.isEmpty()) {
                    bot.sendMessage(chatId, "You have no requests.")
                } else {
                    allRequest.forEach {
                        requestRepository.delete(it)
                    }
                    bot.sendMessage(chatId, "All your requests were stopped (deleted).")
                }
                userStates.remove(chatId)
            } else {
                bot.sendMessage(chatId, "Stop command cancelled.")
                userStates.remove(chatId)
            }
        } else {
            bot.sendMessage(
                chatId,
                "Are you sure you want to stop and delete all requests? Please type 'OK' to confirm."
            )
            userStates[chatId] = UserState.AWAITING_STOP_CONFIRMATION
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage().apply {
            setChatId(chatId)
            setText(text)
        }
        execute(message)
    }
}