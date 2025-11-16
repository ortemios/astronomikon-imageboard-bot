package ru.ortemios.imagebot.tg.gateway

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.api.media.sendDocument
import eu.vendeli.tgbot.api.media.sendMediaGroup
import eu.vendeli.tgbot.api.message.editMessageText
import eu.vendeli.tgbot.api.message.editText
import eu.vendeli.tgbot.api.message.sendMessage
import eu.vendeli.tgbot.types.component.ImplicitFile
import eu.vendeli.tgbot.types.component.InputFile
import eu.vendeli.tgbot.types.component.getOrNull
import eu.vendeli.tgbot.types.media.InputMedia

class MessageGateway(private val bot: TelegramBot) {

    suspend fun sendTextMessage(chatId: String, text: String): Long {
        return sendMessage(text = text).sendReturning(chatId, bot)
            .await()
            .getOrNull()
            ?.messageId ?: throw Exception("Error while sending message to $chatId: $text")
    }

    suspend fun updateTextMessage(chatId: String, messageId: Long, text: String) {
        editText(messageId, { text }).send(chatId, bot)
    }

    suspend fun sendImagesGroup(chatId: String, images: List<ByteArray>) {
        sendMediaGroup(images.map {
            InputMedia.Photo(
                media = ImplicitFile.InpFile(
                    file = InputFile(
                        data = it
                    )
                )
            )
        }).send(chatId, bot)
    }

    suspend fun sendImageAsDoc(chatId: String, title: String, content: ByteArray): String {
        return sendDocument(
            ImplicitFile.InpFile(
                file = InputFile(
                    data = content,
                    fileName = title
                )
            )
        ).sendReturning(chatId, bot).getOrNull()?.document?.fileId
            ?: throw Exception("Error while sending document to $chatId: $title: $content")
    }

    suspend fun sendDocsGroup(chatId: String, docIds: List<String>) {
        sendMediaGroup(docIds.map {
            InputMedia.Document(
                media = ImplicitFile.Str(it)
            )
        }).send(chatId, bot)
    }

    suspend fun deleteMessage(chatId: String, messageId: Long) {
        eu.vendeli.tgbot.api.message.deleteMessage(messageId).send(chatId, bot)
    }
}