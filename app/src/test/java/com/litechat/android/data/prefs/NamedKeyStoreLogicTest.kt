package com.litechat.android.data.prefs

import com.litechat.android.data.prefs.NamedKeyStore.NamedKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NamedKeyStoreLogicTest {

    @Test
    fun `new active key stays active after insert`() {
        val list = listOf(NamedKey("old", "k1", isActive = true))
        val out = NamedKeyStore.withKey(list, NamedKey("new", "k2", isActive = true))
        val active = out.filter { it.isActive }
        assertEquals(1, active.size)
        assertEquals("new", active[0].name)
    }

    @Test
    fun `saving an active key deactivates the previous one`() {
        val list = listOf(NamedKey("old", "k1", isActive = true))
        val out = NamedKeyStore.withKey(list, NamedKey("new", "k2", isActive = true))
        assertFalse(out.first { it.name == "old" }.isActive)
        assertTrue(out.first { it.name == "new" }.isActive)
    }

    @Test
    fun `inactive key does not steal active flag`() {
        val list = listOf(NamedKey("old", "k1", isActive = true))
        val out = NamedKeyStore.withKey(list, NamedKey("new", "k2", isActive = false))
        assertTrue(out.first { it.name == "old" }.isActive)
        assertFalse(out.first { it.name == "new" }.isActive)
    }

    @Test
    fun `editing an existing key keeps its position and flags`() {
        val list = listOf(NamedKey("a", "k1", isActive = false), NamedKey("b", "k2", isActive = true))
        val out = NamedKeyStore.withKey(list, NamedKey("b", "k2-updated", isActive = true))
        assertEquals(2, out.size)
        assertEquals("k2-updated", out.first { it.name == "b" }.key)
        assertTrue(out.first { it.name == "b" }.isActive)
    }
}
