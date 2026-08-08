package com.litechat.android.data

import android.content.Context
import androidx.room.Room
import com.litechat.android.data.api.OpenAiCompatibleClient
import com.litechat.android.data.billing.BillingRepository
import com.litechat.android.data.db.AppDatabase
import com.litechat.android.data.db.ChatRepository
import com.litechat.android.data.prefs.SecureStore
import com.litechat.android.data.prefs.SettingsRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "litechat.db"
    ).fallbackToDestructiveMigration().build()

    val secureStore = SecureStore(appContext)
    val settingsRepository = SettingsRepository(appContext, secureStore)
    val chatRepository = ChatRepository(database.conversationDao(), database.messageDao())
    val openAiClient = OpenAiCompatibleClient()
    val billingRepository = BillingRepository(appContext)
}
