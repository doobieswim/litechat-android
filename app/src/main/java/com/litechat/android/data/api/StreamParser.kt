package com.litechat.android.data.api

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * Cold, backpressure-aware SSE line parser.
 *
 * Reads an [InputStream] line-by-line, emits every `data:` payload, and
 * drops older lines when the downstream consumer cannot keep up (bounded
 * Channel buffer of 16 with DROP_OLDEST). This keeps RSS flat on 4GB
 * devices even when a fast provider sends 100+ tokens/sec.
 *
 * The parser is pure, dependency-free, and testable on the JVM:
 * [java.io.ByteArrayInputStream] → [parseSSE] → [kotlinx.coroutines.flow.toList].
 */
object StreamParser {

    /** Default buffer capacity — one Compose frame at 60 fps is ~16ms, so 16 lines
     *  covers ~250ms of streaming at typical rates before dropping. */
    private const val BUFFER_CAPACITY = 16

    /**
     * Parse an SSE [InputStream] into a cold [Flow] of raw `data:` payloads.
     *
     * Non-`data:` lines (comments, event lines, blanks) are silently skipped.
     * The flow completes when the input stream is exhausted or the collector
     * cancels the coroutine.
     *
     * Wire with [buffer] to apply backpressure:
     * ```kotlin
     * parseSSE(response.body!!.byteStream())
     *     .buffer(capacity = BUFFER_CAPACITY, onBufferOverflow = BufferOverflow.DROP_OLDEST)
     * ```
     */
    fun parseSSE(input: InputStream): Flow<String> = flow {
        val reader = BufferedReader(InputStreamReader(input))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val trimmed = line!!.trim()
            if (trimmed.startsWith("data:")) {
                emit(trimmed.removePrefix("data:").trim())
            }
        }
    }
}