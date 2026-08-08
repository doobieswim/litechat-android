# OpenAI-Client-Android — streaming library notes

Date: 2026-08-08  
Repo: [mardillu/OpenAI-Client-Android](https://github.com/mardillu/OpenAI-Client-Android) (MIT, ★~80 on JitPack)

## What it is

A Kotlin Android library wrapping OpenAI's full REST API: chat, images, audio, assistants, threads, runs, files, fine-tuning, embeddings, moderations. Includes SSE streaming (added in changelog).

## Streaming architecture

```
OpenApiClient.streamChatCompletion()
  → streamChatCompletionChunks()   // line-buffered SSE, no full-body buffer
    → ChatSseParser.parseStream()  // BufferedReader → typed ChatStreamChunk
      → ChatSseParser.dataPayloadFromLine()  // strips "data: " prefix
      → ChatSseParser.parseDataPayload()     // Gson → ChatStreamChunk
```

### Comparison: OCA vs LiteChat SSE parser

| Aspect | OpenAI-Client-Android | LiteChat |
|--------|----------------------|----------|
| Language | Kotlin `object` | Kotlin `object` |
| JSON lib | Gson | kotlinx.serialization |
| Typed model | `ChatStreamChunk` data class | `JsonObject` (lenient) |
| Test framework | JUnit 4 | JUnit 4 |
| Test fixture count | 5 (payload, delta, finish, multi-chunk, noise) | 8 (payload, DONE, delta, legacy-text, role-frames, empty-deltas, error, full-stream, noise) |
| Dependency weight | Gson (~250KB) | kotlinx.serialization-json (already in LiteChat) |
| Stream loop | `BufferedReader.readLine()` in `parseStream()` | Same, inline in `OpenAiCompatibleClient.parseSse()` |
| Error handling | Silent null on malformed JSON | Same, plus explicit `StreamEvent.Error` extraction |
| Multi-line SSE | Concatenates consecutive `data:` lines (OpenAI spec) | Single-line only (no multi-line support) |

### Key observation: multi-line SSE

OCA's `dataPayloadFromLine` concatenates consecutive `data:` lines until a blank line (per the SSE spec, some providers embed JSON across multiple `data:` lines). LiteChat treats each line independently. This matters for providers that emit large JSON objects across lines (rare in chat completions but common in other endpoints).

## Test fixture value for LiteChat

LiteChat already has comprehensive SSE tests. The remaining gaps OCA could fill:

1. **Multi-line SSE concatenation** test — not currently needed (no provider observed using it for `/chat/completions`)
2. **Typed model parsing** — LiteChat uses lenient `JsonObject` which is more robust to provider quirks; OCA's typed `ChatStreamChunk` is stricter but catches shape regressions
3. **Benchmark fixture** — neither library has a large-stream perf test

### Verdict: vendor-neutral test fixtures

LiteChat's existing `ChatSseParserTest` is already vendor-neutral (tests OpenAI-compatible wire format, not OpenAI-specific payloads). The OCA test suite adds no new fixture value beyond what LiteChat already has. **No additional test fixtures needed.**

## One historical note

OCA's README states "lightweight" as its core value prop — ironic given it wraps 13+ API endpoints with Gson. The actual "lightweight" library pattern (numAi NNJSON, 2.5KB) demonstrates what lightweight really means for 4GB devices.

## Recommendations

1. **No dependency** — LiteChat should NOT add OCA as a dependency (Gson weight, extra endpoints, API surface creep)
2. **Steal the multi-line SSE pattern IF** a provider shows up that emits split `data:` lines for chat completions
3. **Steal the `parseStream(BufferedReader, callback)` pattern** if LiteChat ever needs SSE parsing outside `OpenAiCompatibleClient`
4. **Test fixtures: no action needed** — existing coverage is sufficient

### Backlog line

No new ticket needed. Update LOST-REPOS.md with these notes.