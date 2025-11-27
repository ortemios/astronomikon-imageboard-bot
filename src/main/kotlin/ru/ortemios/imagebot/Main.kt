package ru.ortemios.imagebot

import okhttp3.OkHttpClient
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import ru.ortemios.imagebot.db.datasource.UserDataSource
import ru.ortemios.imagebot.db.setup.ConnectionFactory
import ru.ortemios.imagebot.domain.entity.User
import ru.ortemios.imagebot.domain.service.UserService
import ru.ortemios.imagebot.imboard.ImageDownloader
import ru.ortemios.imagebot.imboard.ImageUrlExtractor
import ru.ortemios.imagebot.tg.UpdateHandler
import ru.ortemios.imagebot.tg.gateway.MessageGateway
import ru.ortemios.imagebot.tg.res.CommandMatcher
import ru.ortemios.imagebot.tg.res.Commands
import ru.ortemios.imagebot.tg.res.Messages
import ru.ortemios.imagebot.tg.usecases.CheckUserAccess
import ru.ortemios.imagebot.tg.usecases.SetGroup
import ru.ortemios.imagebot.tg.usecases.postimages.PostImages
import java.time.Duration
import java.util.logging.Logger


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
    val log = Logger.getLogger("Bot")

    val commandMatcher = CommandMatcher()
    commandMatcher.registerCommand(Commands.setGroup, 1) { user, args ->
        setGroupUseCase.execute(user, args[0])
    }

    UpdateHandler().start(botToken) { update ->
        if (update.hasMessage()) {
            val text = update.message.text
            val from = update.message.from
            val user = User(
                id = from.id.toString(),
                username = from.userName,
            )
            log.info("Received message `$text` from `$user`")
            try {
                if (!commandMatcher.handle(user, text)) {
                    postImages.execute(user, text)
                }
            } catch (e: CommandMatcher.IncorrectSyntaxException) {
                messageGateway.sendTextMessage(user.id, Messages.INCORRECT_SYNTAX)
            }
        }
    }
}

