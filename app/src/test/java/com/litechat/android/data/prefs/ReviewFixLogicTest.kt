package com.litechat.android.data.prefs

import com.litechat.android.data.api.BrowseUrl
import com.litechat.android.data.api.OpenAiCompatibleClient
import com.litechat.android.data.api.SlashInput
import com.litechat.android.data.context.MemoryManager
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
        assertEquals(
            "https://openrouter.ai/api/v1/images",
            OpenAiCompatibleClient.openrouterImagesUrl("https://openrouter.ai/api/v1"),
        )
    }

    @Test
    fun `xai edit json is not multipart`() {
        val body = OpenAiCompatibleClient.xaiEditJson(
            "grok-imagine-image-2.0",
            "make it blue",
            "data:image/png;base64,abc",
        )
        assertTrue(body.contains("grok-imagine-image-2.0"))
        assertTrue(body.contains("image_url"))
        assertTrue(body.contains("data:image/png;base64,abc"))
        assertFalse(body.contains("multipart"))
        assertFalse(body.contains("form-data"))
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
    fun `oauth 401 is plain english not google json`() {
        val msg = OpenAiCompatibleClient.friendlyMediaError(
            "pictures",
            401,
            """{ "error": { "code": 401, "message": "API keys are not supported by this API. Expected OAuth2 access token or" } }""",
        )
        assertTrue(msg.contains("AI Studio"))
        assertFalse(msg.contains("HTTP 401"))
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

class ApiKeySanitizerTest {
    @Test
    fun `strips superscript six and keeps the rest`() {
        val raw = "\u2076AQ.test-key_1"
        assertEquals("AQ.test-key_1", ApiKeySanitizer.headerSafe(raw))
        okhttp3.Headers.Builder()
            .add("x-goog-api-key", ApiKeySanitizer.headerSafe(raw))
            .add("Authorization", "Bearer ${ApiKeySanitizer.headerSafe(raw)}")
            .build()
    }

    @Test
    fun `userSafeError hides Unexpected char and the key`() {
        val e = IllegalArgumentException(
            "Unexpected char 0x2076 at 0 in x-goog-api-key value: SECRETKEY",
        )
        val line = ApiKeySanitizer.userSafeError(e, "Image generation")
        assertEquals(ApiKeySanitizer.BAD_KEY_LINE, line)
        assertFalse(line.contains("SECRETKEY"))
        assertFalse(line.contains("0x2076"))
        assertFalse(line.contains("Unexpected char"))
    }
}

class BrowseUrlTest {
    @Test
    fun `bare host gets https`() {
        assertEquals("https://example.com", BrowseUrl.normalize("example.com"))
        assertEquals("https://www.bbc.co.uk/news", BrowseUrl.normalize("www.bbc.co.uk/news"))
    }

    @Test
    fun `keeps a real https url`() {
        assertEquals(
            "https://en.wikipedia.org/wiki/Tea",
            BrowseUrl.normalize("https://en.wikipedia.org/wiki/Tea"),
        )
    }

    @Test
    fun `fixes http slash typo`() {
        assertEquals("https://example.com", BrowseUrl.normalize("http//:example.com"))
        assertEquals("https://example.com", BrowseUrl.normalize("https//:example.com"))
    }
}

class MemoryDecodeTest {
    @Test
    fun `missing hitCount does not wipe the list`() {
        val list = MemoryManager.decodeList("""[{"fact":"likes tea"}]""")
        assertEquals(1, list.size)
        assertEquals("likes tea", list[0].fact)
        assertEquals(1, list[0].hitCount)
    }
}

class SlashInputTest {
    @Test
    fun `peels a stuck letter in front of imagine`() {
        assertEquals("/imagine a cat", SlashInput.peel("v/imagine a cat"))
        assertEquals("/imagine a cat", SlashInput.peel("\u2076/imagine a cat"))
        assertEquals("/imagine a cat", SlashInput.peel("/imagine a cat"))
    }
}
