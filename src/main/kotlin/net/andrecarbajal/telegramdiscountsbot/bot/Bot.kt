package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.ApplicationConfiguration
import net.andrecarbajal.telegramdiscountsbot.SchedulerConfiguration
import net.andrecarbajal.telegramdiscountsbot.TelegramBotConfiguration
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.generics.TelegramClient

@Service
class Bot @Autowired constructor(
    private val requestRepository: RequestRepository,
    private val scheduler: Scheduler,
    private val applicationConfiguration: ApplicationConfiguration,
    private val telegramBotConfiguration: TelegramBotConfiguration,
    private val schedulerConfiguration: SchedulerConfiguration
) : LongPollingSingleThreadUpdateConsumer {
    private val telegramClient: TelegramClient = OkHttpTelegramClient(botToken)

    val botUsername: String
        get() = applicationConfiguration.name

    val botToken: String
        get() = telegramBotConfiguration.token

    val userStates = mutableMapOf<Long, UserState>()

    enum class UserState {
        AWAITING_URL, AWAITING_DELETE_INDEX, AWAITING_STOP_CONFIRMATION, AWAITING_POSTPONE_TIME, NONE
    }

    override fun consume(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            val message = SendMessage(chatId.toString(), "")

            val userState = userStates[chatId] ?: UserState.NONE

            when (userState) {
                UserState.AWAITING_URL -> handleAddCommand(this, update, message, requestRepository)
                UserState.AWAITING_DELETE_INDEX -> handleDeleteCommand(this, update, message, requestRepository)
                UserState.AWAITING_STOP_CONFIRMATION -> handleStopCommand(this, update, message, requestRepository)
                UserState.AWAITING_POSTPONE_TIME -> handlePostponeCommand(this, update, message, requestRepository)
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
            Commands.POSTPONE -> handlePostponeCommand(this, update, message, requestRepository)
            Commands.EXE -> if (schedulerConfiguration.enabledExeCommand) handleExeCommand(
                this, message, scheduler
            )
        }
    }

    fun sendMessage(chatId: Long, text: String) {
        val message = SendMessage(chatId.toString(), text).apply {
            enableMarkdown(true)
        }
        telegramClient.execute(message)
    }

    fun sendPhotoMessage(chatId: Long, caption: String, photo_url: String) {
        val inputFile = InputFile(photo_url)
        val sendPhoto = SendPhoto(chatId.toString(), inputFile).apply {
            setCaption(caption)
            parseMode = "Markdown"
        }
        telegramClient.execute(sendPhoto)
    }
}
