package com.litechat.android.util

import android.content.Context
import java.io.File

/**
 * C-029: FIFO disk cap for generated media (images/videos).
 *
 * Generated files live in cacheDir and are not covered by Coil's cache or the
 * chat DB. Without a cap they accumulate forever on weak devices. This walks
 * only the generated-media prefix (`gen_*.jpg`, `vid_*.mp4`) and evicts oldest
 * files first once total bytes exceed a band-tuned ceiling.
 *
 * Band-tuned caps (mirror ImageCacheConfig): TIGHT=20MB, COMFORTABLE=50MB,
 * ROOMY/GENEROUS=150MB. Never touches the Coil disk cache or chat DB.
 */
object MediaCleanup {

    private const val IMAGE_PREFIX = "gen_"
    private const val VIDEO_PREFIX = "vid_"

    fun capBytes(band: DeviceCompat.Band): Long = when (band) {
        DeviceCompat.Band.TIGHT       -> 20L * 1024 * 1024
        DeviceCompat.Band.COMFORTABLE -> 50L * 1024 * 1024
        else                          -> 150L * 1024 * 1024
    }

    /**
     * Evict oldest generated media until the band's cap is respected.
     * Idempotent and safe to call after every successful generation.
     */
    fun run(context: Context) {
        val band = DeviceCompat.snapshot(context).band
        val dir = context.cacheDir
        val media = dir.listFiles { f -> isGeneratedMedia(f) } ?: return
        if (media.isEmpty()) return

        val sorted = media.sortedBy { it.lastModified() } // oldest first
        val cap = capBytes(band)
        var total = sorted.sumOf { it.length() }

        for (f in sorted) {
            if (total <= cap) break
            val len = f.length()
            if (f.delete()) total -= len
        }
    }

    fun isGeneratedMedia(f: File): Boolean {
        val n = f.name
        return n.startsWith(IMAGE_PREFIX) || n.startsWith(VIDEO_PREFIX)
    }
}
