package com.litechat.android.util

import coil3.ImageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy

/**
 * Per-band Coil image cache tuning for 4GB-first devices.
 *
 * Coil's default memory cache is 20% of app heap (~50MB on a 256MB heap).
 * On TIGHT/COMFORTABLE devices that competes with foreground processes.
 * We clamp it to band-appropriate values.
 */
object ImageCacheConfig {

    /**
     * Memory cache size in bytes per DeviceCompat.Band.
     * Values are aggressive reductions vs Coil defaults.
     */
    fun cacheSizeBytes(band: DeviceCompat.Band): Long = when (band) {
        DeviceCompat.Band.TIGHT        ->   2 * 1024 * 1024  //  2 MB — single image
        DeviceCompat.Band.COMFORTABLE  ->   5 * 1024 * 1024  //  5 MB — 2-3 images
        DeviceCompat.Band.ROOMY        ->  10 * 1024 * 1024  // 10 MB — 4-5 images
        DeviceCompat.Band.GENEROUS     ->  20 * 1024 * 1024  // 20 MB — safe floor
    }

    /** Tuned ImageLoader that should be installed as the Coil singleton. */
    fun createImageLoader(band: DeviceCompat.Band): ImageLoader = ImageLoader.Builder(LiteChatApp.instance)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizeBytes(cacheSizeBytes(band))
                .build()
        }
        // Disk cache: Coil defaults to 250MB — fine since it's on-disk, not RAM.
        // On TIGHT we also reduce disk.
        .diskCache {
            val diskSize = when (band) {
                DeviceCompat.Band.TIGHT -> 50 * 1024 * 1024L
                else -> 250 * 1024 * 1024L
            }
            coil3.disk.DiskCache.Builder()
                .directory(LiteChatApp.instance.cacheDir.resolve("coil_disk"))
                .maxSizeBytes(diskSize)
                .build()
        }
        .build()
}