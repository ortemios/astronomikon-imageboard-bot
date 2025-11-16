package ru.ortemios.imagebot.tg.usecases.postimages

class MessageUrlExtractor {

    companion object {
        private val hosts = listOf(
            "https://danbooru.donmai.us/posts/",
            "https://betabooru.donmai.us/posts/"
        )

        fun extract(message: String): List<String> {
            return message.split("\n")
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .map {
                    if (!hosts.any { host -> it.startsWith(host) }) {
                        throw IncorrectUrlException(it)
                    }
                    it
                }
        }
    }

    class IncorrectUrlException(val url: String) : Exception("Incorrect imageboard URL: $url")
}