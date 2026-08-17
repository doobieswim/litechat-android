package com.litechat.android.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupGateTest {
    @Test
    fun `cold start defaults must not show setup`() {
        assertFalse(SetupGate.showOnboarding(settingsReady = false, onboardingDone = false))
    }

    @Test
    fun `after disk says done do not show setup`() {
        assertFalse(SetupGate.showOnboarding(settingsReady = true, onboardingDone = true))
    }

    @Test
    fun `first install after load shows setup`() {
        assertTrue(SetupGate.showOnboarding(settingsReady = true, onboardingDone = false))
    }

    @Test
    fun `restore keeps the open chat`() {
        assertEquals("b", SetupGate.restoreConversationId("b", listOf("a", "b", "c")))
    }

    @Test
    fun `restore opens newest when nothing is open`() {
        assertEquals("a", SetupGate.restoreConversationId(null, listOf("a", "b")))
    }

    @Test
    fun `restore is null when there are no chats`() {
        assertNull(SetupGate.restoreConversationId(null, emptyList()))
    }
}
