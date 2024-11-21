package net.andrecarbajal.telegramdiscountsbot.bot

enum class Commands(val command: String, val description: String, val onDevelopment: Boolean = false) {
    START("/start", "Get the list of commands."),
    ADD("/add", "Add a URL to the list of requests."),
    DELETE("/delete", "Delete a URL from the list of requests."),
    LIST("/list", "Get the list of your requests."),
    STOP("/stop", "Stop receiving product information and delete all added products."),
    EXE("/exe", "Execute the scheduler.", true);

    companion object {
        fun fromString(command: String): Commands? {
            return Commands.entries.find { it.command == command }
        }
    }
}