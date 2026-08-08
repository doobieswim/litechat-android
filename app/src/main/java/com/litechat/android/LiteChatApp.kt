package com.litechat.android

import android.app.Application
import com.litechat.android.data.AppContainer

class LiteChatApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // C-001: AdMob SDK init is lazy — triggered only when a banner first
        // shows (i.e. non-Pro users). Pro users pay no ads-init RAM tax at cold start.
    }
}
