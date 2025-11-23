package ru.ortemios.imagebot.tg.gateway

import org.telegram.telegrambots.meta.api.methods.send.SendDocument
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto
import org.telegram.telegrambots.meta.generics.TelegramClient
import java.io.ByteArrayInputStream


class MessageGateway(private val client: TelegramClient) {

    fun sendTextMessage(chatId: String, text: String): Long {
        val result = client.execute(SendMessage(chatId, text))
        return result.messageId.toLong()
    }

    fun updateTextMessage(chatId: String, messageId: Long, text: String) {
        val method = EditMessageText(text)
        method.chatId = chatId
        method.messageId = messageId.toInt()
        client.execute(method)
    }

    fun sendImagesGroup(chatId: String, images: List<ByteArray>) {
        val filename = "file"
        if (images.size == 1) {
            client.execute(SendPhoto(chatId, InputFile(
                ByteArrayInputStream(images.first()), filename)
            ))
        } else {
            client.execute(SendMediaGroup(chatId, images.map {
                InputMediaPhoto(ByteArrayInputStream(it), filename)
            }))
        }
    }

    fun sendImageAsDoc(chatId: String, title: String, content: ByteArray): String {
        val method = SendDocument(chatId,
            InputFile(
                ByteArrayInputStream(content),
                title   
            )
        )
        val result = client.execute(method)
        return result.document.fileId
    }

    fun sendDocsGroup(chatId: String, docIds: List<String>) {
        if (docIds.size == 1) {
            client.execute(SendDocument(chatId, InputFile(docIds.first())))
        } else {
            client.execute(SendMediaGroup(chatId, docIds.map {
                InputMediaDocument(it)
            }))
        }
    }

    fun deleteMessage(chatId: String, messageId: Long) {
        client.execute(DeleteMessage(chatId, messageId.toInt()))
    }
}