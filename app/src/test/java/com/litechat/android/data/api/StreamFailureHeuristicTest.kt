package com.litechat.android.data.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * numAi-plus stream-failure heuristic: zero tokens + these symptoms → retry
 * once with stream=false.
 */
class StreamFailureHeuristicTest {

    @Test
    fun `classic failure phrases are detected`() {
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("Socket closed"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("unexpected end of stream"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("malformed chunk"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("read timed out"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("JSON parse error"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("stream ended abruptly"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("connection reset"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("unexpected EOF"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("HTTP 500: server error"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("HTTP 429: too many requests"))
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("network is unreachable"))
    }

    @Test
    fun `non-failure messages are not detected`() {
        assertFalse(OpenAiCompatibleClient.isStreamClassFailure(""))
        assertFalse(OpenAiCompatibleClient.isStreamClassFailure("Invalid API key"))
        assertFalse(OpenAiCompatibleClient.isStreamClassFailure("Model not found"))
        assertFalse(OpenAiCompatibleClient.isStreamClassFailure("You exceeded your current quota"))
        assertFalse(OpenAiCompatibleClient.isStreamClassFailure("The model `gpt-4o` does not exist"))
    }

    @Test
    fun `case insensitivity and substring behavior`() {
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("CONNECTION RESET BY PEER"))
        // "stream" appears in "streaming" — heuristic is intentionally broad.
        assertTrue(OpenAiCompatibleClient.isStreamClassFailure("streaming response aborted"))
    }
}
