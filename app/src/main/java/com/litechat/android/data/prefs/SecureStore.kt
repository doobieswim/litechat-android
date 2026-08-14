package com.litechat.android.data.prefs

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * API key storage. Uses EncryptedSharedPreferences (AES-256).
 * Never log the key.
 */
class SecureStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "litechat_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getApiKey(): String = prefs.getString(KEY_API, "") ?: ""

    fun setApiKey(value: String) {
        prefs.edit().putString(KEY_API, value.trim()).apply()
    }

    /** C-017: failover provider keys, stored encrypted (REVIEW finding B1). */
    fun getProviderKey(providerId: String): String =
        prefs.getString(providerKey(providerId), "") ?: ""

    fun setProviderKey(providerId: String, value: String) {
        prefs.edit().putString(providerKey(providerId), value.trim()).apply()
    }

    fun removeProviderKey(providerId: String) {
        prefs.edit().remove(providerKey(providerId)).apply()
    }

    companion object {
        private const val KEY_API = "api_key"
        private fun providerKey(id: String) = "provider_key_$id"
    }
}
