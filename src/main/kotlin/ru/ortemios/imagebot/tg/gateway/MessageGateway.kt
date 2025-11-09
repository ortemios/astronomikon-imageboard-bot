package ru.ortemios.imagebot.tg.gateway

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.message.sendMessage

class MessageGateway(private val bot: TelegramBot) {

    suspend fun sendTextMessage(chatId: String, text: String) {
        sendMessage(text = text).send(chatId, bot)
    }
}