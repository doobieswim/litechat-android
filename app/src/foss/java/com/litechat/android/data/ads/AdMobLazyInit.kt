package com.litechat.android.data.ads

import android.content.Context

/**
 * FOSS flavor stub (C-002): no AdMob SDK is linked in this build, so init is a
 * no-op. The play flavor carries the real lazy initializer.
 */
object AdMobLazyInit {
    fun ensureInitialized(context: Context) = Unit
}
