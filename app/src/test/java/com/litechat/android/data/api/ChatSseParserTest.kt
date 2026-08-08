package com.litechat.android.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatSseParserTest {

    @Test
    fun `dataPayload strips prefix and trims`() {
        assertEquals("{\"a\":1}", ChatSseParser.dataPayload("data: {\"a\":1}"))
        assertEquals("{\"a\":1}", ChatSseParser.dataPayload("data:{\"a\":1}"))
        assertNull(ChatSseParser.dataPayload("event: message"))
        assertNull(ChatSseParser.dataPayload(": keep-alive comment"))
    }

    @Test
    fun `parseEvent returns Done for DONE sentinel`() {
        assertEquals(StreamEvent.Done, ChatSseParser.parseEvent("data: [DONE]"))
        assertEquals(StreamEvent.Done, ChatSseParser.parseEvent("data:[DONE]"))
    }

    @Test
    fun `parseEvent extracts content deltas`() {
        val ev = ChatSseParser.parseEvent(
            "data: {\"choices\":[{\"delta\":{\"content\":\"Hi\"},\"finish_reason\":null}]}"
        )
        assertTrue(ev is StreamEvent.Delta)
        assertEquals("Hi", (ev as StreamEvent.Delta).text)
    }

    @Test
    fun `parseEvent falls back to legacy text field`() {
        val ev = ChatSseParser.parseEvent(
            "data: {\"choices\":[{\"text\":\"legacy\"}]}"
        )
        assertTrue(ev is StreamEvent.Delta)
        assertEquals("legacy", (ev as StreamEvent.Delta).text)
    }

    @Test
    fun `parseEvent ignores role frames and empty deltas`() {
        assertNull(
            ChatSseParser.parseEvent(
                "data: {\"choices\":[{\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}"
            )
        )
        assertNull(ChatSseParser.parseEvent("data: {\"choices\":[{\"delta\":{}}]}"))
    }

    @Test
    fun `parseEvent extracts error payloads`() {
        val ev = ChatSseParser.parseEvent(
            "data: {\"error\":{\"message\":\"rate limited\",\"type\":\"rate_limit\"}}"
        )
        assertTrue(ev is StreamEvent.Error)
        assertEquals("rate limited", (ev as StreamEvent.Error).message)
    }

    @Test
    fun `parseEvent swallows malformed json and noise without throwing`() {
        assertNull(ChatSseParser.parseEvent("data: not-json"))
        assertNull(ChatSseParser.parseEvent("data: "))
        assertNull(ChatSseParser.parseEvent("random line"))
        assertNull(ChatSseParser.parseEvent(""))
        // Valid JSON but not a chunk shape → no delta, no error.
        assertNull(ChatSseParser.parseEvent("data: {\"foo\":1}"))
    }

    @Test
    fun `full stream assembles and terminates`() {
        val stream = listOf(
            "data: {\"choices\":[{\"delta\":{\"role\":\"assistant\"},\"finish_reason\":null}]}",
            "data: {\"choices\":[{\"delta\":{\"content\":\"Hi\"},\"finish_reason\":null}]}",
            "data: {\"choices\":[{\"delta\":{\"content\":\" there\"},\"finish_reason\":null}]}",
            "data: {\"choices\":[{\"delta\":{},\"finish_reason\":\"stop\"}]}",
            "data: [DONE]",
        )
        val parts = mutableListOf<String>()
        for (line in stream) {
            when (val ev = ChatSseParser.parseEvent(line)) {
                is StreamEvent.Delta -> parts.add(ev.text)
                StreamEvent.Done -> break
                else -> Unit
            }
        }
        assertEquals(listOf("Hi", " there"), parts)
        assertEquals("Hi there", parts.joinToString(""))
    }

    @Test
    fun `noise lines between chunks are skipped`() {
        val stream = listOf(
            ": keep-alive",
            "event: ping",
            "data: {\"choices\":[{\"delta\":{\"content\":\"ok\"},\"finish_reason\":null}]}",
            "data: not-json",
            "data: [DONE]",
        )
        val parts = mutableListOf<String>()
        for (line in stream) {
            when (val ev = ChatSseParser.parseEvent(line)) {
                is StreamEvent.Delta -> parts.add(ev.text)
                StreamEvent.Done -> break
                else -> Unit
            }
        }
        assertEquals(listOf("ok"), parts)
    }
}
