package com.litechat.android.data.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FtsQueryTest {

    @Test
    fun `plain words become quoted tokens`() {
        assertEquals("\"hello\" \"world\"", FtsQuery.escape("hello world"))
    }

    @Test
    fun `surrounding quotes are stripped not treated as a phrase operator`() {
        assertEquals("\"hello\"", FtsQuery.escape("\"hello\""))
        assertEquals("\"hello\" \"world\"", FtsQuery.escape("\"hello\" world"))
    }

    @Test
    fun `dashes split words and never become NOT`() {
        assertEquals("\"foo\" \"bar\"", FtsQuery.escape("foo-bar"))
        assertEquals("\"hello\" \"world\"", FtsQuery.escape("hello -world"))
        assertEquals("\"hello\" \"world\"", FtsQuery.escape("hello --world"))
    }

    @Test
    fun `stars and other FTS marks are stripped`() {
        assertEquals("\"foo\"", FtsQuery.escape("foo*"))
        assertEquals("\"hello\"", FtsQuery.escape("hel*lo"))
        assertEquals("\"hello\"", FtsQuery.escape("(hello)"))
    }

    @Test
    fun `punctuation-only and blank queries are rejected`() {
        assertNull(FtsQuery.escape(""))
        assertNull(FtsQuery.escape("   "))
        assertNull(FtsQuery.escape("***"))
        assertNull(FtsQuery.escape("---"))
        assertNull(FtsQuery.escape("\"\"\""))
    }

    @Test
    fun `underscores also split like dashes`() {
        assertEquals("\"api\" \"key\"", FtsQuery.escape("api_key"))
    }
}
