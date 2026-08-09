package com.litechat.android.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * Scans common LAN addresses for Ollama instances.
 * Tries popular router patterns: 192.168.0.x, 192.168.1.x, 10.0.0.x
 * On success, returns the Ollama API base URL.
 */
object LanDetector {
    private const val TIME_MS = 1_500L

    suspend fun scan(): String? = withContext(Dispatchers.IO) {
        // Quick scan: try common addresses on local /24
        val bases = mutableListOf<String>()
        for (octet in 1..10) {
            bases.add("http://192.168.0.$octet:11434")
            bases.add("http://192.168.1.$octet:11434")
            bases.add("http://10.0.0.$octet:11434")
        }
        for (base in bases) {
            try {
                val conn = URL("$base/api/tags").openConnection()
                conn.connectTimeout = TIME_MS.toInt()
                conn.readTimeout = TIME_MS.toInt()
                val text = conn.getInputStream().bufferedReader().readText()
                if (text.contains("\"name\"")) return@withContext base
            } catch (_: Exception) { /* try next */ }
        }
        null
    }
}