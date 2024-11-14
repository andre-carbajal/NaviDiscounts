package net.andrecarbajal.telegramdiscountsbot.bot

enum class Commands(val command: String, val description: String, val needUrl: Boolean) {
    START("/start", "Get the list of commands", false),
    REQUEST("/request", "Get the discounts from the given URL", true),
    ADD("/add", "Add a URL to the list of requests", true),
    DELETE("/delete", "Delete a URL from the list of requests", true);

    companion object {
        fun fromString(command: String): Commands? {
            return Commands.entries.find { it.command == command }
        }
    }
}