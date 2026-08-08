package com.litechat.android.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class InputPolicyTest {

    @Test
    fun `short input passes through unchanged`() {
        assertEquals("hi", InputPolicy.cap("hi"))
        assertEquals("", InputPolicy.cap(""))
    }

    @Test
    fun `exactly max length passes through`() {
        val s = "x".repeat(InputPolicy.MAX_INPUT_CHARS)
        assertEquals(s, InputPolicy.cap(s))
    }

    @Test
    fun `oversized input is truncated to max`() {
        val s = "x".repeat(InputPolicy.MAX_INPUT_CHARS + 10_000)
        val capped = InputPolicy.cap(s)
        assertEquals(InputPolicy.MAX_INPUT_CHARS, capped.length)
    }

    @Test
    fun `max is the documented 32k paste ceiling`() {
        assertEquals(32_000, InputPolicy.MAX_INPUT_CHARS)
    }
}
