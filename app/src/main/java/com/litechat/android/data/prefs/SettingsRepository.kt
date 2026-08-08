package com.litechat.android.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("litechat_settings")

data class AppSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val model: String = "gpt-4o-mini",
    val temperature: Float = 0.7f,
    val onboardingDone: Boolean = false,
    val isPro: Boolean = false,
)

class SettingsRepository(
    private val context: Context,
    private val secureStore: SecureStore,
) {
    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val MODEL = stringPreferencesKey("model")
        val TEMPERATURE = floatPreferencesKey("temperature")
        val ONBOARDING = booleanPreferencesKey("onboarding_done")
        val IS_PRO = booleanPreferencesKey("is_pro")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            baseUrl = p[Keys.BASE_URL] ?: "https://api.openai.com/v1",
            model = p[Keys.MODEL] ?: "gpt-4o-mini",
            temperature = p[Keys.TEMPERATURE] ?: 0.7f,
            onboardingDone = p[Keys.ONBOARDING] ?: false,
            isPro = p[Keys.IS_PRO] ?: false,
        )
    }

    fun getApiKey(): String = secureStore.getApiKey()

    suspend fun setApiKey(key: String) = secureStore.setApiKey(key)

    suspend fun update(
        baseUrl: String? = null,
        model: String? = null,
        temperature: Float? = null,
        onboardingDone: Boolean? = null,
        isPro: Boolean? = null,
    ) {
        context.dataStore.edit { p ->
            baseUrl?.let { p[Keys.BASE_URL] = it.trim().trimEnd('/') }
            model?.let { p[Keys.MODEL] = it.trim() }
            temperature?.let { p[Keys.TEMPERATURE] = it.coerceIn(0f, 2f) }
            onboardingDone?.let { p[Keys.ONBOARDING] = it }
            isPro?.let { p[Keys.IS_PRO] = it }
        }
    }

    companion object {
        val PRESETS = listOf(
            Preset("OpenAI", "https://api.openai.com/v1", "gpt-4o-mini"),
            Preset("OpenRouter", "https://openrouter.ai/api/v1", "openai/gpt-4o-mini"),
            Preset("Groq", "https://api.groq.com/openai/v1", "llama-3.3-70b-versatile"),
            Preset("Ollama (local)", "http://127.0.0.1:11434/v1", "llama3.2"),
            Preset("Custom", "https://", "your-model-id"),
        )
    }

    data class Preset(val name: String, val baseUrl: String, val model: String)
}
