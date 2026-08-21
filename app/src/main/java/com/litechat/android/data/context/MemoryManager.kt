package com.litechat.android.data.context

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer
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

    @kotlinx.serialization.Serializable
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

    /** Wipe every stored memory fact (Settings → Clear memory). */
    fun clear() {
        prefs.edit().remove("memories").apply()
    }

    /** P-006: every stored fact, promoted or not. */
    fun list(): List<MemoryEntry> = getAll()

    fun update(oldFact: String, newFact: String) {
        val trimmed = newFact.trim()
        if (trimmed.isEmpty()) return
        val memories = getAll().toMutableList()
        val idx = memories.indexOfFirst { it.fact.equals(oldFact, ignoreCase = true) }
        if (idx < 0) return
        memories[idx] = memories[idx].copy(fact = trimmed)
        saveAll(memories)
    }

    fun delete(fact: String) {
        saveAll(getAll().filterNot { it.fact.equals(fact, ignoreCase = true) })
    }

    fun recall(topic: String): List<MemoryEntry> {
        val q = topic.trim()
        if (q.isEmpty()) return getPromoted().map { MemoryEntry(it, 5) }
        return getAll().filter { it.fact.contains(q, ignoreCase = true) }
    }

    /** P-006: store a rolling summary as an already-promoted memory. */
    fun addSummary(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val memories = getAll().toMutableList()
        memories.add(MemoryEntry("Summary: $trimmed", hitCount = 5))
        saveAll(memories)
    }

    private fun getAll(): List<MemoryEntry> {
        val raw = prefs.getString("memories", "[]") ?: "[]"
        return decodeList(raw)
    }

    private fun saveAll(list: List<MemoryEntry>) {
        val encoded = json.encodeToString(ListSerializer(MemoryEntry.serializer()), list)
        prefs.edit().putString("memories", encoded).apply()
    }

    companion object {
        private val json = Json { encodeDefaults = true }

        /** Missing hitCount must not wipe the whole list. */
        internal fun decodeList(raw: String): List<MemoryEntry> {
            return try {
                Json.parseToJsonElement(raw).jsonArray.mapNotNull { el ->
                    val obj = el.jsonObject
                    val fact = obj["fact"]?.jsonPrimitive?.content?.trim().orEmpty()
                    if (fact.isEmpty()) return@mapNotNull null
                    val hits = obj["hitCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: 1
                    MemoryEntry(fact, hits)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}