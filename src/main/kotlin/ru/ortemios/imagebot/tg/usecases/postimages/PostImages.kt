package ru.ortemios.imagebot.tg.usecases.postimages

import kotlinx.coroutines.sync.Semaphore
import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.imboard.ImageDownloader
import ru.ortemios.imagebot.imboard.ImageUrlExtractor
import ru.ortemios.imagebot.imboard.ImboardImage
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.tg.usecases.CheckUserAccess
import java.util.logging.Logger

class PostImages(
    private val userService: UserService,
    private val messageGateway: MessageGateway,
    private val checkUserAccess: CheckUserAccess,
    private val urlExtractor: ImageUrlExtractor,
    private val imageDownloader: ImageDownloader,
) {
    private val log = Logger.getLogger(this::javaClass.name)
    private val accessSemaphore = Semaphore(1)

    suspend fun execute(user: User, messageText: String) {
        if (checkUserAccess.execute(user)) {
            if (accessSemaphore.tryAcquire()) {
                try {
                    executeTask(
                        TaskInfo(messageId = null, user = user),
                        messageText
                    )
                } catch (e: Exception) {
                    log.warning(e.stackTraceToString())
                    messageGateway.sendTextMessage(user.id, Messages.GENERAL_ERROR)
                } finally {
                    accessSemaphore.release()
                }
            } else {
                messageGateway.sendTextMessage(user.id, Messages.SERVICE_IS_BUSY)
            }
        }
    }

    private fun executeTask(info: TaskInfo, messageText: String) {
        val groupId = userService.getGroup(info.user.id)
        if (groupId != null) {
            try {
                setTaskStatus(info, Messages.DOWNLOAD_STARTING)
                val urls = MessageUrlExtractor.extract(messageText)
                val images = downloadImages(info, urls)
                sendImages(info, groupId, images)
                setTaskStatus(info, Messages.DOWNLOAD_COMPLETE)
                log.info("Uploaded ${urls.size} images to $groupId")
            } catch (e: MessageUrlExtractor.IncorrectUrlException) {
                log.warning(e.toString())
                setTaskStatus(info, Messages.incorrectUrl(e.url))
            } catch (e: ImageUrlExtractor.UrlNotFoundException) {
                log.warning(e.toString())
                setTaskStatus(info, Messages.downloadUrlNotFound(e.url))
            } catch (e: ImageDownloader.UnsupportedFormatException) {
                log.warning(e.toString())
                setTaskStatus(info, Messages.unsupportedImageFormat(e.url))
            } catch (e: Exception) {
                log.severe(e.stackTraceToString())
                setTaskStatus(info, Messages.DOWNLOAD_FAILED)
            }
        } else {
            setTaskStatus(info, Messages.GROUP_NOT_SET_ERROR)
        }
    }

    private fun downloadImages(info: TaskInfo, urls: List<String>): List<ImboardImage> {
        setTaskStatus(
            info,
            Messages.downloadingImage(1, urls.size),
        )
        return urls.mapIndexed { index, pageUrl ->
            if (index > 0) {
                setTaskStatus(
                    info,
                    Messages.downloadingImage(index + 1, urls.size),
                )
            }
            val imageUrl = urlExtractor.extract(pageUrl)
            imageDownloader.download(imageUrl)
        }
    }

    private fun sendImages(info: TaskInfo, groupId: String, images: List<ImboardImage>) {
        setTaskStatus(
            info,
            Messages.DOWNLOAD_UPLOADING_SD
        )
        messageGateway.sendImagesGroup(groupId, images.map { it.content })
        setTaskStatus(
            info,
            Messages.DOWNLOAD_UPLOADING_HD,
        )
        val docIds = images.map {
            val docId = messageGateway.sendImageAsDoc(
                info.user.id,
                it.title,
                it.content
            )
            info.shouldRecreateMessage = true
            docId
        }
        messageGateway.sendDocsGroup(groupId, docIds)
    }

    private fun setTaskStatus(taskInfo: TaskInfo, text: String) {
        val messageId = taskInfo.messageId
        if (messageId == null || taskInfo.shouldRecreateMessage) {
            if (messageId != null) {
                messageGateway.deleteMessage(taskInfo.user.id, messageId)
            }
            taskInfo.messageId = messageGateway.sendTextMessage(taskInfo.user.id, text)
            taskInfo.shouldRecreateMessage = false
        } else {
            messageGateway.updateTextMessage(taskInfo.user.id, messageId, text)
        }
    }

    private data class TaskInfo(
        var messageId: Long?,
        val user: User,
        var shouldRecreateMessage: Boolean = false,
    )
}