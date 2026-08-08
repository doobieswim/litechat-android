package com.litechat.android.ui

/**
 * Pure input admission policy (no Android deps — unit-testable).
 *
 * The ceiling protects against the Compose `Constraints` overflow / OOM
 * failure class (gpt_mobile#226: "Can't represent a width of 905 and height
 * of 369898 in Constraints") on weak devices.
 */
object InputPolicy {
    /** Soft ceiling keeps layout measure and binder IPC safe on 4GB devices. */
    const val MAX_INPUT_CHARS = 32_000

    /** Cap a pasted/typed value to [MAX_INPUT_CHARS] characters. */
    fun cap(value: String): String =
        if (value.length > MAX_INPUT_CHARS) value.take(MAX_INPUT_CHARS) else value
}
