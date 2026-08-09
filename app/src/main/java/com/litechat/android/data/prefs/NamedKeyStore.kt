package com.litechat.android.data.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * C-023: Multi-key per provider support.
 * Stores named API keys with encrypted values.
 * Builds on existing SecureStore pattern.
 */
class NamedKeyStore(context: Context) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "litechat_named_keys",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    data class NamedKey(val name: String, val key: String, val isActive: Boolean = false)

    fun getAll(): List<NamedKey> {
        val json = prefs.getString("keys", "[]") ?: "[]"
        return try {
            val arr = kotlinx.serialization.json.Json.parseToJsonElement(json).jsonArray
            arr.map { el ->
                val obj = el.jsonObject
                NamedKey(
                    name = obj["name"]!!.jsonPrimitive.content,
                    key = obj["key"]?.jsonPrimitive?.content ?: "",
                    isActive = obj["isActive"]?.jsonPrimitive?.content?.toBoolean() ?: false,
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    fun getActiveKey(): String =
        getAll().firstOrNull { it.isActive }?.key ?: ""

    fun save(key: NamedKey) {
        val list = getAll().toMutableList()
        val idx = list.indexOfFirst { it.name == key.name }
        if (idx >= 0) list[idx] = key else list.add(key)
        if (key.isActive) {
            list.forEachIndexed { i, k ->
                if (i != idx && k.isActive) list[i] = k.copy(isActive = false)
            }
        }
        persist(list)
    }

    fun delete(name: String) {
        persist(getAll().filter { it.name != name })
    }

    private fun persist(list: List<NamedKey>) {
        val sb = StringBuilder("[")
        list.forEachIndexed { i, k ->
            if (i > 0) sb.append(",")
            sb.append("""{"name":"${k.name}","key":"${k.key}","isActive":${k.isActive}}""")
        }
        sb.append("]")
        prefs.edit().putString("keys", sb.toString()).apply()
    }
}