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
            val messageText = update.message.text
            val chatId = update.message.chatId

            val message = SendMessage()
            message.setChatId(chatId)
            if (messageText.contains("mifarma.com")) {
                message.text = scrappingMifarma(messageText)
            } else if (messageText.contains("inkafarma.pe")) {
                message.text = scrappingInkaFarma(messageText)
            } else {
                message.text = "The URL is not from Mifarma or Inkafarma"
            }

            execute(message)
        }
    }
}