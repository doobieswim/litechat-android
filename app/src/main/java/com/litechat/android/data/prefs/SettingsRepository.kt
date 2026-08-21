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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("litechat_settings")

data class AppSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val model: String = "gpt-5.6-luna",
    val temperature: Float = 0.7f,
    val onboardingDone: Boolean = false,
    val isPro: Boolean = false,
    /** C-032: user accepted the in-app acceptable-use terms (one-time). */
    val acceptableUseAccepted: Boolean = false,
    /** P-012: model reply language ("" = default). FREE — never gated (H-009). */
    val language: String = "",
    /** P-004: when Pro was first turned on (0 = unknown). */
    val proSinceMillis: Long = 0L,
    /** P-010: selected persona pack id ("" = none). */
    val activePersonaId: String = "",
    /** P-013: extra model knobs. 0 / 1 / false = leave them off the request. */
    val topP: Float = 1f,
    val presencePenalty: Float = 0f,
    val frequencyPenalty: Float = 0f,
    val maxTokens: Int = 0,
    val promptCache: Boolean = false,
    /** P-003: user already dismissed the one backup reminder. */
    val backupReminderDone: Boolean = false,
    /** P-001: voice daily-limit bookkeeping (free = 1/day). */
    val voiceDay: String = "",
    val voiceUsedToday: Int = 0,
    /** Free /imagine test via Pollinations (no key, labeled "Free test"). */
    val freeTestImages: Boolean = true,
)

/** C-012: prompt template with dynamic [Variable] fields. */
data class PromptTemplate(
    val id: String,
    val name: String,
    val template: String,
    val variables: Map<String, String> = emptyMap(),
) {
    /** Render template by replacing [Var] with values. */
    fun render(vars: Map<String, String> = variables): String {
        var result = template
        for ((key, value) in vars) {
            result = result.replace("[$key]", value)
        }
        return result
    }
}

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
        val ACCEPTABLE_USE = booleanPreferencesKey("acceptable_use_accepted")
        val NON_STREAM_EXPIRY = stringPreferencesKey("non_stream_expiry_map")
        val LANGUAGE = stringPreferencesKey("language")
        val DRAFTS = stringPreferencesKey("drafts_json")
        val PRO_SINCE = stringPreferencesKey("pro_since_millis")
        val PERSONA = stringPreferencesKey("active_persona_id")
        val TOP_P = floatPreferencesKey("top_p")
        val PRESENCE = floatPreferencesKey("presence_penalty")
        val FREQUENCY = floatPreferencesKey("frequency_penalty")
        val MAX_TOKENS = stringPreferencesKey("max_tokens")
        val PROMPT_CACHE = booleanPreferencesKey("prompt_cache")
        val BACKUP_REMINDER = booleanPreferencesKey("backup_reminder_done")
        val VOICE_DAY = stringPreferencesKey("voice_day")
        val VOICE_USED = stringPreferencesKey("voice_used_today")
        val FOLDERS = stringPreferencesKey("folders_json")
        val FREE_TEST_IMAGES = booleanPreferencesKey("free_test_images")
    }

    /**
     * C-004: baseUrl → epoch millis when its "prefer non-stream" flag expires.
     * TTL-based by design: a provider that fixes streaming recovers
     * automatically after [STREAM_BROKEN_TTL_MS]. Only baseUrls are stored —
     * never keys, tokens, or message content.
     */
    suspend fun markStreamBroken(
        baseUrl: String,
        ttlMs: Long = STREAM_BROKEN_TTL_MS,
    ) {
        context.dataStore.edit { p ->
            val map = decodeExpiryMap(p[Keys.NON_STREAM_EXPIRY])
            map[baseUrl] = System.currentTimeMillis() + ttlMs
            p[Keys.NON_STREAM_EXPIRY] = encodeExpiryMap(map)
        }
    }

    /** True while the baseUrl's non-stream flag is unexpired. */
    suspend fun isStreamBrokenNow(baseUrl: String): Boolean {
        val expiry = decodeExpiryMap(
            context.dataStore.data.first()[Keys.NON_STREAM_EXPIRY]
        )[baseUrl] ?: return false
        return expiry > System.currentTimeMillis()
    }

    /** Drop a flag early (e.g. user changed provider and wants fresh streams). */
    suspend fun clearStreamBroken(baseUrl: String) {
        context.dataStore.edit { p ->
            val map = decodeExpiryMap(p[Keys.NON_STREAM_EXPIRY])
            if (map.remove(baseUrl) != null) {
                p[Keys.NON_STREAM_EXPIRY] = encodeExpiryMap(map)
            }
        }
    }

    private fun decodeExpiryMap(raw: String?): MutableMap<String, Long> {
        if (raw.isNullOrBlank()) return mutableMapOf()
        return try {
            Json.parseToJsonElement(raw).jsonObject
                .mapValues { (_, v) -> v.toString().trim('"').toLong() }
                .toMutableMap()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    private fun encodeExpiryMap(map: Map<String, Long>): String =
        buildJsonObject {
            map.forEach { (k, v) -> put(k, v) }
        }.toString()

    // ── C-012: Prompt template storage ──────────────────────────────

    private val templateKey = stringPreferencesKey("prompt_templates_json")

    /** Observe all saved templates (reactive). */
    val templates: Flow<List<PromptTemplate>> = context.dataStore.data.map { p ->
        decodeTemplates(p[templateKey])
    }

    /** Read templates once (non-reactive, for send-time). */
    suspend fun getTemplates(): List<PromptTemplate> =
        decodeTemplates(context.dataStore.data.first()[templateKey])

    /** Save a template (upsert by id). */
    suspend fun saveTemplate(template: PromptTemplate) {
        context.dataStore.edit { p ->
            val list = decodeTemplates(p[templateKey]).toMutableList()
            val idx = list.indexOfFirst { it.id == template.id }
            if (idx >= 0) list[idx] = template else list.add(template)
            p[templateKey] = encodeTemplates(list)
        }
    }

    /** Delete a template by id. */
    suspend fun deleteTemplate(id: String) {
        context.dataStore.edit { p ->
            val list = decodeTemplates(p[templateKey]).filter { it.id != id }
            p[templateKey] = encodeTemplates(list)
        }
    }

    private fun decodeTemplates(raw: String?): List<PromptTemplate> {
        if (raw.isNullOrBlank()) return BUILT_IN_TEMPLATES.toList()
        return try {
            val arr = Json.parseToJsonElement(raw).jsonArray
            arr.map { el ->
                val obj = el.jsonObject
                PromptTemplate(
                    id = obj["id"]!!.jsonPrimitive.content,
                    name = obj["name"]!!.jsonPrimitive.content,
                    template = obj["template"]!!.jsonPrimitive.content,
                    variables = obj["variables"]?.jsonObject?.mapValues {
                        it.value.jsonPrimitive.content
                    } ?: emptyMap(),
                )
            }
        } catch (_: Exception) {
            BUILT_IN_TEMPLATES.toList()
        }
    }

    private fun encodeTemplates(list: List<PromptTemplate>): String =
        kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(TemplateDto.serializer()),
            list.map { TemplateDto(it.id, it.name, it.template, it.variables) }
        )

    @kotlinx.serialization.Serializable
    private data class TemplateDto(
        val id: String,
        val name: String,
        val template: String,
        val variables: Map<String, String>,
    )

    // ── C-017: Provider failover list ─────────────────────────────

    private val providerListKey = stringPreferencesKey("provider_list_json")

    /** Extra providers for failover (does not include primary). */
    data class ProviderEntry(val baseUrl: String, val apiKey: String = "", val model: String = "")

    suspend fun getProviderList(): List<ProviderEntry> {
        val raw = context.dataStore.data.first()[providerListKey] ?: return emptyList()
        return try {
            val arr = Json.parseToJsonElement(raw).jsonArray
            arr.map { el ->
                val obj = el.jsonObject
                val id = obj["baseUrl"]!!.jsonPrimitive.content
                ProviderEntry(
                    baseUrl = id,
                    // C-017 (REVIEW B1): the key is stored encrypted in SecureStore,
                    // never in the unencrypted DataStore JSON. Decode it at read time.
                    apiKey = secureStore.getProviderKey(id),
                    model = obj["model"]?.jsonPrimitive?.content ?: "",
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun saveProviderList(list: List<ProviderEntry>) {
        // B1: keys go to SecureStore; DataStore JSON stores only an id (baseUrl).
        list.forEach { p ->
            if (p.apiKey.isNotBlank()) {
                secureStore.setProviderKey(p.baseUrl, p.apiKey)
            } else {
                secureStore.removeProviderKey(p.baseUrl)
            }
        }
        val encoded = kotlinx.serialization.json.Json.encodeToString(
            kotlinx.serialization.builtins.ListSerializer(ProviderEntryDto.serializer()),
            list.map { ProviderEntryDto(it.baseUrl, it.model) }
        )
        context.dataStore.edit { p -> p[providerListKey] = encoded }
    }

    @kotlinx.serialization.Serializable
    private data class ProviderEntryDto(val baseUrl: String, val model: String)

    val settings: Flow<AppSettings> = context.dataStore.data.map { p ->
        AppSettings(
            baseUrl = p[Keys.BASE_URL] ?: "https://api.openai.com/v1",
            model = p[Keys.MODEL] ?: "gpt-5.6-luna",
            temperature = p[Keys.TEMPERATURE] ?: 0.7f,
            onboardingDone = p[Keys.ONBOARDING] ?: false,
            isPro = p[Keys.IS_PRO] ?: false,
            acceptableUseAccepted = p[Keys.ACCEPTABLE_USE] ?: false,
            language = p[Keys.LANGUAGE] ?: "",
            proSinceMillis = p[Keys.PRO_SINCE]?.toLongOrNull() ?: 0L,
            activePersonaId = p[Keys.PERSONA] ?: "",
            topP = p[Keys.TOP_P] ?: 1f,
            presencePenalty = p[Keys.PRESENCE] ?: 0f,
            frequencyPenalty = p[Keys.FREQUENCY] ?: 0f,
            maxTokens = p[Keys.MAX_TOKENS]?.toIntOrNull() ?: 0,
            promptCache = p[Keys.PROMPT_CACHE] ?: false,
            backupReminderDone = p[Keys.BACKUP_REMINDER] ?: false,
            voiceDay = p[Keys.VOICE_DAY] ?: "",
            voiceUsedToday = p[Keys.VOICE_USED]?.toIntOrNull() ?: 0,
            freeTestImages = p[Keys.FREE_TEST_IMAGES] ?: true,
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
        acceptableUseAccepted: Boolean? = null,
        language: String? = null,
        proSinceMillis: Long? = null,
        activePersonaId: String? = null,
        topP: Float? = null,
        presencePenalty: Float? = null,
        frequencyPenalty: Float? = null,
        maxTokens: Int? = null,
        promptCache: Boolean? = null,
        backupReminderDone: Boolean? = null,
        voiceDay: String? = null,
        voiceUsedToday: Int? = null,
        freeTestImages: Boolean? = null,
    ) {
        context.dataStore.edit { p ->
            baseUrl?.let { p[Keys.BASE_URL] = it.trim().trimEnd('/') }
            model?.let { p[Keys.MODEL] = it.trim() }
            temperature?.let { p[Keys.TEMPERATURE] = it.coerceIn(0f, 2f) }
            onboardingDone?.let { p[Keys.ONBOARDING] = it }
            isPro?.let {
                p[Keys.IS_PRO] = it
                // P-004: stamp the first Pro day once.
                if (it && (p[Keys.PRO_SINCE].isNullOrBlank() || p[Keys.PRO_SINCE] == "0")) {
                    p[Keys.PRO_SINCE] = System.currentTimeMillis().toString()
                }
            }
            acceptableUseAccepted?.let { p[Keys.ACCEPTABLE_USE] = it }
            language?.let { p[Keys.LANGUAGE] = it.trim() }
            proSinceMillis?.let { p[Keys.PRO_SINCE] = it.toString() }
            activePersonaId?.let { p[Keys.PERSONA] = it }
            topP?.let { p[Keys.TOP_P] = it.coerceIn(0f, 1f) }
            presencePenalty?.let { p[Keys.PRESENCE] = it.coerceIn(-2f, 2f) }
            frequencyPenalty?.let { p[Keys.FREQUENCY] = it.coerceIn(-2f, 2f) }
            maxTokens?.let { p[Keys.MAX_TOKENS] = it.coerceAtLeast(0).toString() }
            promptCache?.let { p[Keys.PROMPT_CACHE] = it }
            backupReminderDone?.let { p[Keys.BACKUP_REMINDER] = it }
            voiceDay?.let { p[Keys.VOICE_DAY] = it }
            voiceUsedToday?.let { p[Keys.VOICE_USED] = it.toString() }
            freeTestImages?.let { p[Keys.FREE_TEST_IMAGES] = it }
        }
    }

    // ── P-014: per-conversation drafts (unsent input survives switching) ──

    suspend fun getDraft(conversationId: String): String {
        val raw = context.dataStore.data.first()[Keys.DRAFTS]
        return decodeDrafts(raw)[conversationId] ?: ""
    }

    suspend fun saveDraft(conversationId: String, text: String) {
        context.dataStore.edit { p ->
            val drafts = decodeDrafts(p[Keys.DRAFTS]).toMutableMap()
            if (text.isBlank()) drafts.remove(conversationId)
            else drafts[conversationId] = text
            p[Keys.DRAFTS] = encodeDrafts(drafts)
        }
    }

    suspend fun clearDraft(conversationId: String) = saveDraft(conversationId, "")

    // ── P-009: folders (DataStore JSON) ────────────────────────────

    val folders: Flow<List<ChatFolder>> = context.dataStore.data.map { p ->
        decodeFolders(p[Keys.FOLDERS])
    }

    suspend fun saveFolder(folder: ChatFolder) {
        context.dataStore.edit { p ->
            val list = decodeFolders(p[Keys.FOLDERS]).toMutableList()
            val idx = list.indexOfFirst { it.id == folder.id }
            if (idx >= 0) list[idx] = folder else list.add(folder)
            p[Keys.FOLDERS] = encodeFolders(list)
        }
    }

    suspend fun deleteFolder(id: String) {
        context.dataStore.edit { p ->
            p[Keys.FOLDERS] = encodeFolders(decodeFolders(p[Keys.FOLDERS]).filter { it.id != id })
        }
    }

    // ── P-010: templates only (no secrets) ─────────────────────────

    suspend fun exportTemplatesJson(): String {
        val list = getTemplates()
        return encodeTemplates(list)
    }

    suspend fun importTemplatesJson(json: String): String? {
        return try {
            val list = decodeTemplatesStrict(json)
            if (list.isEmpty()) return "No templates in file"
            context.dataStore.edit { p -> p[templateKey] = encodeTemplates(list) }
            null
        } catch (_: Exception) {
            "Templates file is not valid"
        }
    }

    // ── C-022: Settings export/import (JSON — never secrets) ──────────

    @kotlinx.serialization.Serializable
    private data class SettingsExportDto(
        val version: Int = 1,
        val baseUrl: String,
        val model: String,
        val temperature: Float,
        val templates: List<TemplateDto> = emptyList(),
    )

    /**
     * C-022: serialize non-secret settings for export. API keys stay on the
     * device (SecureStore), Pro state is earned via billing, and compliance
     * flags are device-local — none of them cross the JSON boundary.
     */
    suspend fun exportSettingsJson(): String {
        val s = context.dataStore.data.first()
        val templates = decodeTemplates(s[templateKey])
        val dto = SettingsExportDto(
            baseUrl = s[Keys.BASE_URL] ?: "https://api.openai.com/v1",
            model = s[Keys.MODEL] ?: "gpt-5.6-luna",
            temperature = s[Keys.TEMPERATURE] ?: 0.7f,
            templates = templates.map { TemplateDto(it.id, it.name, it.template, it.variables) },
        )
        return kotlinx.serialization.json.Json.encodeToString(
            SettingsExportDto.serializer(), dto
        )
    }

    /** C-022: apply an exported settings JSON. Returns an error message, or null on success. */
    suspend fun importSettingsJson(json: String): String? {
        val dto = try {
            kotlinx.serialization.json.Json.decodeFromString(
                SettingsExportDto.serializer(), json
            )
        } catch (_: Exception) {
            return "Settings file is not valid"
        }
        if (dto.version != 1) return "Unsupported settings version ${dto.version}"
        context.dataStore.edit { p ->
            if (dto.baseUrl.isNotBlank()) p[Keys.BASE_URL] = dto.baseUrl.trim().trimEnd('/')
            if (dto.model.isNotBlank()) p[Keys.MODEL] = dto.model
            p[Keys.TEMPERATURE] = dto.temperature.coerceIn(0f, 2f)
            p[templateKey] = encodeTemplates(
                dto.templates.map { PromptTemplate(it.id, it.name, it.template, it.variables) }
            )
        }
        return null
    }

    companion object {
        /** C-004: how long a baseUrl stays flagged "prefer non-stream" (24h). */
        const val STREAM_BROKEN_TTL_MS = 24L * 60 * 60 * 1000
        val PRESETS = listOf(
            Preset("OpenAI", "https://api.openai.com/v1", "gpt-5.6-luna"),
            Preset("OpenRouter", "https://openrouter.ai/api/v1", "openrouter/free"),
            Preset("Groq", "https://api.groq.com/openai/v1", "openai/gpt-oss-20b"),
            Preset("Ollama (local)", "http://127.0.0.1:11434/v1", "llama3.2"),
            Preset("Custom", "https://", "your-model-id"),
        )

        /** C-012: one built-in template available to all users (free tier limit = 1). */
        val BUILT_IN_TEMPLATES = listOf(
            PromptTemplate(
                id = "builtin_translate",
                name = "Translate",
                template = "Translate the following text into [Language]:\n\n[Text]",
                variables = mapOf("Language" to "Spanish", "Text" to ""),
            ),
        )

        /** Free-tier template limit. Pro users have no limit. */
        const val FREE_TEMPLATE_LIMIT = 1

        /** P-012: reply-language choices (Research C market languages + a few). */
        val LANGUAGES = listOf(
            "", "English", "Spanish", "Portuguese", "Indonesian", "Hindi",
            "French", "Arabic", "Russian", "German", "Japanese", "Vietnamese",
        )

        /**
         * P-014: per-conversation draft map encode/decode (pure — unit-tested
         * without a DataStore, NamedKeyStoreLogicTest pattern).
         */
        internal fun encodeDrafts(map: Map<String, String>): String =
            Json.encodeToString(MapSerializer(String.serializer(), String.serializer()), map)

        internal fun decodeDrafts(raw: String?): Map<String, String> {
            if (raw.isNullOrBlank()) return emptyMap()
            return runCatching {
                Json.decodeFromString(
                    MapSerializer(String.serializer(), String.serializer()), raw
                )
            }.getOrDefault(emptyMap())
        }

        internal fun encodeFolders(list: List<ChatFolder>): String =
            Json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(ChatFolder.serializer()),
                list,
            )

        internal fun decodeFolders(raw: String?): List<ChatFolder> {
            if (raw.isNullOrBlank()) return emptyList()
            return try {
                Json.decodeFromString(
                    kotlinx.serialization.builtins.ListSerializer(ChatFolder.serializer()),
                    raw,
                )
            } catch (_: Exception) {
                emptyList()
            }
        }

        /** Import-only: never fall back to builtins (that would wipe user templates). */
        internal fun decodeTemplatesStrict(raw: String): List<PromptTemplate> {
            val arr = Json.parseToJsonElement(raw).jsonArray
            return arr.map { el ->
                val obj = el.jsonObject
                PromptTemplate(
                    id = obj["id"]!!.jsonPrimitive.content,
                    name = obj["name"]!!.jsonPrimitive.content,
                    template = obj["template"]!!.jsonPrimitive.content,
                    variables = obj["variables"]?.jsonObject?.mapValues {
                        it.value.jsonPrimitive.content
                    } ?: emptyMap(),
                )
            }
        }
    }

    data class Preset(val name: String, val baseUrl: String, val model: String)
}
