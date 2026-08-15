# [APP] BYO AI — Thin BYOK AI Chat for Android 8+ (4GB-friendly)

**I'm the developer** — this is open source (MIT). Feedback welcome.

## What is BYO AI?

A lightweight AI chat client that connects to YOUR OpenAI-compatible API key. No accounts, no subscriptions, no data collection. Your key, your endpoint, your device.

## Why this over the official ChatGPT app?

- **Works on weak phones** — targets 4GB RAM devices, honest compatibility matrix tells you if your phone can run it
- **One-time $4.99 Pro** (optional) — removes ads, not a subscription
- **Your API key** — pay OpenAI directly per token, much cheaper than $20/month ChatGPT Plus if you're a moderate user
- **Open source** — MIT licensed, FOSS flavor available (no Google Play Services)
- **Small** — 1.6 MB foss release APK (arm64, measured), native Kotlin + Compose

## Features

- Streaming SSE responses (with automatic non-stream fallback for flaky providers)
- `/imagine` — generate images via your provider's key
- `/video` — generate videos (streamed to disk, RAM-safe)
- `/browse` — fetch and analyze web pages
- Attach images for vision models
- Voice input, prompt templates with variables
- Multi-provider support (OpenAI, OpenRouter, Groq, Ollama, LM Studio, any OpenAI-compatible)
- Conversation forks, user memory, settings export/import
- Chat backup/restore, floating overlay (chat from any app)
- Honest free-RAM compatibility matrix on first launch

## Screenshots

[Attach screenshots here]

## Download

- **GitHub Releases:** https://github.com/flamingspade1995-coder/litechat-android/releases
- **Play Store:** [Coming soon]
- **F-Droid:** [Coming soon]

## Requirements

- Android 8.0+
- An API key from OpenAI, OpenRouter, Groq, or any OpenAI-compatible provider
- 4GB RAM recommended (works on less, but slower)

---

*Open source, MIT. Not affiliated with OpenAI, Google, or any AI company.*
