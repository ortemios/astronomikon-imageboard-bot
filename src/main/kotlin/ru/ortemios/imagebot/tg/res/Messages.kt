package ru.ortemios.imagebot.tg.res

class Messages {
    companion object {
        const val ACCESS_DENIED = "Доступ запрещён."
        const val SERVICE_IS_BUSY = "Сервис загружен, приходите позже."
        const val GENERAL_ERROR = ""

        const val GROUP_SET = "Группа установлена."
        const val GROUP_NOT_SET_ERROR = "Группа для публикации не установлена. " +
                "Пригласите бота в нужную группу и используйте команду `${Commands.setGroup} <group_id>`."

        fun downloadUrlNotFound(url: String): String {
            return "Изображение по адресу $url не найдено."
        }

        fun unsupportedImageFormat(url: String): String {
            return "Формат изображения по ссылке $url не поддерживается."
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