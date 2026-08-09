# Video generation feasibility for 4GB devices

Date: 2026-08-09
Status: Research

## RAM analysis: video playback

Video on Android uses **GPU hardware decoding** — the GPU has dedicated silicon for H.264/HEVC.
This is the most RAM-efficient media operation Android can do.

| Component | Memory |
|-----------|--------|
| Android built-in VideoView (wraps MediaPlayer + SurfaceView) | 0 KB APK — platform API |
| GPU decode buffers (720p, 8 sec video) | ~3-5 MB |
| MediaPlayer internal buffers | ~2-3 MB |
| **Total per playing video** | **~5-8 MB** |
| MP4 file on disk (720p H.264, 8 sec) | ~2-8 MB |

**Verdict: Video playback is RAM-safe on 4GB.** GPU decode uses almost zero CPU or heap memory.

## APK cost: Zero

- Android ships `VideoView` / `MediaPlayer` in the platform since API 1
- No ExoPlayer needed (saves ~1.5 MB APK)
- `VideoView` is a thin wrapper: `<VideoView>` in layout or `AndroidView` in Compose

## API feasibility

| Provider | Status | Cost |
|----------|--------|------|
| **OpenAI Sora 2** (`POST /v1/videos`) | 🔴 Deprecated — shutting down Sept 24, 2026 | Same key as chat |
| **BFL Flux 3** (via Nous subscription) | ✅ Available now, ~2-5 min generate | Included in Nous sub |
| **Runway Gen-3** | ✅ Separate API key needed | Paid per generation |
| **Pollinations AI** | 🟡 Free but low quality, no real video | Free |

**Problem:** Sora is the only BYOK option (same API key) but it's dying. We'd need:
1. Implement with Sora API now (maybe gets 3 months of life)
2. Or use BFL Flux 3 (separate endpoint, different auth)
3. Or wait for OpenAI's Sora replacement

## Implementation sketch

```kotlin
// /video command in ChatViewModel
if (text.startsWith("/video ")) {
    // 1. POST /v1/videos { prompt, size: "720p", duration: 8 }
    val job = client.createVideo(prompt)
    // 2. Poll GET /v1/videos/{job.id} every 2 seconds
    // 3. Show progress: "Generating… [=====>    ] 67%"
    // 4. Download MP4 when complete
    // 5. Save to cache as [VIDEO:path]
}

// MessageBubble video renderer
if (msg.content.startsWith("[VIDEO:")) {
    val path = extract path
    AndroidView(factory = { VideoView(it).apply {
        setVideoPath(path)
        setOnPreparedListener { start() }
    }})
}
```

## Files touched

| File | Change | APK |
|------|--------|-----|
| `OpenAiCompatibleClient.kt` | +`createVideo()`, +`pollVideo()` | 0 KB |
| `ChatViewModel.kt` | +`/video` handler + progress state | 0 KB |
| `Screens.kt` | +`VideoView` in MessageBubble | 0 KB |
| `build.gradle.kts` | No new deps | 0 KB |
| **Total APK impact** | **0 KB** — all platform APIs | |

## RAM concern on TIGHT devices

| Scenario | RAM |
|----------|-----|
| LiteChat baseline | 80 MB |
| Video file on disk (8s, 720p) | 4 MB disk |
| Video playback (GPU decode) | 6 MB |
| Image cache (2 MB, TIGHT band) | 2 MB |
| **Total during video playback** | **~88 MB** |
| On TIGHT device (<1GB free) | **9% of available RAM** |

**Conclusion: Video playback is RAM-trivial** because the GPU handles it. The bottleneck is API availability, not device memory.

## The honest problem

Sora is deprecated. We build it, it works for ~3 months, then breaks. Alternatives require separate API keys. This feature has an **expiration date**, not a RAM problem.

**Recommendation:** Build it anyway. The `/video` architecture (poll + progress + VideoView) is ~80 lines. If Sora dies, we swap the API endpoint — everything else stays. The UX and playback code is the real investment, and that's API-agnostic.