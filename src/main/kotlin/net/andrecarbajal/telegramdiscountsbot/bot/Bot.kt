package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.ApplicationConfiguration
import net.andrecarbajal.telegramdiscountsbot.SchedulerConfiguration
import net.andrecarbajal.telegramdiscountsbot.TelegramBotConfiguration
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
@Service
class Bot @Autowired constructor(
    private val requestRepository: RequestRepository,
    private val scheduler: Scheduler,
    private val applicationConfiguration: ApplicationConfiguration,
    private val telegramBotConfiguration: TelegramBotConfiguration,
    private val schedulerConfiguration: SchedulerConfiguration
) : TelegramLongPollingBot() {
    override fun getBotUsername(): String? {
        return applicationConfiguration.name
    }

    override fun getBotToken(): String? {
        return telegramBotConfiguration.token
    }

    val userStates = mutableMapOf<Long, UserState>()

    enum class UserState {
        AWAITING_URL, AWAITING_DELETE_INDEX, AWAITING_STOP_CONFIRMATION, NONE
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
            Commands.START -> handleStartCommand(this, message, schedulerConfiguration)
            Commands.ADD -> handleAddCommand(this, update, message, requestRepository)
            Commands.DELETE -> handleDeleteCommand(this, update, message, requestRepository)
            Commands.STOP -> handleStopCommand(this, update, message, requestRepository)
            Commands.LIST -> handleListCommand(this, message, requestRepository)
            Commands.EXE -> if (schedulerConfiguration.enabledExeCommand) handleExeCommand(
                this, message, scheduler
            )
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