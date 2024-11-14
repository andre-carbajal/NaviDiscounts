package net.andrecarbajal.telegramdiscountsbot.bot

enum class Commands(val command: String) {
    START("/start"), REQUEST("/request");

    companion object {
        fun fromString(command: String): Commands? {
            return Commands.entries.find { it.command == command }
        }
    }
}