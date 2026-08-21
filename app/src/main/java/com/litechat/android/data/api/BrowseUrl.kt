package com.litechat.android.data.api

/**
 * /browse should take "example.com" — people should not have to type https://.
 */
object BrowseUrl {
    fun normalize(raw: String): String {
        var s = raw.trim().trim('"', '\'')
        if (s.isEmpty()) return s
        val lower = s.lowercase()
        if (lower.startsWith("javascript:") ||
            lower.startsWith("data:") ||
            lower.startsWith("file:")
        ) {
            return s
        }
        if (lower.startsWith("https://") || lower.startsWith("http://")) return s
        if (s.startsWith("//")) return "https:$s"
        // Typed "http//:" or "https//:" (missing colon after http)
        s = s.replaceFirst(Regex("^(https?)//:", RegexOption.IGNORE_CASE), "")
        s = s.trimStart('/')
        if (s.isEmpty()) return s
        val again = s.lowercase()
        if (again.startsWith("https://") || again.startsWith("http://")) return s
        return "https://$s"
    }
}
