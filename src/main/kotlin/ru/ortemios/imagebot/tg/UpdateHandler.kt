package ru.ortemios.imagebot.tg

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer
import org.telegram.telegrambots.meta.api.objects.Update
import java.util.logging.Logger

class UpdateHandler {
    private val log = Logger.getLogger(this::javaClass.name)

    suspend fun start(botToken: String, handler: suspend (update: Update) -> Unit) {
        coroutineScope {
            val botsApplication = TelegramBotsLongPollingApplication()
            botsApplication.registerBot(botToken, object : LongPollingSingleThreadUpdateConsumer {
                override fun consume(update: Update) {
                    launch(Dispatchers.Default) {
                        try {
                            handler(update)
                        } catch (e: Throwable) {
                            log.severe(e.stackTraceToString())
                        }
                    }
                }
            })
            Thread.currentThread().join()
        }
    }
}