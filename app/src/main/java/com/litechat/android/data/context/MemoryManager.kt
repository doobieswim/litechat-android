package com.litechat.android.data.context

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * C-020: Lightweight persistent memory.
 * Tracks repeated user facts, promotes to system prompt after hitCount >= 5.
 * Pattern from Kai 9000.
 */
class MemoryManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("litechat_memory", Context.MODE_PRIVATE)

    data class MemoryEntry(val fact: String, val hitCount: Int = 1)

    fun record(fact: String) {
        val memories = getAll().toMutableList()
        val idx = memories.indexOfFirst { it.fact.equals(fact, ignoreCase = true) }
        if (idx >= 0) {
            memories[idx] = memories[idx].copy(hitCount = memories[idx].hitCount + 1)
        } else {
            memories.add(MemoryEntry(fact))
        }
        saveAll(memories)
    }

    fun getPromoted(threshold: Int = 5): List<String> =
        getAll().filter { it.hitCount >= threshold }.map { it.fact }

    fun getMemoryPrompt(): String {
        val facts = getPromoted()
        if (facts.isEmpty()) return ""
        return facts.joinToString("; ", prefix = "User preferences: ", postfix = ".")
    }

    private fun getAll(): List<MemoryEntry> {
        val raw = prefs.getString("memories", "[]") ?: "[]"
        return try {
            Json.parseToJsonElement(raw).jsonArray.map { el ->
                val obj = el.jsonObject
                MemoryEntry(
                    obj["fact"]!!.jsonPrimitive.content,
                    obj["hitCount"]!!.jsonPrimitive.content.toInt()
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun saveAll(list: List<MemoryEntry>) {
        val sb = StringBuilder("[")
        list.forEachIndexed { i, m ->
            if (i > 0) sb.append(",")
            sb.append("{\"fact\":\"${m.fact}\",\"hitCount\":${m.hitCount}}")
        }
        sb.append("]")
        prefs.edit().putString("memories", sb.toString()).apply()
    }
}