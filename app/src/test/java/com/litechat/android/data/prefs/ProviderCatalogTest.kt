package com.litechat.android.data.prefs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderCatalogTest {

    @Test
    fun `catalog includes the popular pick-and-paste providers`() {
        val ids = ProviderCatalog.PROVIDERS.map { it.id }.toSet()
        for (need in listOf(
            "gemini", "groq", "openrouter", "openai",
            "xai", "deepseek", "huggingface", "mistral",
            "ollama", "custom",
        )) {
            assertTrue("missing $need", ids.contains(need))
        }
    }

    @Test
    fun `known base URLs map back to the right provider`() {
        assertEquals("xai", ProviderCatalog.fromBaseUrl("https://api.x.ai/v1").id)
        assertEquals("openrouter", ProviderCatalog.fromBaseUrl("https://openrouter.ai/api/v1/").id)
        assertEquals("deepseek", ProviderCatalog.fromBaseUrl("https://api.deepseek.com/v1").id)
        assertEquals("huggingface", ProviderCatalog.fromBaseUrl("https://router.huggingface.co/v1").id)
        assertEquals("custom", ProviderCatalog.fromBaseUrl("https://mystery.example/v1").id)
    }

    @Test
    fun `every named provider has at least one model so the user never types an id`() {
        for (p in ProviderCatalog.PROVIDERS) {
            if (p.id == "custom") {
                assertTrue(p.models.isEmpty())
            } else {
                assertTrue("${p.id} has no models", p.models.isNotEmpty())
                assertTrue(p.models.first().id.isNotBlank())
            }
        }
    }

    @Test
    fun `xai is pick-and-paste and warns that it can cost money`() {
        val xai = ProviderCatalog.PROVIDERS.first { it.id == "xai" }
        assertEquals("https://api.x.ai/v1", xai.baseUrl.trimEnd('/'))
        assertTrue(xai.needsKey)
        assertTrue(xai.paid)
        assertNotNull(xai.keyUrl)
        assertTrue(xai.models.any { it.id.contains("grok") })
        assertTrue(xai.tagline.contains("cost", ignoreCase = true) || xai.tagline.contains("paid", ignoreCase = true))
    }

    @Test
    fun `gemini picker lists current 3-series models not shut 2-series`() {
        val gemini = ProviderCatalog.PROVIDERS.first { it.id == "gemini" }
        val ids = gemini.models.map { it.id }
        assertEquals(
            listOf("gemini-3.6-flash", "gemini-3.7-flash", "gemini-3.1-pro-preview"),
            ids,
        )
        assertFalse(ids.any { it.startsWith("gemini-2.") })
    }

    @Test
    fun `old gemini ids remap so a saved pick still works`() {
        assertEquals("gemini-3.6-flash", ProviderCatalog.resolveModel("gemini-2.0-flash"))
        assertEquals("gemini-3.6-flash", ProviderCatalog.resolveModel("gemini-2.5-flash"))
        assertEquals("gemini-3.1-pro-preview", ProviderCatalog.resolveModel("gemini-2.5-pro"))
        assertEquals("gemini-3.1-pro-preview", ProviderCatalog.resolveModel("models/gemini-2.5-pro"))
        assertEquals("gpt-5.6-luna", ProviderCatalog.resolveModel("gpt-4o-mini"))
        assertEquals("openrouter/free", ProviderCatalog.resolveModel("openai/gpt-4o-mini"))
        assertEquals("deepseek-v4-flash", ProviderCatalog.resolveModel("deepseek-chat"))
        assertEquals("openai/gpt-oss-20b", ProviderCatalog.resolveModel("gemma2-9b-it"))
        assertEquals("gemini-3.6-flash", ProviderCatalog.resolveModel("gemini-3.6-flash"))
        assertEquals("custom-keep", ProviderCatalog.resolveModel("custom-keep"))
    }

    @Test
    fun `imagine and video pick the host picture or video name not openai only`() {
        val gemini = "https://generativelanguage.googleapis.com/v1beta/openai/"
        val groq = "https://api.groq.com/openai/v1"
        val openai = "https://api.openai.com/v1"
        val xai = "https://api.x.ai/v1"
        assertEquals("gemini-3.1-flash-image", ProviderCatalog.resolveImageModel(gemini))
        assertEquals("gpt-image-2", ProviderCatalog.resolveImageModel(openai))
        assertEquals("grok-imagine-image-2.0", ProviderCatalog.resolveImageModel(xai))
        assertEquals(null, ProviderCatalog.resolveImageModel(groq))
        assertEquals("veo-3.1-generate-preview", ProviderCatalog.resolveVideoModel(gemini))
        assertEquals("sora-2", ProviderCatalog.resolveVideoModel(openai))
        assertEquals("grok-imagine-video-1.5", ProviderCatalog.resolveVideoModel(xai))
        assertEquals(null, ProviderCatalog.resolveVideoModel(groq))
        assertTrue(ProviderCatalog.imageModelFallbacks(gemini).contains("gemini-2.5-flash-image"))
        assertTrue(ProviderCatalog.imageUsesNativeGenerate(gemini))
        assertFalse(ProviderCatalog.imageUsesNativeGenerate(openai))
        assertTrue(ProviderCatalog.videoUsesNativeVeo(gemini))
        assertFalse(ProviderCatalog.videoUsesNativeVeo(openai))
        assertTrue(ProviderCatalog.videoUsesXaiImagine(xai))
        assertFalse(ProviderCatalog.videoUsesXaiImagine(gemini))
    }

    @Test
    fun `free-key providers stay marked not paid`() {
        for (id in listOf("gemini", "groq", "openrouter")) {
            val p = ProviderCatalog.PROVIDERS.first { it.id == id }
            assertFalse(p.paid)
            assertNotNull(p.keyUrl)
        }
    }
}
