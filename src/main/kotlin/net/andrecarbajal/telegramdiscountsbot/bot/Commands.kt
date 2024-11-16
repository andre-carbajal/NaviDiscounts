package net.andrecarbajal.telegramdiscountsbot.bot

enum class Commands(val command: String, val description: String, val needUrl: Boolean, val onDevelopment: Boolean = false) {
    START("/start", "Get the list of commands.", false),
    ADD("/add", "Add a URL to the list of requests.", true),
    DELETE("/delete", "Delete a URL from the list of requests.", true),
    STOP("/stop", "Stop receiving product information and delete all added products.", false),
    LIST("/list", "Get the list of your requests.", false),
    EXE("/exe", "Execute the scheduler. (only in development)", false, true);

    companion object {
        fun fromString(command: String): Commands? {
            return Commands.entries.find { it.command == command }
        }
    }
}