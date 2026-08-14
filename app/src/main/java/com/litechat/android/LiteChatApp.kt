package com.litechat.android

import android.app.Application
import coil3.ImageLoader
import coil3.ImageLoaderFactory
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import com.litechat.android.data.AppContainer
import com.litechat.android.util.DeviceCompat
import com.litechat.android.util.ImageCacheConfig

@OptIn(ExperimentalCoilApi::class)
class LiteChatApp : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = AppContainer(this)

        // Coil 3 singleton wired to band-tuned loader (TIGHT=2MB/RGB_565)
        SingletonImageLoader.setSafe { newImageLoader() }
    }

    override fun newImageLoader(): ImageLoader {
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
        coil3.Coil.imageLoader(this).memoryCache?.trimToSize(0)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_MODERATE) {
            coil3.Coil.imageLoader(this).memoryCache?.trimToSize(0)
        }
    }
}
