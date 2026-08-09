# Video generation profitability vs competition

Date: 2026-08-09

## What competitors charge / offer

| Competitor | Video generation? | Price model |
|-----------|------------------|-------------|
| **TypingMind** ($39-99 one-time) | ❌ Not available | BYOK |
| **Chatbox** (40k stars) | ❌ No | BYOK |
| **Agora** (51MB APK) | ❌ No | Free |
| **OpenMinis** (iOS + Android) | ❌ No | Free, FOSS |
| **Enchanted** (Material You) | ❌ No | BYOK |
| **EveryTalk** (176 stars) | ❌ No | BYOK |
| **LiteChat** | 🟢 Would be FIRST | $4.99 one-time Pro |

**Nobody in the mobile BYOK chat space offers video generation.** This is wide-open territory.

## What video costs the user

| Source | Price per 10-second video |
|--------|--------------------------|
| OpenAI Sora 2 official (720p) | $1.00 |
| Third-party reseller (laozhang.ai) | $0.15 flat |
| Third-party reseller (kie.ai) | $0.15-0.30 |
| Runway Gen-4 (different API) | $0.50 |
| **Cheapest reliable option** | **$0.15-0.50 per video** |

The user pays this directly via their API key. LiteChat takes nothing.

## How LiteChat makes money from video

Video generation costs LiteChat nothing. But it makes money by:

### 1. It justifies the $4.99 Pro price
Right now Pro = "remove ads + unlock templates." That's weak. Add video and Pro = "generate AI videos from your phone." That's a REASON to pay.

### 2. Free trial hook
Free users get 1 video per week. Then they see a banner: "Want unlimited videos? Pro is $4.99 one-time." They've already tried it. They know it works. The conversion rate will be much higher than "pay to remove ads."

### 3. It's a competitive moat
Every competitor comparison table will show:
- TypingMind: No video
- Chatbox: No video  
- Agora: No video
- **LiteChat: Video generation ✅**

On the Play Store listing, "AI video generation" in the feature list alone could drive downloads.

## The math

| Scenario | Revenue |
|----------|---------|
| Pro price | $4.99 |
| After Google's 15% cut | $4.24 |
| 100 users buy Pro for video | $424 |
| 1,000 users buy Pro for video | $4,240 |
| 10,000 users buy Pro for video | $42,400 |
| **LiteChat's cost per video** | **$0.00** (user pays API directly) |

Each Pro sale is pure profit. Video is just the reason they click "buy."

## What to build

```
/video command → Pro-gated (free tier: 1 per week)
→ Poll Sora API with progress bar
→ Save MP4, play in VideoView
→ Zero APK cost, zero server cost, zero per-video cost to LiteChat
```

## Risk: Sora is dying

Sora 2 API shuts down September 24, 2026. But:
- Third-party resellers may continue operating after shutdown
- Runway Gen-4 has its own API ($0.50/video, needs separate key)
- The client code (VideoView, progress UI) works with any video API
- OpenAI will likely announce a Sora replacement before shutdown

## Bottom line

**Video generation makes LiteChat money by being the reason people pay $4.99.** Not by charging per video — by being the ONLY BYOK chat app that does it. It's a sales pitch, not a cost center.