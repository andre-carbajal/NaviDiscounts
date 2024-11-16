package net.andrecarbajal.telegramdiscountsbot.bot

import net.andrecarbajal.telegramdiscountsbot.request.Request
import net.andrecarbajal.telegramdiscountsbot.request.RequestRepository
import net.andrecarbajal.telegramdiscountsbot.scrapping.Websites
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingInkaFarma
import net.andrecarbajal.telegramdiscountsbot.scrapping.scrappingMifarma
import net.andrecarbajal.telegramdiscountsbot.util.isNotValidUrl
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import kotlin.collections.forEach

internal fun handleStartCommand(message: SendMessage) {
    val commandsDescription = Commands.entries.joinToString("\n") {
        if (it.needUrl) "${it.command} <URL> - ${it.description}" else "${it.command} - ${it.description}"
    }
    message.text = "Welcome to Discounts Bot! You can use the following commands:\n $commandsDescription"
}

internal fun handleRequestCommand(messageText: String, command: Commands, message: SendMessage) {
    val url = messageText.removePrefix(command.command).trim()
    if (isNotValidUrl(url)) {
        message.text = "Invalid URL"
    } else {
        message.text = when {
            url.contains(Websites.MIFARMA.url) -> scrappingMifarma(url)
            url.contains(Websites.INKA_FARMA.url) -> scrappingInkaFarma(url)
            else -> return
        }
    }
}

internal fun handleAddCommand(
    messageText: String,
    command: Commands,
    message: SendMessage,
    requestRepository: RequestRepository
) {
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

internal fun handleDeleteCommand(
    messageText: String,
    command: Commands,
    message: SendMessage,
    requestRepository: RequestRepository
) {
    val url = messageText.removePrefix(command.command).trim()
    if (isNotValidUrl(url)) {
        message.text = "Invalid URL"
    } else {
        val existingRequest = requestRepository.findByChatIdAndUrl(chatId = message.chatId.toLong(), url = url)
        if (existingRequest != null) {
            requestRepository.delete(existingRequest)
            message.text = "Request deleted successfully"
        } else {
            message.text = "Request not found"
        }
    }
}

//TODO Add confirmation message
internal fun handleStopCommand(message: SendMessage, requestRepository: RequestRepository) {
    val allRequest: List<Request> = requestRepository.findAllByChatId(message.chatId.toLong())
    if (allRequest.isEmpty()) {
        message.text = "You have no any request"
    }
    allRequest.forEach {
        requestRepository.delete(it)
    }
    message.text = "All you request were stopped (deleted)"
}

internal fun handleListCommand(message: SendMessage, requestRepository: RequestRepository) {
    val allRequest: List<Request> = requestRepository.findAllByChatId(message.chatId.toLong())
    if (allRequest.isEmpty()) {
        message.text = "You have no requests."
    } else {
        message.text = allRequest.mapIndexed { index, request -> "${index + 1}. ${request.url}" }
            .joinToString(separator = "\n", prefix = "Your request list:\n")
    }
}