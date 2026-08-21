package com.litechat.android.data.prefs

/**
 * B-001 / R-019: keys go into HTTP headers. OkHttp rejects non-ASCII
 * (phone paste can prefix U+2076). Never put the raw exception in the UI —
 * it includes the header value (the key).
 */
object ApiKeySanitizer {
    const val BAD_KEY_LINE =
        "This key has a bad character. Delete it and paste again."

    /** Bytes OkHttp allows in a header value, minus space/tab. */
    fun headerSafe(raw: String): String =
        raw.filter { it.code in 0x21..0x7E }

    fun isIllegalHeader(e: Throwable): Boolean {
        val m = e.message.orEmpty()
        return m.contains("Unexpected char")
    }

    fun userSafeError(e: Throwable, kind: String): String {
        if (isIllegalHeader(e)) return BAD_KEY_LINE
        val msg = e.message?.take(80) ?: "Unknown error"
        return "$kind failed: $msg"
    }
}
