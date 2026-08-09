package com.litechat.android.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build

/**
 * Honest device / free-RAM compatibility (ReOldAi-style matrix, modernized).
 *
 * 4GB installed ≠ free RAM: GMS/OEM often leave ~1.5–2.5 GB. We surface
 * *avail* MemInfo first so users don't confuse SoftRAM marketing with reality.
 */
object DeviceCompat {

    enum class Band {
        /** < 1.0 GB free — chat OK if careful; no extras */
        TIGHT,
        /** 1.0–2.0 GB free — default daily driver band for LiteChat */
        COMFORTABLE,
        /** 2.0–3.5 GB free — room for a second app while streaming */
        ROOMY,
        /** ≥ 3.5 GB free — headroom; still not a 7B local brain */
        GENEROUS,
    }

    enum class Verdict {
        /** Green — recommended on this device */
        GO,
        /** Yellow — works with caveats */
        CAUTION,
        /** Red — do not expect this mode here */
        NO,
    }

    data class Snapshot(
        val totalRamMb: Long,
        val availRamMb: Long,
        val lowMemory: Boolean,
        val thresholdMb: Long,
        val band: Band,
        val androidRelease: String,
        val apiLevel: Int,
        val abi: String,
    ) {
        val headline: String
            get() = when (band) {
                Band.TIGHT -> "Tight free RAM — LiteChat chat mode is still designed for this"
                Band.COMFORTABLE -> "Good fit — thin BYOK chat is the right product here"
                Band.ROOMY -> "Roomy — chat is smooth; still keep heavy brains off-device"
                Band.GENEROUS -> "Generous free RAM — chat is fine; local 7B+ still not this app"
            }

        val summaryLine: String
            get() = "${availRamMb} MB free · ${totalRamMb} MB total · Android $androidRelease"
    }

    data class MatrixRow(
        val feature: String,
        val tight: Verdict,
        val comfortable: Verdict,
        val roomy: Verdict,
        val generous: Verdict,
        val note: String = "",
    ) {
        fun forBand(band: Band): Verdict = when (band) {
            Band.TIGHT -> tight
            Band.COMFORTABLE -> comfortable
            Band.ROOMY -> roomy
            Band.GENEROUS -> generous
        }
    }

    /** Static product truth — same rows for every device; highlight = live band. */
    val MATRIX: List<MatrixRow> = listOf(
        MatrixRow(
            feature = "BYOK cloud / LAN chat (this app)",
            tight = Verdict.GO,
            comfortable = Verdict.GO,
            roomy = Verdict.GO,
            generous = Verdict.GO,
            note = "Thin UI + SSE; brain is remote",
        ),
        MatrixRow(
            feature = "Banner ads (free tier)",
            tight = Verdict.CAUTION,
            comfortable = Verdict.GO,
            roomy = Verdict.GO,
            generous = Verdict.GO,
            note = "Single banner only; Pro removes it",
        ),
        MatrixRow(
            feature = "Long chat history in memory",
            tight = Verdict.CAUTION,
            comfortable = Verdict.GO,
            roomy = Verdict.GO,
            generous = Verdict.GO,
            note = "History lives on disk; UI pages what it needs",
        ),
        MatrixRow(
            feature = "Image generation + attachments",
            tight = Verdict.CAUTION,
            comfortable = Verdict.GO,
            roomy = Verdict.GO,
            generous = Verdict.GO,
            note = "Coil cache clamped: 2/5/10/20MB per band; trim on low memory",
        ),
        MatrixRow(
            feature = "LAN Ollama on PC (phone thin client)",
            tight = Verdict.GO,
            comfortable = Verdict.GO,
            roomy = Verdict.GO,
            generous = Verdict.GO,
            note = "Phone stays thin; PC holds the model",
        ),
        MatrixRow(
            feature = "Ollama *on this phone*",
            tight = Verdict.NO,
            comfortable = Verdict.CAUTION,
            roomy = Verdict.CAUTION,
            generous = Verdict.CAUTION,
            note = "Tiny models only; heat + LMK risk",
        ),
        MatrixRow(
            feature = "Full on-device agent runtime (OpenClaw-class)",
            tight = Verdict.NO,
            comfortable = Verdict.NO,
            roomy = Verdict.CAUTION,
            generous = Verdict.CAUTION,
            note = "Different product — not LiteChat default",
        ),
        MatrixRow(
            feature = "Local 7B+ LLM resident",
            tight = Verdict.NO,
            comfortable = Verdict.NO,
            roomy = Verdict.NO,
            generous = Verdict.NO,
            note = "Needs desktop / VPS / 8–12GB+ device",
        ),
    )

    fun snapshot(context: Context): Snapshot {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        val totalMb = (mi.totalMem / (1024L * 1024L)).coerceAtLeast(0)
        val availMb = (mi.availMem / (1024L * 1024L)).coerceAtLeast(0)
        val thrMb = (mi.threshold / (1024L * 1024L)).coerceAtLeast(0)
        val band = bandFor(availMb = availMb, totalMb = totalMb)
        val abi = if (Build.SUPPORTED_ABIS.isNotEmpty()) Build.SUPPORTED_ABIS[0] else Build.CPU_ABI
        return Snapshot(
            totalRamMb = totalMb,
            availRamMb = availMb,
            lowMemory = mi.lowMemory,
            thresholdMb = thrMb,
            band = band,
            androidRelease = Build.VERSION.RELEASE ?: "?",
            apiLevel = Build.VERSION.SDK_INT,
            abi = abi,
        )
    }

    fun bandFor(availMb: Long, totalMb: Long = 0): Band {
        // Prefer free RAM; if avail is weird/zero at boot, fall back to total bands.
        val basis = if (availMb > 64) availMb else totalMb / 2
        return when {
            basis < 1024 -> Band.TIGHT
            basis < 2048 -> Band.COMFORTABLE
            basis < 3584 -> Band.ROOMY
            else -> Band.GENEROUS
        }
    }

    fun verdictGlyph(v: Verdict): String = when (v) {
        Verdict.GO -> "🟩"
        Verdict.CAUTION -> "🟨"
        Verdict.NO -> "🟥"
    }

    fun bandLabel(band: Band): String = when (band) {
        Band.TIGHT -> "<1 GB free"
        Band.COMFORTABLE -> "1–2 GB free"
        Band.ROOMY -> "2–3.5 GB free"
        Band.GENEROUS -> "≥3.5 GB free"
    }
}
