package ru.ortemios.imagebot.tg.usecases

import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.tg.res.Messages

class CheckUserAccess(
    private val userService: UserService,
    private val messageGateway: MessageGateway
) {

    suspend fun execute(user: User): Boolean {
        if (userService.hasAccess(user)) {
            return true
        } else {
            messageGateway.sendTextMessage(user.id, Messages.accessDeniedError)
            return false
        }
    }
}