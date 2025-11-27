package ru.ortemios.imagebot.imboard

import okhttp3.OkHttpClient
import okhttp3.Request
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.logging.Logger
import javax.imageio.ImageIO
import kotlin.math.min

class ImageDownloader(private val httpClient: OkHttpClient) {
    private val maxImageSizeBytes = 10485760
    private val maxSizeSum = 10000

    private val log = Logger.getLogger(this::javaClass.name)

    fun download(url: String): ImboardImage {
        val request = Request.Builder()
            .url(url)
            .build()
        val response = httpClient.newCall(request).execute()
        val imageBytes = response.body.bytes()
        try {
            return ImboardImage(
                title = extractImageName(url),
                content = resize(imageBytes),
            )
        } catch (e: Throwable) {
            log.warning(e.stackTraceToString())
            throw UnsupportedFormatException(url)
        }
    }

    private fun extractImageName(url: String): String {
        return url.split("/").last()
    }

    private fun resize(imageBytes: ByteArray): ByteArray {
        val image = ImageIO.read(ByteArrayInputStream(imageBytes))
        val scaleFactor = min(
            scaleBySize(imageBytes.size),
            scaleByDimension(image.width, image.height)
        )
        if (scaleFactor < 1) {
            val resized = BufferedImage(
                (image.width * scaleFactor).toInt(),
                (image.height * scaleFactor).toInt(),
                image.type
            )
            val g = resized.createGraphics()
            g.drawImage(image, 0, 0, resized.width, resized.height, null)
            g.dispose()

            val out = ByteArrayOutputStream()
            ImageIO.write(resized, "png", out)
            return out.toByteArray()
        } else {
            return imageBytes
        }
    }

    private fun scaleBySize(imageSizeBytes: Int): Double {
        return maxImageSizeBytes.toDouble() / imageSizeBytes
    }

    private fun scaleByDimension(width: Int, height: Int): Double {
        return maxSizeSum.toDouble() / (width + height)
    }

    class UnsupportedFormatException(val url: String) : RuntimeException("Unsupported image format: $url")
}