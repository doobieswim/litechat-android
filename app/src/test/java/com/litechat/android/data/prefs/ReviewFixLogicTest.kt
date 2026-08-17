package com.litechat.android.data.prefs

import com.litechat.android.data.api.OpenAiCompatibleClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImagesUrlTest {
    @Test
    fun `catalog v1 host does not double v1`() {
        assertEquals(
            "https://api.openai.com/v1/images/edits",
            OpenAiCompatibleClient.imagesUrl("https://api.openai.com/v1", "edits"),
        )
        assertEquals(
            "https://api.openai.com/v1/images/generations",
            OpenAiCompatibleClient.imagesUrl("https://api.openai.com/v1", "generations"),
        )
    }

    @Test
    fun `trailing slash and already-complete url`() {
        assertEquals(
            "https://api.openai.com/v1/images/edits",
            OpenAiCompatibleClient.imagesUrl("https://api.openai.com/v1/", "edits"),
        )
        assertEquals(
            "https://api.openai.com/v1/images/edits",
            OpenAiCompatibleClient.imagesUrl("https://api.openai.com/v1/images/edits", "edits"),
        )
        assertEquals(
            "https://api.openai.com/v1/images/edits",
            OpenAiCompatibleClient.imagesUrl(
                "https://api.openai.com/v1/images/generations",
                "edits",
            ),
        )
    }
}

class VideoUrlTest {
    private val gemini = "https://generativelanguage.googleapis.com/v1beta/openai/"

    @Test
    fun `gemini native root strips openai and does not double v1`() {
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta",
            OpenAiCompatibleClient.geminiNativeRoot(gemini),
        )
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/veo-3.1-generate-preview:predictLongRunning",
            OpenAiCompatibleClient.veoStartUrl(gemini, "veo-3.1-generate-preview"),
        )
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/operations/abc",
            OpenAiCompatibleClient.veoPollUrl(gemini, "operations/abc"),
        )
        assertFalse(OpenAiCompatibleClient.veoStartUrl(gemini, "veo-3.1-generate-preview").contains("/openai"))
        assertFalse(OpenAiCompatibleClient.veoStartUrl(gemini, "veo-3.1-generate-preview").contains("/v1/v1"))
    }

    @Test
    fun `gemini picture uses generateContent not openai images`() {
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.1-flash-image:generateContent",
            OpenAiCompatibleClient.geminiGenerateContentUrl(gemini, "gemini-3.1-flash-image"),
        )
        assertFalse(
            OpenAiCompatibleClient.geminiGenerateContentUrl(gemini, "gemini-3.1-flash-image")
                .contains("/openai"),
        )
        assertEquals(
            "abc123",
            OpenAiCompatibleClient.firstInlineImageB64(
                """{"candidates":[{"content":{"parts":[{"text":"ok"},{"inlineData":{"mimeType":"image/png","data":"abc123"}}]}}]}""",
            ),
        )
        assertEquals(
            "snake",
            OpenAiCompatibleClient.firstInlineImageB64(
                """{"candidates":[{"content":{"parts":[{"inline_data":{"data":"snake"}}]}}]}""",
            ),
        )
        assertEquals(null, OpenAiCompatibleClient.firstInlineImageB64("""{"candidates":[]}"""))
    }

    @Test
    fun `openai and xai video urls do not double v1`() {
        assertEquals(
            "https://api.openai.com/v1/videos",
            OpenAiCompatibleClient.openaiVideosUrl("https://api.openai.com/v1"),
        )
        assertEquals(
            "https://api.x.ai/v1/videos/generations",
            OpenAiCompatibleClient.xaiVideoStartUrl("https://api.x.ai/v1"),
        )
        assertEquals(
            "https://api.x.ai/v1/videos/req1",
            OpenAiCompatibleClient.xaiVideoPollUrl("https://api.x.ai/v1", "req1"),
        )
    }
}

class FolderJsonTest {
    @Test
    fun `newline in name survives a save cycle`() {
        val list = listOf(ChatFolder("1", "Work\nhome"))
        val raw = SettingsRepository.encodeFolders(list)
        val back = SettingsRepository.decodeFolders(raw)
        assertEquals(1, back.size)
        assertEquals("Work\nhome", back[0].name)
    }

    @Test
    fun `quotes and dashes survive`() {
        val list = listOf(ChatFolder("q", """Q1 "draft" — Work-home"""))
        val back = SettingsRepository.decodeFolders(SettingsRepository.encodeFolders(list))
        assertEquals(list[0].name, back[0].name)
    }

    @Test
    fun `old hand-built json still loads`() {
        val raw = """[{"id":"1","name":"Work"}]"""
        val back = SettingsRepository.decodeFolders(raw)
        assertEquals(1, back.size)
        assertEquals("1", back[0].id)
        assertEquals("Work", back[0].name)
    }
}

class TemplateImportStrictTest {
    @Test(expected = Exception::class)
    fun `settings object is not a template array`() {
        SettingsRepository.decodeTemplatesStrict("""{"version":1,"templates":[]}""")
    }

    @Test(expected = Exception::class)
    fun `garbage is not treated as builtins`() {
        SettingsRepository.decodeTemplatesStrict("not-json")
    }

    @Test
    fun `real array imports`() {
        val list = SettingsRepository.decodeTemplatesStrict(
            """[{"id":"a","name":"A","template":"Hi [X]","variables":{"X":"1"}}]""",
        )
        assertEquals(1, list.size)
        assertEquals("A", list[0].name)
        assertEquals("Hi [X]", list[0].template)
        assertFalse(list[0].id == "builtin_translate")
        assertTrue(list[0].variables["X"] == "1")
    }
}
