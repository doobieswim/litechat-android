# Markdown Renderer Cost — LiteChat v1 Decision

**Research ticket:** R-002  
**Date:** 2026-08-08  
**Status:** Complete — recommendation: **DEFER markdown, stay plain text for v1**

---

## Executive summary

Adding markdown rendering to LiteChat would cost ~300–500 KB of APK bytes (15–30% increase over the current ~1.5–2 MB baseline) and 5–15 MB of RSS for the render cache + parsed ASTs. The streaming path requires an incremental parser (`StreamingMarkdownState`) to avoid O(n²) parse cost per reply, which adds engineering complexity around hoisted state, cache eviction, and toolchain bumps. The historical record across J2ME, Palm OS, and early Android is unanimous: rich text on constrained devices is always a second-wave feature, never v1. **Recommendation: ship v1 with plain text, open a post-v1 markdown ticket (C-008) as `Idea`, and let user demand drive priority.**

---

## 1. Library comparison — APK byte cost

Four options were compared. Raw artifact sizes are from Maven Central as of 2026-08-08. "Effective APK cost" is an estimate after R8 + resource shrinking on arm64, informed by maid-native's measured growth from 1.3 MB → 1.7 MB when they added the markdown renderer + Compose bump.

### Option A: Plain text (current)

| Metric | Value |
|--------|-------|
| Raw dep size | 0 bytes |
| APK cost | 0 bytes |
| RSS (render) | Negligible — `BasicText` composable, no parse, no spans |
| Streaming | Text concatenation only; O(1) per token |
| Engineering | Zero — already working |

### Option B: commonmark-java (org.commonmark:commonmark 0.24.0)

| Metric | Value |
|--------|-------|
| Raw JAR | **214 KB** |
| Transitive deps | None (standalone parser) |
| Android rendering | None built-in — outputs AST/HTML; needs a renderer on top |
| APK cost (est.) | ~120–170 KB (JAR only; no Compose renderer included) |
| RSS | AST objects ~9.5 B per character of content; no built-in cache |
| Streaming | Parser is batch-only; full re-parse per chunk → O(n²) on streaming |
| Notes | See §1.1 in the SPEC.md. The parser is vendored into OpenJDK; well-maintained. But needs a renderer to convert AST → Compose UI. Markwon bundles it and adds Spannable rendering (View system, not Compose). |

### Option C: Markwon (io.noties:markwon-core 4.6.2)

| Metric | Value |
|--------|-------|
| Raw AAR (core only) | **130 KB** |
| Transitive deps | commonmark-java (~214 KB), plus optional extensions |
| Total raw | ~344 KB minimum (core + commonmark) |
| Android rendering | Spannable-based → **TextView only** (View system, not Compose) |
| APK cost (est.) | ~200–280 KB |
| RSS | SpannableStringBuilder construction: 51ms / 5,855 chars / 456 spans (Dan Lew 2018, Samsung S7). Layout cost: ~9ms per StaticLayout. Spannable objects add per-span overhead in parallel arrays. |
| Streaming | Batch only — no incremental path documented |
| Notes | Last release 2021-02-08 (4.6.2). 3.4k stars, mature, but **View-system only**. LiteChat is pure Compose; mixing View/Compose interop for markdown would add complexity and defeat the purpose. Rejected on architectural grounds. |

### Option D: multiplatform-markdown-renderer (com.mikepenz v0.43.0)

This is the library maid-native adopted. Two artifacts needed for Compose M3:

| Artifact | Raw size |
|----------|----------|
| `multiplatform-markdown-renderer-android` 0.43.0 AAR | **386 KB** |
| `multiplatform-markdown-renderer-m3` 0.43.0 JAR | **5 KB** |
| **Subtotal** | **391 KB** |

Transitive dependencies (from POM):

| Dependency | Est. size |
|------------|-----------|
| `org.jetbrains:markdown-jvm` 0.7.5 | ~200 KB (estimated — no content-length on Maven, but sources JAR is 201 KB) |
| `kotlinx-collections-immutable-jvm` 0.5.0 | ~50 KB |
| `kotlinx-coroutines-core-jvm` 1.11.0 | Already present (LiteChat uses coroutines) |

| Metric | Value |
|--------|-------|
| Total raw deps (new) | ~640 KB |
| APK cost after R8 (est.) | **~300–500 KB** |
| APK % increase (1.5–2 MB base) | **15–33%** |
| Compose-native | Yes — renders `Markdown` composables directly |
| Streaming support | Yes — `StreamingMarkdownState` (append-only incremental parser) |
| Maid-native production proof | Yes — v1.2.0 ships it, APK grew 1.3→1.7 MB total (~400 KB of that was markdown + Compose; markdown share estimated 300–400 KB) |

#### Maid-native "~0.4 MB class" — what it actually means

The backlog reference to maid-native's ~0.4 MB markdown cost comes from the 2026-07-29 commit (`1218382`), where the toolchain bump (Compose 1.7.6→1.10.x + renderer 0.33.0→0.43.0) grew the signed release APK from ~1.3 MB to ~1.7 MB. The full 400 KB increase includes:

1. **Compose bump** (1.7.6 → 1.10.x): AndroidX Compose libraries grew between those versions — probably 100–150 KB of the total.
2. **Renderer bump** (0.33.0 → 0.43.0): the renderer AAR itself grew from 313 KB → 386 KB (+73 KB), and the 0.43.0 version pulled in `StreamingMarkdownState` from `org.jetbrains:markdown` 0.7.5.
3. **Toolchain artifacts**: Kotlin 2.1.0→2.4.10, AGP 9, etc., may shift R8 output slightly.

The **markdown-specific APK cost** for a new app starting from zero would be:
- Renderer AAR + M3 JAR + markdown-jvm + kotlinx-collections-immutable = ~640 KB raw
- After R8: ~300–400 KB decompressed in APK (the rest is the Compose baseline any Compose app already pays)

---

## 2. RSS (memory footprint) analysis

### Settled messages (scrolling/reading)

All markdown libraries build an AST (abstract syntax tree). maid-native measured this precisely (§11.1 of their SPEC):

| Content size | AST nodes | Parse time (desktop) | Est. on-device | AST memory |
|-------------|-----------|---------------------|----------------|------------|
| 406 chars | 88 | 0.15 ms | ~0.5–0.9 ms | ~3.9 KB |
| 1,194 chars | 248 | 0.22 ms | ~0.7–1.3 ms | ~11 KB |
| 3,164 chars | 648 | 0.59 ms | ~1.8–3.5 ms | ~30 KB |
| 7,952 chars | 1,608 | 0.67 ms | ~2.0–4.0 ms | ~76 KB |

**Key metric:** ~9.5 bytes of AST per character of markdown content.

**Cache memory:** maid-native's parse cache retains parsed ASTs for settled messages to avoid re-parsing on scroll-back. With a 512 KB character budget (their v2 fix), the cache retains ~5 MB. With their original 480-entry cap, it could reach 14–34 MB for chat-heavy users.

**LiteChat RSS impact estimate:**
- 20 messages × 1,000 chars avg = 20 KB characters → 190 KB AST cache
- 100 messages × 3,000 chars avg = 300 KB characters → 2.85 MB AST cache
- Peak (480-entry cap, worst-case long messages): up to 34 MB

For a 4 GB device with ~1.5–2.5 GB free after system/GMS, adding 2–5 MB for markdown is **tolerable but not free**. On a heavily loaded device near LMK thresholds, it could be the difference between staying resident and getting killed.

### Streaming messages

The incremental parser (`StreamingMarkdownState`) keeps a `StringBuilder` of accumulated content + `Snapshot(stableAst, unstableAstTail)`. Each token appends and re-parses only the trailing unfinished block — **O(n) per response instead of O(n²)**. Without it, the cumulative parse cost for a single 7,952-char reply was **449.8 ms (desktop) / ~1.5–2.7 seconds on-device** — and that's just parsing, not counting UI recomposition.

The streaming state must be **hoisted above the LazyColumn** (not inside a list item that LazyColumn disposes on scroll). This is an architectural constraint that affects the ViewModel→UI data flow.

---

## 3. Incremental vs batch rendering — streaming feasibility

### Can partial markdown crash the parser?

**No, with the right parser.** The key insight: a markdown parser that expects complete documents will fail on partial input (e.g., an unclosed code fence, a half-written table). But the `StreamingMarkdownState` in multiplatform-markdown-renderer 0.42+ is specifically designed for this:

- It uses `org.intellij.markdown.parser.StreamingMarkdownFile` (new in `org.jetbrains:markdown` 0.7.5)
- Keeps accumulated content in a `StringBuilder` and exposes `Snapshot(stableAst, unstableAstTail)`
- Stable nodes preserve identity across appends → Compose skips re-rendering finalized blocks
- Only the trailing unfinished block is re-parsed on each append

**Without incremental parsing**, you get what maid-native measured: a 7,952-char reply ≈ 1.6 million AST nodes allocated across 1,987 tokens, taking ~450ms desktop / ~2+ seconds on-device — of pure parse time, every reply. That's a jank factory.

### Integration decisions (from maid-native §11.2)

Two architectural decisions the maid-native team had to make:

1. **Hoist state above LazyColumn.** `rememberStreamingMarkdownState` dies with its composable and the parser cannot be re-seeded. If it lives inside a list item, scrolling it out of view destroys it mid-stream. Fix: hoist into `ChatScreen`, keyed on `streamingId`.

2. **Derive deltas from accumulated text, not chunk Flow.** `ChatUiState.streamingText` stays the single source of truth; the renderer tracks how much it has already appended and feeds `text.substring(appended)` from inside `snapshotFlow { … }.collect { … }`. This avoids exposing a `SharedFlow` of chunks to the UI and makes conflation safe by construction.

### Known upstream caveats

The `StreamingMarkdownState` in v0.43.0 has three quality issues raised in PR review and left as-is:

- Default rendering uses a keyless `Column` (slot reuse across tail changes is unguarded)
- `LazyMarkdownSuccess` keys on `startOffset` alone (collision risk if two blocks share an offset)
- `MarkdownElementInternal` keys its `remember` on the mutable `StringBuilder` by reference (the remembered model can go stale)

None of these has bitten maid-native in practice (tested on Android 11 and 16), but they are the first place to look if streaming rendering misbehaves.

**Bottom line:** Incremental streaming markdown IS feasible on weak devices — maid-native proved it — but it requires non-trivial engineering: hoisted state, delta derivation, cache character-budget eviction, and acceptance of known upstream caveats.

---

## 4. Historical lesson: rich text on constrained devices

The pattern across four decades of mobile computing is unambiguous: **rich text rendering is always a v2 feature.**

### J2ME LCDUI (MIDP 1.0/2.0, ca. 2000–2008)

| Constraint | Value |
|------------|-------|
| Typical heap | 32–128 KB for the entire Java VM |
| Text widgets | `TextField` (single-line, editable), `StringItem` (read-only label) |
| Rich text | **None.** No spans, no fonts, no colors, no inline formatting in standard API |
| Custom rendering | `CustomItem.paint(Graphics g)` — you drew every pixel, every glyph, yourself |
| Lesson | When your entire app has less RAM than a single modern AST, "rich text" is a fantasy. The most successful J2ME apps (Opera Mini, games) rendered **server-side or procedurally** and shipped thin clients. |

### Palm OS (ca. 1996–2007)

| Constraint | Value |
|------------|-------|
| Dynamic heap | 32–256 KB (separate from storage heap; manually managed with lock/unlock) |
| Text fields | Plain text only. No spans, no formatting API |
| UI model | Simple controls: Field, List, Table. All text rendering was system-provided, monochrome or grayscale, single font per field. |
| Lesson | Palm's entire UX philosophy was "simple, fast, gets out of your way." Rich text formatting simply did not exist in the API. The storage heap vs. dynamic heap split is the direct ancestor of our Room/disk vs. RAM working set distinction. |

### Early Android Spannable (API 1–14, ca. 2008–2012)

| Constraint | Value |
|------------|-------|
| Device RAM | 192–512 MB (HTC Dream/G1: 192 MB; Galaxy Nexus: 1 GB) |
| Spannable cost | Dan Lew (2018) benchmarked on Samsung S7 (4 GB): constructing a SpannableStringBuilder for 5,855 chars + 456 spans = **51.3 ms** and 713 append calls. Switch to StringBuilder+SpannableString: **1.5 ms** (25× faster). But layout cost inverted: SpannableStringBuilder layout = 9ms, SpannableString = 16ms. |
| Internal structure | SpannableStringBuilder uses a balanced interval tree; SpannableString uses parallel arrays. The tree is optimized for editing (EditText), not construction. |
| Lesson | Rich text is a performance tradeoff: construction speed vs. layout speed vs. memory. The internal data structures were designed for the EditText use case, not for constructing large formatted blocks from ASTs. Every span is an object allocation. On 192 MB devices, span-heavy UIs were a known source of GC pressure and jank. |

### The universal pattern

```
Generation 1: Plain text (prove the product)
Generation 2: Rich text (when RAM/APK budgets allow)
Generation 3: WYSIWYG editors (when devices are truly powerful)
```

Every platform followed this: SMS → MMS, IRC → Slack, plain-text email → HTML email, early web → CSS. The constraint isn't API availability — it's the cumulative cost of APK bytes + RAM + engineering time on devices where every byte counts.

### Relevance to LiteChat

LiteChat targets devices with 4 GB installed (~1.5–2.5 GB free). That's 10,000× the heap of a J2ME phone, but the physics is the same: the OS, GMS, and other apps consume most of it. The markdown renderer cache alone (5 MB for a normal chat, 14–34 MB for pathological cases) is a rounding error on an 8 GB flagship but a meaningful fraction of available RAM on a 4 GB device running multiple apps.

**The historical lesson is not "avoid rich text" — it's "don't add rich text until you know users want it, and even then, measure the cost before shipping."**

---

## 5. Decision and rationale

### Recommendation: **DEFER markdown rendering — stay plain text for LiteChat v1**

**Rationale:**

1. **APK budget.** LiteChat's core value proposition is being the lightest possible BYOK chat client. Adding 300–500 KB (15–33% increase) for a rendering feature that is not essential to the core "send text, receive text, stream tokens" loop undermines that positioning.

2. **RSS budget.** On a 4 GB device, every megabyte matters near LMK thresholds. The markdown cache retains 5 MB in normal use and up to 34 MB in edge cases. This directly competes with keeping the streaming socket alive in the background.

3. **Engineering cost.** Incremental markdown streaming requires:
   - Hoisted composable state above LazyColumn
   - Delta derivation from accumulated text (not chunk Flow)
   - Character-budget cache eviction (not entry-count)
   - Toolchain bump (renderer 0.43.0 needs compileSdk 37, Kotlin 2.4.x, AGP 9)
   - Testing on weak devices for streaming jank
   
   Every hour spent on markdown engineering is an hour not spent on stability, performance, or distribution.

4. **Plain text covers the critical use case.** The most common reason a user wants markdown in a BYOK chat client is reading code blocks from the AI. Plain text with monospace styling (which Compose `BasicText` supports natively) handles indentation, newlines, and ASCII structure without a parser. Bold/italic/headings/links/tables are nice-to-have, not need-to-have.

5. **Maid-native's path is proven for post-v1.** When user demand justifies it, the multiplatform-markdown-renderer (`com.mikepenz`) with `StreamingMarkdownState` is the correct choice — it's Compose-native, has an incremental streaming path, and maid-native has documented the integration decisions and pitfalls. The toolchain bump required (compileSdk 37, AGP 9, Kotlin 2.4+) is known and manageable.

### What to do instead (v1 plain-text enhancements)

For v1, LiteChat should render AI responses with these low-cost formatting affordances:

1. **Monospace for code:** Detect fenced code blocks via simple string scanning (three backticks) and render content between them in a monospace `BasicText`. No full parser needed — just line-by-line state machine.
2. **Preserve whitespace:** Newlines and indentation carry meaning in plain text; don't collapse them.
3. **Horizontally scrollable code blocks:** Long lines in code should scroll rather than wrap (avoid layout thrash on small screens).

---

## 6. Post-v1 markdown ticket (C-008)

If markdown is deferred, the following ticket captures what needs to happen when it's eventually pursued.

### C-008 — Markdown rendering (post-v1)

- **Status:** Idea
- **Depends on:** User demand data from v1 (Play reviews, feedback channels)
- **Goal:** Add incremental streaming markdown rendering using `com.mikepenz:multiplatform-markdown-renderer` with `StreamingMarkdownState`, following the maid-native integration pattern.
- **Acceptance criteria:**
  - [ ] APK growth ≤ 500 KB over baseline (measure before/after with `assembleRelease` diff)
  - [ ] Streaming replies use `StreamingMarkdownState` (O(n) parse, not O(n²))
  - [ ] Parse cache bounded by character count (512 KB budget ≈ 5 MB retained)
  - [ ] Markdown state hoisted above LazyColumn, keyed on streamingId
  - [ ] Settled messages render from synchronous `parseMarkdown()` with LRU cache
  - [ ] Toolchain: compileSdk 37+, Kotlin 2.4.x+, AGP 9.x+, Compose BOM 2026.06+
  - [ ] Tested on 4 GB device: streaming jank ≤ 1 skipped frame per 100 tokens
  - [ ] Plain-text fallback: if renderer init fails, degrade to `BasicText` (never crash)
- **Files likely touched:** `build.gradle.kts`, `libs.versions.toml`, `ui/markdown/Markdown.kt` (new), `ui/chat/ChatScreen.kt`, `ui/chat/MessageItem.kt`, `ChatViewModel.kt`
- **Out of scope:** Markdown images (needs Coil), syntax-highlighted code blocks, LaTeX math, HTML rendering, WYSIWYG composer
- **Research links:**
  - `docs/MARKDOWN-COST.md` (this document)
  - maid-native SPEC.md §11, §11.1, §11.2 (parse cost audit, streaming rework)
  - maid-native commit `1218382` (incremental streaming parse + char-bounded cache)
  - Dan Lew, "Exploring Spannable Performance" (2018) — SpannableStringBuilder vs. StringBuilder+SpannableString construction costs

---

## Appendix A: Raw Maven Central artifact sizes

Measured 2026-08-08 via `curl -sI` on Maven Central:

| Artifact | Version | Type | Bytes | KB |
|----------|---------|------|-------|-----|
| org.commonmark:commonmark | 0.24.0 | JAR | 214,528 | 210 |
| io.noties:markwon-core | 4.6.2 | AAR | 133,475 | 130 |
| com.mikepenz:multiplatform-markdown-renderer-android | 0.43.0 | AAR | 386,300 | 377 |
| com.mikepenz:multiplatform-markdown-renderer-m3 | 0.43.0 | JAR | 4,933 | 5 |
| com.mikepenz:multiplatform-markdown-renderer-android | 0.33.0 | AAR | 312,820 | 305 |
| org.jetbrains:markdown (JVM) | 0.7.5 | JAR | ~200,000 | ~195 |

Note: `org.jetbrains:markdown-jvm` 0.7.5 did not return a `content-length` header on Maven Central; the estimate is from the sources JAR (200,897 bytes) and typical JVM JAR overhead.

## Appendix B: Key sources

- [maid-native SPEC.md §11–§11.2](https://github.com/HatsyRei/maid-native/blob/main/SPEC.md) — parse cost audit, streaming rework, cache design
- [maid-native commit 1218382](https://github.com/HatsyRei/maid-native/commit/1218382a937a3f177e93e112837a6591464cc3d4) — incremental streaming parse implementation
- [multiplatform-markdown-renderer](https://github.com/mikepenz/multiplatform-markdown-renderer) — Compose M3 markdown library
- [commonmark-java](https://github.com/commonmark/commonmark-java) — Java CommonMark parser
- [Markwon](https://github.com/noties/Markwon) — Android Spannable markdown (View system, last release 2021)
- Dan Lew, ["Exploring Spannable Performance"](https://blog.danlew.net/2018/08/30/exploring-spannable-performance/) (2018) — SpannableStringBuilder construction/layout benchmarks
- Tung Doan, ["The Ultimate Deep Dive into Android Spannable"](https://medium.com/@doanthanhtungnk123/the-ultimate-deep-dive-into-android-spannable-internals-performance-best-practices-compose-35f40be68a5b) (2025)
- Palm OS Programmer's Companion — [Memory Architecture](https://palm.wiki/development/docs/601/PalmOSCompanion/Memory.html)
- Oracle J2ME MIDP 2.0 — [LCDUI package documentation](https://docs.oracle.com/javame/config/cldc/ref-impl/midp2.0/jsr118/javax/microedition/lcdui/package-summary.html)