package ru.ortemios.imagebot.tg.res

class Commands {
    companion object {
        const val setGroup = "/setgroup"

        fun matchCommand(command: String, text: String): List<String>? {
            if (!text.lowercase().startsWith(command)) {
                return null
            }
            return text.trim().split(" ").filter { it.isNotBlank() }.drop(1)
        }
    }
}