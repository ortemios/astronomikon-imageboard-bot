package ru.ortemios.imagebot

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.types.configuration.BotConfiguration
import eu.vendeli.tgbot.utils.common.onMessage
import okhttp3.OkHttpClient
import ru.ortemios.imagebot.db.datasource.UserDataSource
import ru.ortemios.imagebot.db.setup.ConnectionFactory
import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.imboard.ImageDownloader
import ru.ortemios.imagebot.imboard.ImageUrlExtractor
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.tg.res.Commands
import ru.ortemios.imagebot.tg.usecases.CheckUserAccess
import ru.ortemios.imagebot.tg.usecases.SetGroup
import ru.ortemios.imagebot.tg.usecases.postimages.PostImages
import java.time.Duration

suspend fun main() {
    val bot = TelegramBot(
        token = System.getenv("BOT_TOKEN"),
        botConfiguration = {
            updatesListener {
                updatesPollingTimeout = 60
            }
        }
    )
    val dbConn = ConnectionFactory().createConnection(System.getenv("DB_URL"))
    val httpClient = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(10))
        .build()

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
        checkUserAccess = checkUserAccess,
        urlExtractor = ImageUrlExtractor(httpClient),
        imageDownloader = ImageDownloader(httpClient)
    )

    try {
        bot.handleUpdates {
            onMessage {
                val user = User(
                    id = update.user.id.toString(),
                    username = update.user.username ?: ""
                )
                val text = update.text

                // TODO: refactor command matching
                Commands.matchCommand(Commands.setGroup, text).let { args ->
                    args?.getOrNull(0)?.let { groupId ->
                        setGroupUseCase.execute(user, groupId)
                    } ?: postImages.execute(user, text)
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}