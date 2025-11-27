package ru.ortemios.imagebot.tg.res

import ru.ortemios.imagebot.domain.entity.User

typealias CommandHandler = suspend (user: User, args: List<String>) -> Unit

class CommandMatcher {

    private val commands = mutableMapOf<String, Pair<Int, CommandHandler>>()

    fun registerCommand(name: String, argsCount: Int, handler: CommandHandler) {
        commands[name] = Pair(argsCount, handler)
    }

    suspend fun handle(user: User, message: String): Boolean {
        for (command in commands) {
            if (message.lowercase().startsWith(command.key)) {
                val args = message.trim()
                    .split(" ")
                    .filter { it.isNotBlank() }
                    .drop(1)
                if (args.size != command.value.first) {
                    throw IncorrectSyntaxException(message)
                }
                command.value.second(user, args)
                return true
            }
        }
        return false
    }



    class IncorrectSyntaxException(command: String) : RuntimeException("Incorrect command syntax: $command")
}