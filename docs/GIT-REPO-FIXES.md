# Git repo fixes guided by weak-RAM / streaming research

Date: 2026-08-08

## What we looked for

Open-source Android BYOK / OpenAI-compatible clients that are incomplete or
crash under memory pressure — fixable with:

- SSE line-buffered streaming (small working set)
- Input admission control (cap paste / measure height)
- No full-response buffering in libraries

## Candidates surveyed

| Repo | Stars | Finding | Action |
|------|-------|---------|--------|
| [mardillu/OpenAI-Client-Android](https://github.com/mardillu/OpenAI-Client-Android) | ~23 | Roadmap lists **Streaming for Chat Completions** unchecked; library otherwise solid & lightweight | **Fixed in local clone** |
| [Taewan-P/gpt_mobile](https://github.com/Taewan-P/gpt_mobile) #226 | 1.2k | Paste tens of KB → Compose `Constraints` crash (`height of 369898`); OOM issues historically | Upstream has PRs #255/#258; still open — **pattern applied to LiteChat** |
| [DanielBatesUK/chatgpt-android-app](https://github.com/DanielBatesUK/chatgpt-android-app) | 3 | Archived WebView shell; retired | Skip (dead product) |
| [wuxiang999/AIChatApp](https://github.com/wuxiang999/AIChatApp) | 0 | Full agent stack (Hilt, MCP, bash…) — opposite of thin client; claims 65+ fixes already | Skip for 4GB thin-client mission |
| LiteChat (`byok-chat-android`) | — | Our scaffold | **Hardened input cap** |

## Fix 1 — OpenAI-Client-Android streaming (local clone)

Path: `/opt/data/workspace/OpenAI-Client-Android`

### Added
- `ChatStreamChunk` / `ChatStreamChoice` / `ChatStreamDelta` models  
- `ChatSseParser` — pure JVM SSE parser, line-oriented, no full-body buffer  
- `ChatGptApiService.getChatCompletionStream` (`@Streaming` + `ResponseBody`)  
- `OpenApiClient.streamChatCompletionChunks()` → `Flow<ChatStreamChunk>`  
- `OpenApiClient.streamChatCompletion()` → `Flow<String>` content deltas  
- Unit tests: `ChatSseParserTest`  
- README roadmap checkbox + usage sample  
- `kotlinx-coroutines-core` dependency  

### Why this matches the research
- Working set stays small (8 KiB line buffer)  
- Remote brain; phone only joins tokens  
- Same SSE contract as LiteChat’s OkHttp client  

### How to upstream
```bash
cd /opt/data/workspace/OpenAI-Client-Android
# create branch, commit, PR to mardillu/OpenAI-Client-Android
```
Cannot push from this host without your GitHub credentials.

### Verify
```bash
# on a machine with JDK + Android SDK:
./gradlew :openai:testDebugUnitTest --tests '*.ChatSseParserTest'
```
Parser logic also verified ad-hoc via `/tmp/hermes-verify-sse-parser.py` (Python mirror).

## Fix 2 — LiteChat paste admission (gpt_mobile #226 class)

Path: `byok-chat-android/.../ChatViewModel.kt`

```kotlin
const val MAX_INPUT_CHARS = 32_000
// setInput() truncates pastes
```

UI already had `maxLines = 5` + `heightIn(max = 140.dp)`.  
Cap prevents Compose constraint overflow and binder-sized IPC disasters on weak devices.

Root cause class: **unbounded UI measure of pasted text** — not “need more RAM,” but **admission control of W**.

## Not fixed (and why)

- **gpt_mobile #226 upstream**: owner already iterated PRs; reopen status unclear without device test; forking 1.2k-star multi-provider app is a large merge surface. We stole the *pattern*, not the PR.  
- **Full agent apps on 4GB**: research says Civilization A — out of scope for thin LiteChat.  
- **Archived WebView ChatGPT wrappers**: product-obsolete; WebView is high RSS.

## Recommended next forks

1. Open PR for OpenAI-Client-Android streaming (this clone).  
2. If still broken on latest gpt_mobile: reproduce #226 with 100k-char paste; PR stronger cap + `BasicTextField` + document length.  
3. Keep LiteChat as reference thin client.

## Research → engineering map

| Research law | Fix |
|--------------|-----|
| Minimize working set | SSE line parse, no full body string |
| Admission control | MAX_INPUT_CHARS |
| Remote brain | stream tokens only |
| Mobile kills / amnesia | (library) no giant in-memory transcript required |
