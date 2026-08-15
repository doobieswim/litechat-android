package com.litechat.android.data

import android.content.Context
import androidx.room.Room
import com.litechat.android.data.api.OpenAiCompatibleClient
import com.litechat.android.data.api.RetryInterceptor
import com.litechat.android.data.billing.BillingRepository
import com.litechat.android.data.connectivity.ConnectivityObserver
import com.litechat.android.data.db.AppDatabase
import com.litechat.android.data.db.ChatRepository
import com.litechat.android.data.context.MemoryManager
import com.litechat.android.data.prefs.NamedKeyStore
import com.litechat.android.data.prefs.SecureStore
import com.litechat.android.data.prefs.SettingsRepository
import com.litechat.android.core.flags.FeatureFlags

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    /** Public context for file I/O (e.g. saving generated images to cache). */
    val ctx: Context = appContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "litechat.db"
    ).fallbackToDestructiveMigration().build()

    val secureStore = SecureStore(appContext)
    /** C-020: persistent user memory (hit-count promotion, Pro-gated). */
    val memoryManager = MemoryManager(appContext)
    /** C-023: encrypted named API keys per provider (Agora pattern). */
    val namedKeyStore = NamedKeyStore(appContext)
    val settingsRepository = SettingsRepository(appContext, secureStore)
    val chatRepository = ChatRepository(database.conversationDao(), database.messageDao())
    val openAiClient = OpenAiCompatibleClient(
        client = OpenAiCompatibleClient.defaultClient(RetryInterceptor()),
    )
    val billingRepository = BillingRepository(appContext)
    val connectivityObserver = ConnectivityObserver(appContext)

    /** One place to check Pro for gating (Imp#5). */
    fun isPro(): Boolean = FeatureFlags.isPro
}