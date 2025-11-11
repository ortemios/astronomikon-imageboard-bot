package ru.ortemios.imagebot

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.utils.common.onMessage
import ru.ortemios.imagebot.db.setup.ConnectionFactory
import ru.ortemios.imagebot.db.datasource.UserDataSource
import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.tg.res.Commands
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.usecases.CheckUserAccess
import ru.ortemios.imagebot.tg.usecases.PostImages
import ru.ortemios.imagebot.tg.usecases.SetGroup

suspend fun main() {
    val bot = TelegramBot(System.getenv("BOT_TOKEN"))
    val dbConn = ConnectionFactory().createConnection(System.getenv("DB_URL"))

    val messageGateway = MessageGateway(bot)
    val userDataSource = UserDataSource(dbConn)
    val userService = UserService(userDataSource)

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
            try {
                val user = User(
                    id = update.user.id.toString(),
                    username = update.user.username ?: ""
                )
                val text = update.text

                // TODO: refactor command matching
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
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}