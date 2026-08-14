package com.litechat.android.util

import coil3.ImageLoader
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.size.Size
import android.graphics.Bitmap
import okio.Path.Companion.toOkioPath

/**
 * Per-band Coil image cache tuning for 4GB-first devices.
 *
 * Coil's default memory cache is 20% of app heap (~50MB on a 256MB heap).
 * On TIGHT/COMFORTABLE devices that competes with foreground processes.
 * We clamp it to band-appropriate values and use RGB_565 on weak devices.
 */
object ImageCacheConfig {

    fun cacheSizeBytes(band: DeviceCompat.Band): Long = when (band) {
        DeviceCompat.Band.TIGHT        ->   2 * 1024 * 1024  //  2 MB — single image
        DeviceCompat.Band.COMFORTABLE  ->   5 * 1024 * 1024  //  5 MB — 2-3 images
        DeviceCompat.Band.ROOMY        ->  10 * 1024 * 1024  // 10 MB — 4-5 images
        DeviceCompat.Band.GENEROUS     ->  20 * 1024 * 1024  // 20 MB — safe floor
    }

    /** RGB_565 uses 2 bytes/pixel vs ARGB_8888's 4 — halves bitmap memory. */
    fun bitmapConfig(band: DeviceCompat.Band): Bitmap.Config = when (band) {
        DeviceCompat.Band.TIGHT, DeviceCompat.Band.COMFORTABLE ->
            Bitmap.Config.RGB_565
        else -> Bitmap.Config.ARGB_8888
    }

    /** Display-optimal decode size: never decode larger than the screen needs. */
    fun displaySize(band: DeviceCompat.Band): Size = when (band) {
        DeviceCompat.Band.TIGHT        -> Size(360, 360)    // small phone
        DeviceCompat.Band.COMFORTABLE  -> Size(480, 480)    // typical 720p
        else                           -> Size(720, 720)    // 1080p+
    }

    /** Downscale generated images to this max dimension before saving. */
    fun maxSaveDimension(band: DeviceCompat.Band): Int = when (band) {
        DeviceCompat.Band.TIGHT        -> 512
        DeviceCompat.Band.COMFORTABLE  -> 768
        else                           -> 1024
    }

    /** Tuned ImageLoader that should be installed as the Coil singleton. */
    fun createImageLoader(context: android.content.Context, band: DeviceCompat.Band): ImageLoader = ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizeBytes(cacheSizeBytes(band))
                    .build()
            }
            .diskCache {
                val diskSize = when (band) {
                    DeviceCompat.Band.TIGHT -> 50 * 1024 * 1024L
                    else -> 250 * 1024 * 1024L
                }
                coil3.disk.DiskCache.Builder()
                    .directory(context.cacheDir.resolve("coil_disk").toOkioPath())
                    .maxSizeBytes(diskSize)
                    .build()
            }
            .build()
    }