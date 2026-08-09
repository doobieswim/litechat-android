# Differentiation Features Implementation Plan

> **For Hermes:** Use subagent-driven-development skill to implement this plan task-by-task.

**Goal:** Build 5 features that make LiteChat stand out from TypingMind, Chatbox, and Agora.

**Architecture:** All features are zero-APK, zero-server, pure Kotlin/Compose. No new dependencies. Build order: easiest → hardest.

**Tech Stack:** Kotlin, Jetpack Compose, Room, existing OkHttp client.

---

## Feature 1: Real-time cost display

### Task 1.1: Add cost estimation to ChatUiState

**Objective:** Track approximate token cost per response.

**Files:**
- Modify: `app/src/main/java/com/litechat/android/ui/ChatViewModel.kt`

```kotlin
// In ChatUiState, add:
val lastCost: String? = null,  // e.g. "≈ $0.0003"
```

```kotlin
// In send(), after Done event, calculate and set cost:
val approxTokens = acc.length / 4
val cost = approxTokens * 0.0000015  // ~$1.50 per 1M tokens (GPT-4o-mini)
_state.update { it.copy(lastCost = "≈ $${"%.4f".format(cost)}") }
```

### Task 1.2: Show cost banner in UI

**Objective:** Display cost under streaming complete.

**Files:**
- Modify: `app/src/main/java/com/litechat/android/ui/Screens.kt`

Add after truncation indicator:
```kotlin
state.lastCost?.let { cost ->
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
        Text(cost, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
```

### Task 1.3: Update verify_static, commit

---

## Feature 2: Chat as files

### Task 2.1: Add `exportConversation()` to ChatRepository

**Objective:** Export one conversation as plain text.

**Files:**
- Modify: `app/src/main/java/com/litechat/android/data/db/ChatRepository.kt`

```kotlin
suspend fun exportConversation(id: String): String {
    val msgs = messageDao.listForConversation(id)
    val conv = conversationDao.getById(id)
    return buildString {
        appendLine("=== ${conv?.title ?: "Chat"} ===")
        appendLine("Exported: ${java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())}")
        appendLine()
        msgs.forEach { msg ->
            val role = if (msg.role == "user") "You" else "Assistant"
            appendLine("$role:")
            appendLine(msg.content)
            appendLine()
        }
    }
}
```

### Task 2.2: Add export button to chat screen + ViewModel

**Objective:** Share conversation as text file.

Add to ChatScreen top bar actions (share icon).

### Task 2.3: Update verify_static, commit

---

## Feature 3: Screenshot detector

### Task 3.1: Create ScreenshotDetector utility

**Objective:** Detect new screenshots via MediaStore.

New file: `app/src/main/java/com/litechat/android/util/ScreenshotDetector.kt`

```kotlin
object ScreenshotDetector {
    fun detectNew(uri: Uri?): Boolean = uri != null
    
    fun isRecentlyTaken(context: Context, path: String): Boolean {
        val file = File(path)
        return file.exists() && System.currentTimeMillis() - file.lastModified() < 30_000
    }
}
```

### Task 3.2: Add screenshot prompt to ChatViewModel

**Objective:** When returning to app, offer to analyze recent screenshot.

Add `checkRecentScreenshot()` method that scans DCIM for recent images and sets a flag.

### Task 3.3: Add "Ask about this screenshot?" chip to UI

**Objective:** Chip above input when screenshot detected.

### Task 3.4: Update verify_static, commit

---

## Feature 4: Community prompts

### Task 4.1: Create CommunityPrompts.kt

**Objective:** Fetch prompts JSON from GitHub on app start.

New file: `app/src/main/java/com/litechat/android/data/community/CommunityPrompts.kt`

```kotlin
object CommunityPrompts {
    private const val URL = "https://raw.githubusercontent.com/flamingspade1995-coder/litechat-android/main/community/prompts.json"
    
    suspend fun fetch(): List<PromptTemplate> {
        val json = withContext(Dispatchers.IO) {
            URL(URL).readText()
        }
        return parsePrompts(json)
    }
}
```

### Task 4.2: Create prompts.json community file

Create: `community/prompts.json` — starter community prompts.

### Task 4.3: Add community section to template UI

Add "Community" label above templates that are from the community feed.

### Task 4.4: Update verify_static, commit

---

## Feature 5: LAN auto-detect

### Task 5.1: Create LanDetector.kt (mDNS scanner)

**Objective:** Scan local network for Ollama instances.

New file: `app/src/main/java/com/litechat/android/util/LanDetector.kt`

```kotlin
object LanDetector {
    suspend fun scan(context: Context): List<String> {
        // Try common Ollama ports: 11434
        val addresses = listOf(
            "http://192.168.1.1:11434",  // common router IP pattern
            // Scan local network...
        )
        return addresses.filter { addr ->
            try {
                val ok = URL("$addr/api/tags").readText()
                ok.contains("\"name\"")
            } catch (_: Exception) { false }
        }
    }
}
```

### Task 5.2: Add LAN scan to Settings + auto-offer

Add "Scan network" button in Settings → populates model picker with LAN-found models.

### Task 5.3: Update verify_static, commit

---

## Implementation order

| # | Feature | Time | Impact |
|---|---------|------|--------|
| 1 | Cost display | 30 min | Trust |
| 2 | Chat as files | 45 min | Ownership |
| 3 | Screenshot detector | 1h | Speed |
| 4 | Community prompts | 2h | Community |
| 5 | LAN auto-detect | 3h | Offline |

**Total: ~7.5 hours of focused coding. All zero APK, zero server cost.**