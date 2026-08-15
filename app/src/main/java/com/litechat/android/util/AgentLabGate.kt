package com.litechat.android.util

import android.content.Context

/**
 * C-034: opt-in Agent Lab *door* only.
 *
 * This is not an installer. It never downloads Termux, Node, Python, or proot.
 * Weak phones get a hard no. Stronger phones still only see a door: chat stays
 * the product; the fat agent box stays outside this APK.
 */
object AgentLabGate {

    enum class Decision {
        REFUSE,
        WARN,
        ALLOW_DOOR,
    }

    data class Result(
        val decision: Decision,
        val reason: String,
    )

    const val TERMUX_PACKAGE = "com.termux"
    const val MIN_FREE_STORAGE_MB = 400L

    fun decide(band: DeviceCompat.Band, freeStorageMb: Long): Result {
        if (freeStorageMb < MIN_FREE_STORAGE_MB) {
            return Result(
                Decision.REFUSE,
                "Not enough empty storage for an agent box (need about 400 MB). " +
                    "Chat still works. An agent box is not in this app.",
            )
        }
        return when (band) {
            DeviceCompat.Band.TIGHT, DeviceCompat.Band.COMFORTABLE -> Result(
                Decision.REFUSE,
                "This phone does not have enough free memory for an agent box. " +
                    "Chat still works. An agent box is not in this app.",
            )
            DeviceCompat.Band.ROOMY -> Result(
                Decision.WARN,
                "This phone might run an agent box, but it can get slow or close itself. " +
                    "An agent box is not in this app.",
            )
            DeviceCompat.Band.GENEROUS -> Result(
                Decision.ALLOW_DOOR,
                "This phone has room. We still keep the extra computer out " +
                    "so this chat app stays small. An agent box is not in this app.",
            )
        }
    }

    fun mayOpenTermux(decision: Decision, termuxInstalled: Boolean): Boolean =
        termuxInstalled && decision != Decision.REFUSE

    fun freeStorageMb(context: Context): Long {
        val stat = android.os.StatFs(context.filesDir.absolutePath)
        return (stat.availableBytes / (1024L * 1024L)).coerceAtLeast(0)
    }

    fun isTermuxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE, 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}
