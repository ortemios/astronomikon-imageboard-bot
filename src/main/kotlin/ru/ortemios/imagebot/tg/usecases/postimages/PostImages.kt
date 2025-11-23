package ru.ortemios.imagebot.tg.usecases.postimages

import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.imboard.ImageDownloader
import ru.ortemios.imagebot.imboard.ImageUrlExtractor
import ru.ortemios.imagebot.imboard.ImboardImage
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.tg.usecases.CheckUserAccess

class PostImages(
    private val userService: UserService,
    private val messageGateway: MessageGateway,
    private val checkUserAccess: CheckUserAccess,
    private val urlExtractor: ImageUrlExtractor,
    private val imageDownloader: ImageDownloader
) {

    suspend fun execute(user: User, messageText: String) {
        if (checkUserAccess.execute(user)) {
            val groupId = userService.getGroup(user.id)
            if (groupId != null) {
                try {
                    val urls = MessageUrlExtractor.extract(messageText)
                    val images = downloadImages(user, urls)
                    sendImages(user, groupId, images)
                    messageGateway.sendTextMessage(user.id, Messages.DOWNLOAD_COMPLETE)
                } catch (e: MessageUrlExtractor.IncorrectUrlException) {
                    messageGateway.sendTextMessage(user.id, Messages.incorrectUrl(e.url))
                } catch (e: ImageUrlExtractor.UrlNotFoundException) {
                    messageGateway.sendTextMessage(user.id, Messages.downloadUrlNotFound(e.url))
                } catch (e: Exception) {
                    e.printStackTrace()
                    messageGateway.sendTextMessage(user.id, Messages.DOWNLOAD_FAILED)
                }
            } else {
                messageGateway.sendTextMessage(user.id, Messages.GROUP_NOT_SET_ERROR)
            }
        }
    }

    private fun downloadImages(user: User, urls: List<String>): List<ImboardImage> {
        val messageId = messageGateway.sendTextMessage(
            user.id,
            Messages.downloadingImage(1, urls.size)
        )
        try {
            return urls.mapIndexed { index, pageUrl ->
                if (index > 0) {
                    messageGateway.updateTextMessage(
                        user.id,
                        messageId,
                        Messages.downloadingImage(index + 1, urls.size)
                    )
                }
                val imageUrl = urlExtractor.extract(pageUrl)
                imageDownloader.download(imageUrl)
            }
        } finally {
            messageGateway.deleteMessage(user.id, messageId)
        }
    }

    private fun sendImages(user: User, groupId: String, images: List<ImboardImage>) {
        val messageId = messageGateway.sendTextMessage(
            user.id,
            Messages.DOWNLOAD_UPLOADING_HD
        )
        try {
            messageGateway.sendImagesGroup(groupId, images.map { it.content })
            val docIds = images.map { messageGateway.sendImageAsDoc(
                user.id,
                it.title,
                it.content)
            }
            messageGateway.sendDocsGroup(groupId, docIds)
        } finally {
            messageGateway.deleteMessage(user.id, messageId)
        }
    }
}