package net.andrecarbajal

import io.github.cdimascio.dotenv.Dotenv
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class Bot : TelegramLongPollingBot() {
    private val dotenv = Dotenv.load()

    override fun getBotUsername(): String? {
        return "DiscountsBot"
    }

    override fun getBotToken(): String? {
        return dotenv["TELEGRAM_TOKEN"]
    }

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage() && update.message.hasText()) {
            var messageText = update.message.text
            var chatId = update.message.chatId

            var message = SendMessage()
            message.setChatId(chatId)
            message.text = "Received message: $messageText"

            execute(message)
        }
    }
}