package com.litechat.android.util

/**
 * P-001: free users get one voice exchange per calendar day. Pro is unlimited.
 * Pure so tests don't need Android.
 */
object VoiceDailyLimit {
    const val FREE_PER_DAY = 1

    fun allowed(isPro: Boolean, usedToday: Int): Boolean =
        isPro || usedToday < FREE_PER_DAY

    fun nextCount(isPro: Boolean, usedToday: Int): Int =
        if (isPro) usedToday else usedToday + 1
}
