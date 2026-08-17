package com.litechat.android.ui

/**
 * First paint uses default AppSettings (onboardingDone=false) before DataStore
 * has been read. Treating that as "show setup" forced a key screen on every
 * cold start / process death, then a second key paste could overwrite Gemini
 * with the OpenAI defaults.
 */
object SetupGate {
    fun showOnboarding(settingsReady: Boolean, onboardingDone: Boolean): Boolean =
        settingsReady && !onboardingDone

    fun restoreConversationId(activeId: String?, ids: List<String>): String? {
        if (activeId != null && ids.contains(activeId)) return activeId
        return ids.firstOrNull()
    }
}
