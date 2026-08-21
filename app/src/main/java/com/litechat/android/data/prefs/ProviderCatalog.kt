package com.litechat.android.data.prefs

/**
 * C-033: pick a provider, pick a model, paste a key.
 *
 * The user should never type a base URL or a model id.
 * Custom is the only place those fields stay visible.
 *
 * Pure data — no Android imports.
 */
data class ModelOption(
    val id: String,
    val label: String,
)

data class ProviderOption(
    val id: String,
    val name: String,
    val tagline: String,
    val baseUrl: String,
    val needsKey: Boolean,
    val keyUrl: String?,
    /** True = this provider can charge money. Show a warning before they paste. */
    val paid: Boolean,
    val models: List<ModelOption>,
)

object ProviderCatalog {

    val PROVIDERS: List<ProviderOption> = listOf(
        ProviderOption(
            id = "gemini",
            name = "Google Gemini",
            tagline = "Free key. Good at pictures too.",
            baseUrl = "https://generativelanguage.googleapis.com/v1beta/openai/",
            needsKey = true,
            keyUrl = "https://aistudio.google.com/apikey",
            paid = false,
            models = listOf(
                ModelOption("gemini-3.6-flash", "Gemini 3.6 Flash — fast, free key"),
                ModelOption("gemini-3.7-flash", "Gemini 3.7 Flash — newest"),
                ModelOption("gemini-3.1-pro-preview", "Gemini 3.1 Pro — smarter"),
            ),
        ),
        ProviderOption(
            id = "groq",
            name = "Groq",
            tagline = "Free key. Very fast answers.",
            baseUrl = "https://api.groq.com/openai/v1",
            needsKey = true,
            keyUrl = "https://console.groq.com/keys",
            paid = false,
            models = listOf(
                ModelOption("openai/gpt-oss-20b", "GPT-OSS 20B — tiny and fast"),
                ModelOption("openai/gpt-oss-120b", "GPT-OSS 120B — smarter"),
                ModelOption("groq/compound", "Groq Compound — can cost money"),
            ),
        ),
        ProviderOption(
            id = "openrouter",
            name = "OpenRouter",
            tagline = "One key, many models. Has free ones.",
            baseUrl = "https://openrouter.ai/api/v1",
            needsKey = true,
            keyUrl = "https://openrouter.ai/keys",
            paid = false,
            models = listOf(
                ModelOption("openrouter/free", "Auto — any free model"),
                ModelOption("google/gemma-4-26b-a4b-it:free", "Gemma 4 — free"),
                ModelOption("openai/gpt-oss-20b:free", "GPT-OSS 20B — free"),
            ),
        ),
        ProviderOption(
            id = "huggingface",
            name = "Hugging Face",
            tagline = "Free key for many open models.",
            baseUrl = "https://router.huggingface.co/v1",
            needsKey = true,
            keyUrl = "https://huggingface.co/settings/tokens",
            paid = false,
            models = listOf(
                ModelOption("openai/gpt-oss-20b", "GPT-OSS 20B"),
                ModelOption("Qwen/Qwen3-8B", "Qwen 3 8B"),
                ModelOption("openai/gpt-oss-120b", "GPT-OSS 120B"),
            ),
        ),
        ProviderOption(
            id = "xai",
            name = "Grok (xAI)",
            tagline = "Grok. Paste an xAI API key. This can cost money.",
            baseUrl = "https://api.x.ai/v1",
            needsKey = true,
            keyUrl = "https://console.x.ai",
            paid = true,
            models = listOf(
                ModelOption("grok-4.6", "Grok 4.6 — newest"),
                ModelOption("grok-4.5", "Grok 4.5"),
                ModelOption("grok-3-mini", "Grok 3 mini — cheaper"),
            ),
        ),
        ProviderOption(
            id = "openai",
            name = "OpenAI",
            tagline = "ChatGPT’s company. This can cost money.",
            baseUrl = "https://api.openai.com/v1",
            needsKey = true,
            keyUrl = "https://platform.openai.com/api-keys",
            paid = true,
            models = listOf(
                ModelOption("gpt-5.6-luna", "GPT-5.6 Luna — cheap"),
                ModelOption("gpt-5.6-terra", "GPT-5.6 Terra — mid"),
                ModelOption("gpt-5.6-sol", "GPT-5.6 Sol — smarter"),
            ),
        ),
        ProviderOption(
            id = "deepseek",
            name = "DeepSeek",
            tagline = "Strong and cheap. This can cost money.",
            baseUrl = "https://api.deepseek.com/v1",
            needsKey = true,
            keyUrl = "https://platform.deepseek.com/api_keys",
            paid = true,
            models = listOf(
                ModelOption("deepseek-v4-flash", "DeepSeek V4 Flash — fast"),
                ModelOption("deepseek-v4-pro", "DeepSeek V4 Pro — smarter"),
            ),
        ),
        ProviderOption(
            id = "mistral",
            name = "Mistral",
            tagline = "European models. Some plans cost money.",
            baseUrl = "https://api.mistral.ai/v1",
            needsKey = true,
            keyUrl = "https://console.mistral.ai/api-keys",
            paid = true,
            models = listOf(
                ModelOption("mistral-small-latest", "Mistral Small"),
                ModelOption("mistral-medium-latest", "Mistral Medium"),
                ModelOption("codestral-latest", "Codestral — code"),
            ),
        ),
        ProviderOption(
            id = "ollama",
            name = "Ollama (this phone or PC)",
            tagline = "Your own computer. No key.",
            baseUrl = "http://127.0.0.1:11434/v1",
            needsKey = false,
            keyUrl = null,
            paid = false,
            models = listOf(
                ModelOption("llama3.2", "llama3.2"),
                ModelOption("qwen3", "qwen3"),
                ModelOption("gemma3", "gemma3"),
            ),
        ),
        ProviderOption(
            id = "custom",
            name = "Custom URL",
            tagline = "I will type the address myself.",
            baseUrl = "https://",
            needsKey = true,
            keyUrl = null,
            paid = false,
            models = emptyList(),
        ),
    )

    fun fromBaseUrl(baseUrl: String): ProviderOption {
        val clean = baseUrl.trim().trimEnd('/')
        val low = clean.lowercase()
        when {
            "generativelanguage.googleapis.com" in low -> return byId("gemini")
            "api.groq.com" in low -> return byId("groq")
            "openrouter.ai" in low -> return byId("openrouter")
            "router.huggingface.co" in low -> return byId("huggingface")
            "api.x.ai" in low -> return byId("xai")
            "api.openai.com" in low -> return byId("openai")
            "api.deepseek.com" in low -> return byId("deepseek")
            "api.mistral.ai" in low -> return byId("mistral")
            "11434" in low && ("127.0.0.1" in low || "localhost" in low) -> return byId("ollama")
        }
        for (p in PROVIDERS) {
            if (p.id == "custom") continue
            if (clean.startsWith(p.baseUrl.trimEnd('/'))) return p
        }
        return PROVIDERS.first { it.id == "custom" }
    }

    fun byId(id: String): ProviderOption =
        PROVIDERS.firstOrNull { it.id == id } ?: PROVIDERS.first { it.id == "custom" }

    /**
     * Old model ids that hosts now reject or retired.
     * Map them so a saved pick still talks.
     */
    fun resolveModel(model: String): String {
        val id = model.trim().removePrefix("models/")
        return when (id) {
            "gemini-2.0-flash",
            "gemini-2.0-flash-001",
            "gemini-2.5-flash",
            "gemini-2.5-flash-lite" -> "gemini-3.6-flash"
            "gemini-2.5-pro" -> "gemini-3.1-pro-preview"
            "gemma2-9b-it",
            "llama-3.1-8b-instant" -> "openai/gpt-oss-20b"
            "llama-3.3-70b-versatile" -> "openai/gpt-oss-120b"
            "meta-llama/llama-3.3-70b-instruct:free",
            "google/gemini-2.0-flash-exp:free",
            "openai/gpt-4o-mini" -> "openrouter/free"
            "Qwen/Qwen2.5-7B-Instruct",
            "meta-llama/Llama-3.1-8B-Instruct",
            "mistralai/Mistral-7B-Instruct-v0.3" -> "openai/gpt-oss-20b"
            "grok-4",
            "grok-3" -> "grok-4.6"
            "gpt-4o-mini",
            "gpt-4o",
            "o4-mini" -> "gpt-5.6-luna"
            "deepseek-chat" -> "deepseek-v4-flash"
            "deepseek-reasoner" -> "deepseek-v4-pro"
            "mistral-large-latest" -> "mistral-medium-latest"
            "qwen2.5" -> "qwen3"
            else -> model.trim()
        }
    }

    /** Picture model for /imagine. Null = this host cannot make pictures. */
    fun resolveImageModel(baseUrl: String): String? {
        return when (fromBaseUrl(baseUrl).id) {
            "gemini" -> "gemini-3.1-flash-image"
            "xai" -> "grok-imagine-image-2.0"
            "openai", "custom" -> "gpt-image-2"
            "openrouter" -> "openai/gpt-image-2"
            else -> null
        }
    }

    /** Extra picture ids to try if the first 404s. */
    fun imageModelFallbacks(baseUrl: String): List<String> {
        return when (fromBaseUrl(baseUrl).id) {
            "gemini" -> listOf(
                "gemini-3.1-flash-image-preview",
                "gemini-3.1-flash-lite-image",
                "gemini-2.5-flash-image",
                "gemini-3-pro-image",
            )
            "openrouter" -> listOf(
                "google/gemini-3.1-flash-image",
                "google/gemini-2.5-flash-image",
            )
            else -> emptyList()
        }
    }

    fun imageUsesNativeGenerate(baseUrl: String): Boolean =
        fromBaseUrl(baseUrl).id == "gemini"

    fun imageUsesOpenRouter(baseUrl: String): Boolean =
        fromBaseUrl(baseUrl).id == "openrouter"

    fun cannotMakePicturesLine(baseUrl: String): String =
        "${fromBaseUrl(baseUrl).name} cannot make pictures. Switch to Google Gemini."

    /** Video model for /video. Null = this host cannot make videos this way. */
    fun resolveVideoModel(baseUrl: String, nowMs: Long = System.currentTimeMillis()): String? {
        return when (fromBaseUrl(baseUrl).id) {
            "gemini" -> "veo-3.1-generate-preview"
            "xai" -> "grok-imagine-video-1.5"
            "openai", "custom" -> if (soraStillUp(nowMs)) "sora-2" else null
            else -> null
        }
    }

    /** OpenAI Videos API / sora-2 shut 2026-09-24 00:00 UTC. */
    const val SORA_SUNSET_MS = 1_790_208_000_000L

    fun soraStillUp(nowMs: Long = System.currentTimeMillis()): Boolean = nowMs < SORA_SUNSET_MS

    fun cannotMakeVideosLine(baseUrl: String): String =
        "${fromBaseUrl(baseUrl).name} cannot make videos. Switch to Google Gemini."

    /** /edit door. Null = this host has no image-edits API we speak. */
    fun resolveEditModel(baseUrl: String): String? {
        return when (fromBaseUrl(baseUrl).id) {
            "openai", "custom" -> "gpt-image-2"
            "xai" -> "grok-imagine-image-2.0"
            else -> null
        }
    }

    fun cannotEditPicturesLine(baseUrl: String): String =
        "${fromBaseUrl(baseUrl).name} cannot edit pictures. Switch to OpenAI or Grok."

    fun resolveSttModel(baseUrl: String): String? {
        return when (fromBaseUrl(baseUrl).id) {
            "openai", "custom" -> "whisper-1"
            "groq" -> "whisper-large-v3"
            else -> null
        }
    }

    fun resolveTtsModel(baseUrl: String): String? {
        return when (fromBaseUrl(baseUrl).id) {
            "openai", "custom" -> "tts-1"
            else -> null
        }
    }

    fun videoUsesNativeVeo(baseUrl: String): Boolean =
        fromBaseUrl(baseUrl).id == "gemini"

    fun videoUsesXaiImagine(baseUrl: String): Boolean =
        fromBaseUrl(baseUrl).id == "xai"
}
