package ru.ortemios.imagebot.imboard

import okhttp3.OkHttpClient
import okhttp3.Request

class ImageUrlExtractor(private val client: OkHttpClient) {

    fun extract(pageUrl: String): String {
        val request = Request.Builder()
            .url(pageUrl)
            .build()

        val response = client.newCall(request).execute()
        val html = response.body.string()
        val regexp = Regex("https://cdn\\.donmai\\.us/original/[\\w/]*\\.[\\wd]*")
        return regexp.find(html)?.value ?: throw UrlNotFoundException(pageUrl)
    }

    class UrlNotFoundException(val url: String) : RuntimeException("Image URL not found on page: $url")
}