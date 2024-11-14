package net.andrecarbajal.telegramdiscountsbot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
class Bot : TelegramLongPollingBot() {
    private val token: String = System.getenv("TELEGRAM_BOT_TOKEN")

    override fun getBotUsername(): String? {
        return "TelegramDiscountsBot"
    }

    override fun getBotToken(): String? {
        return token
    }

    override fun onUpdateReceived(update: Update?) {
        if (update!!.hasMessage() && update.message.hasText()) {
            val messageText = update.message.text
            val chatId = update.message.chatId

            val message = SendMessage()
            message.setChatId(chatId)

            var command = Commands.Companion.fromString(messageText.split(" ")[0])
            if (command != null) {
                when (command) {
                    Commands.START -> {
                        message.text =
                            "Welcome to Discounts Bot! Execute the command /request followed by the URL of the product you want to know the offer price"
                    }

                    Commands.REQUEST -> {
                        val url = messageText.removePrefix(command.command).trim()
                        message.text = when {
                            url.contains("mifarma.com") -> scrappingMifarma(url)
                            url.contains("inkafarma.pe") -> scrappingInkaFarma(url)
                            else -> "The URL is not from Mifarma or Inkafarma"
                        }
                    }

                    else -> {
                        message.text = "Command not found"
                    }
                }
                execute(message)
                return
            }
        }
    }
}