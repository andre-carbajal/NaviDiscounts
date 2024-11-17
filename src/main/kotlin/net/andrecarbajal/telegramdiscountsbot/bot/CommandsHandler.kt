package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.Request
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.util.Util
import org.springframework.core.env.Environment
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

internal fun handleStartCommand(bot: Bot, message: SendMessage, environment: Environment) {
    val isDevelopment = Util.isDevelopment(environment)
    val commandList = Commands.entries.filter { !it.onDevelopment || isDevelopment }.joinToString("\n") {
        if (it.needUrl) "${it.command} <URL> - ${it.description}" else "${it.command} - ${it.description}"
    }
    bot.sendMessage(message.chatId.toLong(), "Available commands:\n$commandList")
}

internal fun handleListCommand(bot: Bot, message: SendMessage, requestRepository: RequestRepository) {
    val allRequest: List<Request> = requestRepository.findAllByChatId(message.chatId.toLong())
    if (allRequest.isEmpty()) {
        bot.sendMessage(message.chatId.toLong(), "You have no requests.")
    } else {
        var textList = allRequest.mapIndexed { index, request -> "${index + 1}. ${request.url}" }
            .joinToString(separator = "\n", prefix = "Your request list:\n")
        bot.sendMessage(message.chatId.toLong(), textList)
    }
}

internal fun handleExeCommand(bot: Bot, message: SendMessage, scheduler: Scheduler) {
    bot.sendMessage(message.chatId.toLong(), "Scheduled messages sent")
    scheduler.sendMessagesToAllUsers()
}

internal fun handleAddCommand(
    bot: Bot,
    update: Update,
    message: SendMessage,
    requestRepository: RequestRepository
) {
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_URL) {
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
            bot.userStates.remove(chatId)
        }
    } else {
        bot.sendMessage(chatId, "Please enter the URL you want to add:")
        bot.userStates[chatId] = Bot.UserState.AWAITING_URL
    }
}

internal fun handleDeleteCommand(
    bot: Bot,
    update: Update,
    message: SendMessage,
    requestRepository: RequestRepository
) {
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_DELETE_URL) {
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
            bot.userStates.remove(chatId)
        }
    } else {
        bot.sendMessage(chatId, "Please enter the URL you want to delete:")
        bot.userStates[chatId] = Bot.UserState.AWAITING_DELETE_URL
    }
}

internal fun handleStopCommand(
    bot: Bot,
    update: Update,
    message: SendMessage,
    requestRepository: RequestRepository
) {
    val chatId = message.chatId.toLong()
    val userState = bot.userStates[chatId]

    if (userState == Bot.UserState.AWAITING_STOP_CONFIRMATION) {
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
            bot.userStates.remove(chatId)
        } else {
            bot.sendMessage(chatId, "Stop command cancelled.")
            bot.userStates.remove(chatId)
        }
    } else {
        bot.sendMessage(
            chatId,
            "Are you sure you want to stop and delete all requests? Please type 'OK' to confirm."
        )
        bot.userStates[chatId] = Bot.UserState.AWAITING_STOP_CONFIRMATION
    }
}