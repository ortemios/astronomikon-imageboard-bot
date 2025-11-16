package ru.ortemios.imagebot.tg.usecases

import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.gateway.MessageGateway

class SetGroup(
    private val userService: UserService,
    private val messageGateway: MessageGateway,
    private val checkUserAccess: CheckUserAccess
) {

    suspend fun execute(user: User, groupId: String) {
        if (checkUserAccess.execute(user)) {
            userService.setGroup(user.id, groupId)
            messageGateway.sendTextMessage(user.id, Messages.GROUP_SET)
        }
    }
}