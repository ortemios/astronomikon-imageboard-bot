package ru.ortemios.imagebot.tg.res

class Messages {
    companion object {
        const val accessDeniedError = "Доступ запрещён."
        const val incorrectSyntaxError = "Некорректный синтаксис."

        const val groupSetSuccess = "Группа установлена."
        const val groupNotSetWarning = "Группа для публикации не установлена. " +
                "Пригласите бота в нужную группу и используйте команду `${Commands.setGroup} <group_id>`."
    }
}