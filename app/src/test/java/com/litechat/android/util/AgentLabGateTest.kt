package com.litechat.android.util

import com.litechat.android.util.DeviceCompat.Band
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentLabGateTest {

    @Test
    fun `tight and comfortable phones are refused even with lots of storage`() {
        val tight = AgentLabGate.decide(Band.TIGHT, freeStorageMb = 8_000)
        val comfortable = AgentLabGate.decide(Band.COMFORTABLE, freeStorageMb = 8_000)
        assertEquals(AgentLabGate.Decision.REFUSE, tight.decision)
        assertEquals(AgentLabGate.Decision.REFUSE, comfortable.decision)
        assertTrue(tight.reason.contains("enough free memory", ignoreCase = true))
    }

    @Test
    fun `roomy is a warning, generous is the door`() {
        val roomy = AgentLabGate.decide(Band.ROOMY, freeStorageMb = 8_000)
        val generous = AgentLabGate.decide(Band.GENEROUS, freeStorageMb = 8_000)
        assertEquals(AgentLabGate.Decision.WARN, roomy.decision)
        assertEquals(AgentLabGate.Decision.ALLOW_DOOR, generous.decision)
    }

    @Test
    fun `low storage refuses even on a generous phone`() {
        val r = AgentLabGate.decide(Band.GENEROUS, freeStorageMb = 200)
        assertEquals(AgentLabGate.Decision.REFUSE, r.decision)
        assertTrue(r.reason.contains("storage", ignoreCase = true))
    }

    @Test
    fun `open Termux only when already installed and not refused`() {
        assertFalse(AgentLabGate.mayOpenTermux(AgentLabGate.Decision.REFUSE, termuxInstalled = true))
        assertFalse(AgentLabGate.mayOpenTermux(AgentLabGate.Decision.WARN, termuxInstalled = false))
        assertTrue(AgentLabGate.mayOpenTermux(AgentLabGate.Decision.WARN, termuxInstalled = true))
        assertTrue(AgentLabGate.mayOpenTermux(AgentLabGate.Decision.ALLOW_DOOR, termuxInstalled = true))
    }

    @Test
    fun `never claims the agent box lives inside this app`() {
        for (band in Band.entries) {
            val r = AgentLabGate.decide(band, freeStorageMb = 8_000)
            assertFalse(r.reason.contains("installing now", ignoreCase = true))
            assertTrue(r.reason.contains("not in this app", ignoreCase = true))
        }
    }
}
