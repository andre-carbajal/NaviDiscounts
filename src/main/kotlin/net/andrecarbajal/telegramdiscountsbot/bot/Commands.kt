package net.andrecarbajal.telegramdiscountsbot.bot

enum class Commands(val command: String, val description: String, val needUrl: Boolean) {
    START("/start", "Get the list of commands.", false),
    REQUEST("/request", "Get the discounts from the given URL.", true),
    ADD("/add", "Add a URL to the list of requests.", true),
    DELETE("/delete", "Delete a URL from the list of requests.", true),
    STOP("/stop", "Stop to send information about products('This command will delete all your products that you added.')", false),;

    companion object {
        fun fromString(command: String): Commands? {
            return Commands.entries.find { it.command == command }
        }
    }
}