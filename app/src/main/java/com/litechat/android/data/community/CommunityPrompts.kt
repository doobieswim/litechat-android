package com.litechat.android.data.community

import com.litechat.android.data.prefs.PromptTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL

/**
 * Fetches community-contributed prompt templates from GitHub.
 * No accounts, no login — just a static JSON file anyone can PR.
 */
object CommunityPrompts {
    private const val URL = "https://raw.githubusercontent.com/flamingspade1995-coder/" +
        "litechat-android/main/community/prompts.json"

    suspend fun fetch(): List<PromptTemplate> = withContext(Dispatchers.IO) {
        try {
            val raw = URL(URL).readText()
            val arr = Json.parseToJsonElement(raw).jsonArray
            arr.map { el ->
                val obj = el.jsonObject
                PromptTemplate(
                    id = "community_${obj["name"]!!.jsonPrimitive.content.hashCode()}",
                    name = obj["name"]!!.jsonPrimitive.content,
                    template = obj["template"]!!.jsonPrimitive.content,
                )
            }
        } catch (_: Exception) { emptyList() }
    }
}