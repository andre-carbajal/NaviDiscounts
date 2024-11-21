package net.andrecarbajal.telegramdiscountsbot

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.application")
class ApplicationConfiguration {
    var name: String = ""
}

@Configuration
@ConfigurationProperties(prefix = "spring.bot")
class TelegramBotConfiguration {
    var token: String = ""
}

@Configuration
@ConfigurationProperties(prefix = "spring.bot.scheduler")
class SchedulerConfiguration {
    var enabledExeCommand: Boolean = false
    var timeZone: String = ""
    var executionTime: String = ""
}