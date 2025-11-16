package ru.ortemios.imagebot.imboard

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import khttp.responses.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ImageUrlExtractorTest {

    private val imageUrlExtractor = ImageUrlExtractor()
    private val mockResponse = mockk<Response>()

    private val url = "http://sample.url"

    @BeforeEach
    fun setUp() {
        mockkStatic("khttp.KHttp")
        every { khttp.get(url) } returns mockResponse
    }

    @Test
    fun testExtractUrlFound() {
        val expectedImageUrl = "https://cdn.donmai.us/original/ab/cd/1234567890abcdef.jpg"

        every { mockResponse.text } returns "<img src='$expectedImageUrl'>"
        val result = imageUrlExtractor.extract(url)
        assertEquals(result, expectedImageUrl)
    }

    @Test
    fun testExtractUrlNotFound() {
        every { mockResponse.text } returns "<img src='blah'>"
        assertThrows<ImageUrlExtractor.UrlNotFoundException> { imageUrlExtractor.extract(url) }
    }
}