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
                ModelOption("llama-3.3-70b-versatile", "Llama 3.3 70B — fast and free"),
                ModelOption("llama-3.1-8b-instant", "Llama 3.1 8B — tiny and fast"),
                ModelOption("gemma2-9b-it", "Gemma 2 9B — small"),
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
                ModelOption("meta-llama/llama-3.3-70b-instruct:free", "Llama 3.3 70B — free"),
                ModelOption("google/gemini-2.0-flash-exp:free", "Gemini 2.0 Flash — free"),
                ModelOption("openai/gpt-4o-mini", "GPT-4o mini — cheap"),
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
                ModelOption("Qwen/Qwen2.5-7B-Instruct", "Qwen 2.5 7B"),
                ModelOption("meta-llama/Llama-3.1-8B-Instruct", "Llama 3.1 8B"),
                ModelOption("mistralai/Mistral-7B-Instruct-v0.3", "Mistral 7B"),
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
                ModelOption("grok-4", "Grok 4"),
                ModelOption("grok-3-mini", "Grok 3 mini — cheaper"),
                ModelOption("grok-3", "Grok 3"),
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
                ModelOption("gpt-4o-mini", "GPT-4o mini — cheap"),
                ModelOption("gpt-4o", "GPT-4o — smarter"),
                ModelOption("o4-mini", "o4-mini — good at thinking"),
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
                ModelOption("deepseek-chat", "DeepSeek Chat"),
                ModelOption("deepseek-reasoner", "DeepSeek Reasoner"),
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
                ModelOption("mistral-large-latest", "Mistral Large"),
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
                ModelOption("qwen2.5", "qwen2.5"),
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
        for (p in PROVIDERS) {
            if (p.id == "custom") continue
            if (clean.startsWith(p.baseUrl.trimEnd('/'))) return p
        }
        return PROVIDERS.first { it.id == "custom" }
    }

    fun byId(id: String): ProviderOption =
        PROVIDERS.firstOrNull { it.id == id } ?: PROVIDERS.first { it.id == "custom" }

    /**
     * Old Gemini ids Google now rejects for new keys (404).
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
            else -> model.trim()
        }
    }
}
