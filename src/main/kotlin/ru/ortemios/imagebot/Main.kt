package ru.ortemios.imagebot

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update
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
    val botToken = System.getenv("BOT_TOKEN")

    val dbConn = ConnectionFactory().createConnection(System.getenv("DB_URL"))
    val httpClient = OkHttpClient.Builder()
        .callTimeout(Duration.ofSeconds(60))
        .build()
    val messageGateway = MessageGateway(OkHttpTelegramClient(botToken))
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

    val botsApplication = TelegramBotsLongPollingApplication()
    botsApplication.registerBot(botToken, object: LongPollingSingleThreadUpdateConsumer {
        override fun consume(update: Update) {
            runBlocking {
                val text = update.message.text
                val from = update.message.from

                val user = User(
                    id = from.id.toString(),
                    username = from.userName,
                )

                // TODO: refactor command matching
                Commands.matchCommand(Commands.setGroup, text).let { args ->
                    args?.getOrNull(0)?.let { groupId ->
                        setGroupUseCase.execute(user, groupId)
                    } ?: postImages.execute(user, text)
                }
            }
        }
    })
    withContext(Dispatchers.IO) {
        Thread.currentThread().join()
    }
}