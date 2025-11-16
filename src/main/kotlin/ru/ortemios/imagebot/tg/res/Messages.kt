package ru.ortemios.imagebot.tg.res

class Messages {
    companion object {
        const val ACCESS_DENIED = "Доступ запрещён."
        const val INCORRECT_SYNTAX = "Некорректный синтаксис."

        const val GROUP_SET = "Группа установлена."
        const val GROUP_NOT_SET_ERROR = "Группа для публикации не установлена. " +
                "Пригласите бота в нужную группу и используйте команду `${Commands.setGroup} <group_id>`."

        fun downloadUrlNotFound(url: String): String {
            return "Изображение по адресу $url не найдено."
        }

        fun incorrectUrl(url: String): String {
            return "Некорректный адрес: $url"
        }

        const val DOWNLOAD_STARTING = "Начинаю загрузку..."
        const val DOWNLOAD_UPLOADING_SD = "Загружаю изображения в SD..."
        const val DOWNLOAD_UPLOADING_HD = "Загружаю изображения в HD..."
        const val DOWNLOAD_COMPLETE = "Загрузка завершена."
        const val DOWNLOAD_FAILED = "Загрузка не удалась."

        fun downloadingImage(current: Int, total: Int): String {
            return "Загружаю изображение $current/$total..."
        }
    }
}