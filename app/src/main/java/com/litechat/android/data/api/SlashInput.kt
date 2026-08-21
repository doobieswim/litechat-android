package com.litechat.android.data.api

/**
 * Gboard can stick a letter or ⁶ in front of a slash command.
 * Peel a few junk characters so /imagine still runs.
 */
object SlashInput {
    private val COMMANDS = listOf(
        "/imagine ", "/video ", "/browse ", "/edit ", "/search ", "/recall",
        "/imagine", "/video", "/browse", "/edit", "/search",
    )

    fun peel(raw: String): String {
        val t = raw.trim()
        if (t.isEmpty()) return t
        for (cmd in COMMANDS) {
            val i = t.indexOf(cmd, ignoreCase = true)
            if (i in 1..4) return t.substring(i)
        }
        return t
    }
}
