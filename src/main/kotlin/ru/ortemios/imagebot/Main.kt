package ru.ortemios.imagebot

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.utils.common.onMessage
import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.tg.res.Commands
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.tg.usecases.CheckUserAccess
import ru.ortemios.imagebot.tg.usecases.PostImages
import ru.ortemios.imagebot.tg.usecases.SetGroup

suspend fun main() {
    val bot = TelegramBot(System.getenv("BOT_TOKEN"))

    val messageGateway = MessageGateway(bot)
    val userService = UserService()

    val checkUserAccess = CheckUserAccess(
        messageGateway = messageGateway,
        userService = userService
    )
    val setGroupUseCase = SetGroup(
        userService = userService,
        messageGateway = messageGateway,
        checkUserAccess = checkUserAccess
    )
    val postImages = PostImages(
        userService = userService,
        messageGateway = messageGateway,
        checkUserAccess = checkUserAccess
    )


    bot.handleUpdates {
        onMessage {
            val user = User(
                id = update.user.id.toString(),
                username = update.user.username ?: ""
            )
            val text = update.text

            Commands.matchCommand(Commands.setGroup, text).let { args ->
                if (args != null) {
                    args.getOrNull(0).let { groupId ->
                        if (groupId != null) {
                            setGroupUseCase.execute(user, groupId)
                        } else {
                            messageGateway.sendTextMessage(user.id, Messages.incorrectSyntaxError)
                        }
                    }
                } else {
                    postImages.execute(user, text)
                }
            }
        }
    }
}