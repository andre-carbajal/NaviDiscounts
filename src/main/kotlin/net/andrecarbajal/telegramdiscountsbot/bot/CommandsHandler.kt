package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.SchedulerConfiguration
import net.andrecarbajal.telegramdiscountsbot.request.Request
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.util.Util
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDate

internal fun handleStartCommand(bot: Bot, message: SendMessage, schedulerConfiguration: SchedulerConfiguration) {
    val enableExeCommand = schedulerConfiguration.enabledExeCommand
    val commandList = Commands.entries.filter { !it.onDevelopment || enableExeCommand }.joinToString("\n") {
        "${it.command} - ${it.description}"
    }
    bot.sendMessage(message.chatId.toLong(), "${Util.boldString("Available commands:")}\n$commandList")
    bot.sendMessage(
        message.chatId.toLong(),
        "The bot will notify you at ${Util.boldString(schedulerConfiguration.executionTime)} (${schedulerConfiguration.timeZone})"
    )
}

internal fun handleListCommand(bot: Bot, message: SendMessage, requestRepository: RequestRepository) {
    val allRequest: List<Request> = requestRepository.findAllByChatId(message.chatId.toLong())
    if (allRequest.isEmpty()) {
        bot.sendMessage(message.chatId.toLong(), "\u26A0 You have no requests.")
    } else {
        var textList = allRequest.mapIndexed { index, request -> "${index + 1}. ${request.url}" }
            .joinToString(separator = "\n", prefix = "${Util.boldString("\uD83D\uDCDC Your request list:")}\n")
        bot.sendMessage(message.chatId.toLong(), textList)
    }
}

internal fun handleExeCommand(bot: Bot, message: SendMessage, scheduler: Scheduler) {
    bot.sendMessage(message.chatId.toLong(), "\u2699\uFE0F Scheduled execution started.")
    scheduler.sendMessagesToAllUsers()
}

internal fun handleAddCommand(
    bot: Bot, update: Update, message: SendMessage, requestRepository: RequestRepository
) {
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_URL) {
        val url = update.message.text
        if (Util.isNotValidUrl(url)) {
            bot.sendMessage(chatId, "\uD83D\uDEAB Invalid URL. Please execute the command again and enter a valid URL.")
            bot.userStates.remove(chatId)
        } else {
            val existingRequest = requestRepository.findByChatIdAndUrl(chatId = chatId, url = url)
            if (existingRequest != null) {
                bot.sendMessage(chatId, "\u26A0 Request already exists")
            } else {
                val request = Request(chatId = chatId, url = url)
                requestRepository.save(request)
                bot.sendMessage(chatId, "\uD83D\uDCE8 Request added successfully")
            }
            bot.userStates.remove(chatId)
        }
    } else {
        bot.sendMessage(chatId, "\uD83D\uDC47 Please enter the URL you want to add:")
        bot.userStates[chatId] = Bot.UserState.AWAITING_URL
    }
}

internal fun handleDeleteCommand(
    bot: Bot, update: Update, message: SendMessage, requestRepository: RequestRepository
) {
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_DELETE_INDEX) {
        val index = update.message.text.toIntOrNull()
        val allRequest: List<Request> = requestRepository.findAllByChatId(chatId)
        if (index == null || index !in 1..allRequest.size) {
            bot.sendMessage(
                chatId, "\uD83D\uDEAB Invalid index. Please execute the command again and enter a valid index."
            )
        } else {
            val request = allRequest[index - 1]
            requestRepository.delete(request)
            bot.sendMessage(chatId, "\u26A0 Request deleted successfully")
        }
        bot.userStates.remove(chatId)
    } else {
        val allRequest: List<Request> = requestRepository.findAllByChatId(chatId)
        if (allRequest.isEmpty()) {
            bot.sendMessage(chatId, "\u26A0 You have no requests.")
        } else {
            val textList = allRequest.mapIndexed { index, request -> "${index + 1}. ${request.url}" }
                .joinToString(separator = "\n", prefix = "${Util.boldString("\uD83D\uDCDC Your request list:")}\n")
            bot.sendMessage(chatId, textList)
            bot.sendMessage(chatId, "\uD83D\uDC47 Please enter the number of the request you want to delete:")
            bot.userStates[chatId] = Bot.UserState.AWAITING_DELETE_INDEX
        }
    }
}

internal fun handleStopCommand(
    bot: Bot, update: Update, message: SendMessage, requestRepository: RequestRepository
) {
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_STOP_CONFIRMATION) {
        val confirmation = update.message.text
        if (confirmation.equals("OK", ignoreCase = true)) {
            val allRequest: List<Request> = requestRepository.findAllByChatId(chatId)
            if (allRequest.isEmpty()) {
                bot.sendMessage(chatId, "\u26A0 You have no requests.")
            } else {
                allRequest.forEach {
                    requestRepository.delete(it)
                }
                bot.sendMessage(chatId, "\u26D4 All your requests were stopped (deleted).")
            }
            bot.userStates.remove(chatId)
        } else {
            bot.sendMessage(chatId, "\u274C Stop command cancelled.")
            bot.userStates.remove(chatId)
        }
    } else {
        bot.sendMessage(
            chatId,
            "\uD83D\uDEA8 Are you sure you want to stop and delete all requests?\n\uD83D\uDC47 Please type 'OK' to confirm."
        )
        bot.userStates[chatId] = Bot.UserState.AWAITING_STOP_CONFIRMATION
    }
}

internal fun handlePostponeCommand(bot: Bot,update: Update, message: SendMessage, requestRepository: RequestRepository){
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_POSTPONE_TIME) {
        val numberOfDays = update.message.text.toIntOrNull()
        if (numberOfDays == 0){
            val allRequest: List<Request> = requestRepository.findAllByChatId(chatId)
            allRequest.forEach {
                it.postponeDate = null
                requestRepository.save(it)
            }
            bot.sendMessage(chatId, "\u26D4 Your requests are no longer suspended.")
        } else if (numberOfDays == null || numberOfDays < 0) {
            bot.sendMessage(chatId, "\uD83D\uDEAB Invalid number of days. Please execute the command again and enter a valid number of days.")
        } else {
            val date: LocalDate = LocalDate.now().plusDays(numberOfDays.toLong())
            val allRequest: List<Request> = requestRepository.findAllByChatId(chatId)
            allRequest.forEach {
                it.postponeDate = date
                requestRepository.save(it)
            }
            bot.sendMessage(chatId, "\u23F3 All your requests will be postponed until $date.")
        }
        bot.userStates.remove(chatId)
    } else {
        bot.sendMessage(chatId, "\uD83D\uDC47 Please enter the number of days that messages will be postponed.")
        bot.userStates[chatId] = Bot.UserState.AWAITING_POSTPONE_TIME
    }
}