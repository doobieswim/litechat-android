package com.litechat.android

import android.app.Application
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
        // C-001: AdMob SDK init is lazy — triggered only when a banner first
        // shows (i.e. non-Pro users). Pro users pay no ads-init RAM tax at cold start.
    }

    override fun newImageLoader(): coil3.ImageLoader {
        val snap = DeviceCompat.snapshot(this)
        return ImageCacheConfig.createImageLoader(snap.band)
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
