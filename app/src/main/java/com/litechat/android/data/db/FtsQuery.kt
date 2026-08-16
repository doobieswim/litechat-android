package com.litechat.android.data.db

/**
 * P-002: turn what a person types into a safe FTS MATCH string.
 *
 * FTS treats quotes, stars, and dashes as operators. A search for
 * `foo-bar` must not become "foo NOT bar". We strip those marks,
 * keep letters/numbers, and quote each word.
 *
 * Returns null when nothing searchable remains (empty / punctuation only).
 */
object FtsQuery {
    private val splitMarks = Regex("[\\s\\-_]+")
    private val operatorMarks = Regex("""["*():^']""")
    private val keepChars = Regex("[^\\p{L}\\p{N}]+")

    fun escape(raw: String): String? {
        val tokens = raw.trim()
            .split(splitMarks)
            .map { operatorMarks.replace(it, "") }
            .map { keepChars.replace(it, "") }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return null
        return tokens.joinToString(" ") { token ->
            "\"" + token.replace("\"", "") + "\""
        }
    }
}
