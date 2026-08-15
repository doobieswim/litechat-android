package com.litechat.android.data.prefs

import com.litechat.android.data.prefs.SettingsRepository.Companion.decodeDrafts
import com.litechat.android.data.prefs.SettingsRepository.Companion.encodeDrafts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftStoreLogicTest {

    @Test
    fun `round trip preserves drafts including quotes and slashes`() {
        val map = mapOf(
            "conv-1" to "hello",
            "conv-2" to "draft with \"quotes\" and \\ slashes",
        )
        assertEquals(map, decodeDrafts(encodeDrafts(map)))
    }

    @Test
    fun `blank and null decode to empty`() {
        assertTrue(decodeDrafts(null).isEmpty())
        assertTrue(decodeDrafts("").isEmpty())
        assertTrue(decodeDrafts("   ").isEmpty())
    }

    @Test
    fun `garbage json decodes to empty without throwing`() {
        assertTrue(decodeDrafts("{not json").isEmpty())
        assertTrue(decodeDrafts("[\"wrong shape\"").isEmpty())
    }

    @Test
    fun `empty map encodes to an empty json object`() {
        assertEquals("{}", encodeDrafts(emptyMap()))
    }
}
