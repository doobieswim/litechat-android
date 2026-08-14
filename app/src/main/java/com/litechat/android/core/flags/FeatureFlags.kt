package com.litechat.android.core.flags

import com.litechat.android.BuildConfig

/**
 * Central feature toggle object — no DI, no runtime overhead.
 *
 * All deferrable features, Pro gates, and tunable constants live here so
 * a single file answers "what's enabled in this build?"
 *
 * C-006: streamThrottleMs governs the SSE delta → UI paint throttle.
 */
object FeatureFlags {
    /** Pro state: true when Play Billing purchase is verified OR local Dev flag. */
    var isPro: Boolean = false
        private set

    /** Set Pro once (called from ChatViewModel on billing purchase or debug toggle). */
    fun setPro(value: Boolean) {
        isPro = value
    }

    /** C-006: min ms between UI delta updates (numAi-plus pattern). */
    const val streamThrottleMs = 250L
}