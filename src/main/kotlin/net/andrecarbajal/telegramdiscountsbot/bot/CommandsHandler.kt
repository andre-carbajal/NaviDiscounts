package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.Request
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.util.Util
import org.springframework.core.env.Environment
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import kotlin.collections.forEach

internal fun handleStartCommand(bot: Bot, message: SendMessage, environment: Environment) {
    val isDevelopment = Util.isDevelopment(environment)
    val commandList = Commands.entries.filter { !it.onDevelopment || isDevelopment }.joinToString("\n") {
        if (it.needUrl) "${it.command} <URL> - ${it.description}" else "${it.command} - ${it.description}"
    }
    bot.sendMessage(message.chatId.toLong(), "Available commands:\n$commandList")
}

internal fun handleAddCommand(
    bot: Bot,
    messageText: String,
    command: Commands,
    message: SendMessage,
    requestRepository: RequestRepository
) {
    val url = messageText.removePrefix(command.command).trim()
    if (Util.isNotValidUrl(url)) {
        bot.sendMessage(message.chatId.toLong(), "Invalid URL")
    } else {
        val existingRequest = requestRepository.findByChatIdAndUrl(chatId = message.chatId.toLong(), url = url)
        if (existingRequest != null) {
            bot.sendMessage(message.chatId.toLong(), "Request already exists")
            return
        } else {
            val request = Request(chatId = message.chatId.toLong(), url = url)
            requestRepository.save(request)
            bot.sendMessage(message.chatId.toLong(), "Request added successfully")
        }
    }
}

internal fun handleDeleteCommand(
    bot: Bot,
    messageText: String,
    command: Commands,
    message: SendMessage,
    requestRepository: RequestRepository,
) {
    val url = messageText.removePrefix(command.command).trim()
    if (Util.isNotValidUrl(url)) {
        bot.sendMessage(message.chatId.toLong(), "Invalid URL")
    } else {
        val existingRequest = requestRepository.findByChatIdAndUrl(chatId = message.chatId.toLong(), url = url)
        if (existingRequest != null) {
            requestRepository.delete(existingRequest)
            bot.sendMessage(message.chatId.toLong(), "Request deleted successfully")
        } else {
            bot.sendMessage(message.chatId.toLong(), "Request not found")
        }
    }
}

//TODO Add confirmation message
internal fun handleStopCommand(bot: Bot, message: SendMessage, requestRepository: RequestRepository) {
    val allRequest: List<Request> = requestRepository.findAllByChatId(message.chatId.toLong())
    if (allRequest.isEmpty()) {
        bot.sendMessage(message.chatId.toLong(), "You have no requests.")
    }
    allRequest.forEach {
        requestRepository.delete(it)
    }
    bot.sendMessage(message.chatId.toLong(), "All you request were stopped (deleted)")
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