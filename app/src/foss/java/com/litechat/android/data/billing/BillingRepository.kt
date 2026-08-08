package com.litechat.android.data.billing

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * FOSS flavor stub (C-002): no Play Billing library is linked in this build.
 * Pro is always "not owned" and purchase attempts report unavailability — the
 * UI already handles that path gracefully ("Billing unavailable").
 */
class BillingRepository(context: Context) {

    private val _proOwned = MutableStateFlow(false)
    val proOwned: StateFlow<Boolean> = _proOwned.asStateFlow()

    fun startConnection(onReady: (Boolean) -> Unit = {}) {
        onReady(false) // billing simply doesn't exist here
    }

    fun queryOwned() = Unit

    fun launchPurchase(activity: Activity, onResult: (Boolean, String?) -> Unit) {
        onResult(false, "Billing unavailable in FOSS build")
    }

    fun endConnection() = Unit
}
