package com.litechat.android

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import com.litechat.android.data.AppContainer
import com.litechat.android.util.DeviceCompat
import com.litechat.android.util.ImageCacheConfig

@OptIn(ExperimentalCoilApi::class)
class LiteChatApp : Application(), SingletonImageLoader.Factory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)

        // Coil 3 singleton wired to band-tuned loader (TIGHT=2MB/RGB_565)
        SingletonImageLoader.setSafe { context -> newImageLoader(context) }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val snap = DeviceCompat.snapshot(this)
        return ImageCacheConfig.createImageLoader(this, snap.band)
    }

    companion object {
        lateinit var instance: LiteChatApp
            private set

        init {
            // Will be set in onCreate() via Application superclass init order.
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Coil's memory cache automatically reacts to onLowMemory via
        // MemoryCache.trimMemory, but we also signal explicitly.
        SingletonImageLoader.get(this).memoryCache?.trimToSize(0)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            SingletonImageLoader.get(this).memoryCache?.trimToSize(0)
        }
    }
}
