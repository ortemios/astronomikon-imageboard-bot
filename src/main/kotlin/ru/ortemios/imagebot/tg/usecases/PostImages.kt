package ru.ortemios.imagebot.tg.usecases

import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.gateway.MessageGateway

class PostImages(
    private val userService: UserService,
    private val messageGateway: MessageGateway,
    private val checkUserAccess: CheckUserAccess
) {

    suspend fun execute(user: User, urls: String) {
        if (checkUserAccess.execute(user)) {
            val groupId = userService.getGroup(user.id)
            if (groupId != null) {
                messageGateway.sendTextMessage(groupId, urls) // TODO: call image uploading
            } else {
                messageGateway.sendTextMessage(user.id, Messages.groupNotSetWarning)
            }
        }
    }
}