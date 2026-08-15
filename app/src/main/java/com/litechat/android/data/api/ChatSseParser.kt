package com.litechat.android.data.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Pure, dependency-light SSE parser for OpenAI-compatible /chat/completions
 * streams. No Android or OkHttp types — fully unit-testable on the JVM.
 *
 * Line protocol: only `data:` lines carry payloads; `[DONE]` terminates;
 * anything malformed is ignored (never throws).
 */
object ChatSseParser {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse one SSE line into a [StreamEvent].
     *
     * Accepts either a full `data:` line (OpenAI wire format) or an
     * already-stripped payload (what [StreamParser.parseSSE] emits).
     *
     * @return [StreamEvent.Delta] for a content chunk (non-empty text),
     *   [StreamEvent.Error] for an `error` payload, [StreamEvent.Done] for
     *   `[DONE]`, or `null` for non-`data:` lines, blank payloads, empty
     *   deltas, and malformed JSON (all silently skipped).
     */
    fun parseEvent(line: String): StreamEvent? {
        val payload = dataPayload(line) ?: line.trim().takeIf { it.isNotEmpty() } ?: return null
        val p = payload.trim()
        if (p.isEmpty()) return null
        if (p == "[DONE]") return StreamEvent.Done

        val chunk = parseChunk(p) ?: return null
        val delta = deltaOf(chunk)
        if (delta != null) {
            return if (delta.isEmpty()) null else StreamEvent.Delta(delta)
        }
        val err = errorOf(chunk) ?: return null
        return StreamEvent.Error(err)
    }

    /** Payload of a `data:` line, or null for any other line type. */
    fun dataPayload(line: String): String? =
        if (line.startsWith("data:")) line.removePrefix("data:").trim() else null

    /** Parse a payload string to JSON; null for blank or malformed input. */
    fun parseChunk(payload: String): JsonObject? {
        val p = payload.trim()
        if (p.isEmpty()) return null
        return try {
            json.parseToJsonElement(p).jsonObject
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Content delta from a chunk: `choices[0].delta.content`, falling back to
     * legacy `choices[0].text`. Returns "" when choices exist but carry no
     * content (typical role/finish_reason frames); null when the shape is
     * wrong (e.g. an error payload).
     */
    fun deltaOf(chunk: JsonObject): String? {
        val choices = chunk["choices"]?.jsonArray ?: return null
        if (choices.isEmpty()) return ""
        val choice = choices[0].jsonObject
        val delta = choice["delta"]?.jsonObject
        return delta?.get("content")?.jsonPrimitive?.contentOrNull
            ?: choice["text"]?.jsonPrimitive?.contentOrNull
            ?: ""
    }

    /** Error message from a chunk, if the payload carries an `error` field. */
    fun errorOf(chunk: JsonObject): String? {
        val err = chunk["error"] ?: return null
        return when (err) {
            is JsonObject -> err["message"]?.jsonPrimitive?.contentOrNull
            else -> err.toString()
        }
    }
}
