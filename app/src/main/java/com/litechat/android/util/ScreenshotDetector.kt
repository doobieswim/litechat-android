package com.litechat.android.util

import android.content.Context
import android.os.Environment
import java.io.File

/**
 * Detection: checks if a new screenshot was taken recently
 * and offers to analyze it in chat.
 */
object ScreenshotDetector {

    /** Check DCIM/Screenshots for images taken in the last 30 seconds. */
    fun getRecentScreenshot(context: Context): String? {
        val dirs = listOf(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        )
        val cutoff = System.currentTimeMillis() - 30_000
        for (dir in dirs) {
            dir.walkTopDown().maxDepth(2).forEach { file ->
                if (file.isFile && file.extension.lowercase() in listOf("png", "jpg", "jpeg")) {
                    if (file.lastModified() > cutoff) return file.absolutePath
                }
            }
        }
        return null
    }
}