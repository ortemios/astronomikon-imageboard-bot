package ru.ortemios.imagebot.imboard

import okhttp3.OkHttpClient
import okhttp3.Request

class ImageDownloader(private val httpClient: OkHttpClient) {
    private val maxImageSizeBytes = 10485760
    private val maxSizeSum = 10000

    fun download(url: String): ImboardImage {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        val imageBytes = response.body.bytes()
        return ImboardImage(
            title = extractImageName(url),
            content = resize(imageBytes),
        )
    }

    private fun extractImageName(url: String): String {
        return url.split("/").last()
    }

    private fun resize(imageBytes: ByteArray): ByteArray {
        return imageBytes // TODO: implement resizing
    }
}