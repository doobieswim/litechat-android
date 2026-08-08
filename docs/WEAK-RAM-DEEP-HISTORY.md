# Weak-RAM computers → 4GB phones: deep historical research

Last updated: 2026-08-08  
Scope: start from weak-RAM **computers** (1970s–2000s), then map techniques to Android agent/LLM packaging.

Primary sources used this pass:
- Digital Antiquarian — *The 640 K Barrier* (filfre.net)
- Wikipedia — Conventional memory / 640 KB barrier
- VOGONS threads — DOS conventional memory tricks; “Not Enough Memory”
- HN — *How were 90s games so efficient?*
- SoftRAM / SoftRAM95 — Wikipedia, Microsoft Old New Thing, FTC settlement
- Elite / BBC Micro — retrocomputing lore (procedural generation)
- Hackaday — *Surviving the RAM Apocalypse* (2025)
- Prior research: HenWorks, OpenClaw/Termux, LiteRT-LM, Nanobot/PicoClaw

---

## 0. Thesis (one paragraph)

Every era that hits a hard memory ceiling invents the **same family of tricks**: (1) **shrink the resident set**, (2) **page / overlay / stream** the rest from slower storage or network, (3) **generate instead of store**, (4) **split the job across machines**, (5) **lie about free RAM** (placebos). SoftRAM was a fraud; EMS/XMS/overlays/procedural gen were real. A 4GB Android phone is not “more free than a 1995 PC” — Android + system services already ate the easy half. Heavy LLM weights are the new 640K barrier.

---

## 1. Era map: what “weak RAM” meant

| Era | Typical machine | Usable RAM for apps | Hard ceiling people fought |
|-----|-----------------|---------------------|----------------------------|
| Late 70s–early 80s | Apple II, PET, BBC Micro | 16–32 KB | Entire game in RAM |
| 1981–early 90s | IBM PC / clones, MS-DOS | First **640 KB** conventional | 640K barrier + real mode |
| Mid–late 90s | Win3.1 / Win95, 4–16 MB systems | Few MB after OS | Swap thrash, “RAM doublers” |
| Early 2000s mobiles | Feature phones, J2ME | Hundreds of KB–few MB | MIDP heap limits |
| 2010s Android budget | 512 MB–2 GB phones | ~half after system | LMK kills |
| 2026 budget Android | **4 GB** phones | Often ~1.5–2.5 GB free at best | Agent runtimes + local LLMs |

**Important:** 4 GB *installed* ≠ 4 GB for your app. On a daily-driver 4GB phone, expect system + launcher + GMS + WebView processes to leave a thin slice. Same lesson as DOS: **the OS and drivers already own the good seats**.

---

## 2. The 640 KB barrier (the original weak-RAM religion)

### What it actually was
- 8088/8086: 20-bit address space → **1 MB** total addressable.
- IBM reserved upper **384 KB** for video, BIOS, ROMs, expansion → apps got **640 KB conventional**.
- Not a DOS invention alone; IBM PC memory map + real-mode DOS culture locked it in for ~20 years.
- Even with megabytes of physical RAM later, DOS apps still needed **enough conventional** unless they used protected-mode extenders.

### Why people cared
Games and tools said: “Need 580K free” while `MEM` showed 480K because mouse, CD-ROM, SMARTDRV, sound, network TSRs sat in conventional memory.

### Real techniques (from VOGONS / period practice)

**A. Move the OS out of the way**
```
DEVICE=HIMEM.SYS
DEVICE=EMM386.EXE RAM I=B000-B7FF   ; steal mono video hole as UMB
DOS=HIGH,UMB
```
- `DOS=HIGH` → DOS kernel into HMA (first 64K above 1MB)
- UMBs → load drivers **high** (`DEVICEHIGH` / `LH`)
- `I=B000-B7FF` → reclaim monochrome adapter region if unused

**B. Smaller drivers (resident-set diet)**
- Replace MSCDEX with **SHSUCDX**
- Replace MS mouse with **CuteMouse (CTMOUSE)**
- Smaller CD-ROM `.SYS` (QCDROM / vendor native)
- Drop SMARTDRV / DOSKEY when gaming (each costs tens of KB)

**C. Load order & “init size vs resident size”**
- Programs often need **more RAM while loading** than while resident.
- First-fit UMB packing fails → force regions with `LH /L:n`
- `MEM /C` / `MEM /D` like Android `dumpsys meminfo`

**D. Boot menus (multiple personalities)**
- CONFIG.SYS menus: “GAMES / NETWORK / ULTIMA7”
- Ultima VII hated EMM386 → separate boot with almost nothing loaded
- Same idea as Android: **don’t keep proot+Node+browser+model all resident**

**E. Expanded vs extended (EMS vs XMS)**
- **EMS (LIM)**: bank-switch a **page frame** (classically 64 KB window) into larger pool — *window into a warehouse*
- **XMS**: copy blocks between high memory and conventional — *moving truck, slower*
- Games needed one or the other; wrong config = crash or “not enough memory”
- Modern analogue: **mmap model weights**, **KV-cache paging**, **GPU weight cache**, not “double RAM” snake oil

**F. Overlays (pre-virtual-memory)**
- Program split into segments; only active segment in RAM
- Programmers spent huge effort planning overlay graphs (classic OS courses estimate half of coding time on overlays in extreme cases)
- Modern analogue: dynamic feature modules, on-demand model layers, skill plugins loaded then unloaded

**G. Protected-mode DOS extenders**
- DOS/4GW etc. let games use megabytes while still “DOS”
- Analogue: leave thin Android UI in Java/Kotlin; put optional heavy work in a **separate process** that can be killed

### Placebos and frauds (important negative result)
- **SoftRAM / SoftRAM95** claimed to “double RAM”; FTC: false/misleading; often increased swap UI / placebo.
- Lesson for product: **do not sell “runs 70B on 4GB”** marketing. Users remember SoftRAM.

---

## 3. Procedural generation & Elite (fit universes in 22–32 KB)

Elite (BBC Micro, ~32 KB total RAM, game ~22 KB):
- Entire galaxies from **small seeds + deterministic math** (not stored tables)
- Same sprite/palette reuse (Mario clouds = bushes)
- Fixed-point math, backface culling, minimal overdraw

**LLM mapping:**
- Don’t ship encyclopedic local knowledge bases on 4GB
- Prefer **tools + retrieval** (search, files) over stuffing context
- Prefer **small on-device model + remote big model** over one giant local weights file
- Generate UI/state, don’t cache entire chat histories in RAM (Room/disk + window)

---

## 4. 90s game efficiency (HN folklore, still true)

From working 90s developers (HN #29127098):
- Budget **clocks per pixel per frame**
- Fixed-point, not float
- Texture atlases; auto-downsample when artists overflow budget
- LOD: 12-vertex tank when distant
- Dirty rectangles before full 3D redraw

**Android chat mapping:**
- Don’t keep full Compose Material mega-theme + WebView + RN bridge “just in case”
- Cap message list window; recycle aggressively
- Stream tokens; don’t buffer entire reply twice
- Ads: load late / one banner; AdMob known for jank/OOM on weak devices

---

## 5. What 90s/2000s *forums* actually argued about

### VOGONS culture (DOS gaming preservation)
Recurring advice when `Not enough memory`:
1. Post `CONFIG.SYS` + `AUTOEXEC.BAT` + `MEM /C`
2. Free conventional first, not “install more RAM” (you already had 16–64 MB)
3. Trade EMS vs max conventional
4. Some titles require **reboots into lean configs**
5. Third-party memory managers (QEMM, 386MAX, UMBPCI) free more but **break some games**

This is **exactly** the OpenClaw-on-phone discourse:
- wakelock / battery unrestricted / phantom process killer
- lean install vs proot Ubuntu
- disable browser skill to save RAM
- dedicated old phone vs daily driver

### Usenet / period programmer groups
- Overlay linkers, “memory exhausted” at link time on 4 MB machines
- Borland/MSVC complaining while IDE + compiler compete for conventional/XMS
- Same as: Android Studio on weak laptop vs shipping a thin APK

### SoftRAM threads (Ars, Dr. Dobb’s era)
- Community split: marketing believers vs kernel people calling placebo
- Product lesson: **transparent architecture > miracle claims**

---

## 6. Mid-layer history (phones before smartphones)

| Stack | Constraint | Trick |
|-------|------------|--------|
| J2ME / MIDP | Tiny heap | Record stores, not DOM; server-side logic |
| Brew / early native | OEM fragmentation | Thin clients |
| Opera Mini | Weak phones | **Server-side rendering** — classic “dumb terminal” |
| SMS bots | Almost no client RAM | All intelligence off-device |

**HenWorks/Opclaw’s actual product** is closer to “ship a server in your pocket.”  
**LiteChat** is closer to Opera Mini / SMS-bot philosophy: **client thin, brain remote**.

---

## 7. Mapping old techniques → 4GB Android + “heavy LLM”

### 7.1 What does **not** work (SoftRAM class)
| Claim | Reality |
|-------|---------|
| Run 7B–70B comfortably on 4GB daily driver | OOM / thrash / thermal death |
| Bundle full OpenClaw/Hermes + browser tools + local 7B | Stacked residency; OOM folklore already exists |
| “RAM doubler” apps | Swap theater |

### 7.2 What **does** work (real descendants of DOS wisdom)

| Old technique | 2026 analogue for LiteChat / agents |
|---------------|--------------------------------------|
| Free conventional first | Kill bloat: no RN/Flutter/WebView shell for simple chat; R8; arm64-only |
| LOADHIGH / smaller drivers | CuteMouse→small deps; no icons-extended; delay AdMob init |
| Boot menus | Product **modes**: Chat-only / Agent-light / Offline-tiny |
| EMS page frame | mmap GGUF / LiteRT weight paging; don’t lock full model |
| Overlays | Load agent skills on demand; unload after use |
| DOS extenders / second process | Isolate optional local runtime in separate process; LMK can kill it |
| SMARTDRV tradeoffs | Disk cache vs RAM: prefer streaming API, Room pagination |
| Procedural Elite | Tools + cloud model; tiny local model only for offline stubs |
| Dedicated lean machine | Recommend cloud BYOK on 4GB daily phone; agent appliance on spare phone/VPS |
| CONFIG.SYS menus | First-run: “Fast chat (recommended)” vs “Full agent (needs 6–8GB+)” |
| Don’t use QEMM if it breaks Ultima | Don’t enable proot+Node if chat-only SKU |

### 7.3 Honest tiers for “heavy” on 4GB

**Tier A — Product that always feels good on 4GB (LiteChat path)**  
- Thin native client  
- BYOK cloud / LAN Ollama on *another* machine  
- Target APK 2–5 MB; RSS tens–low hundreds MB  
- Ads optional; Pro removes ads  

**Tier B — Agent gateway on 4GB (tight, cloud brain only)**  
- OpenClaw/Hermes-class **without** local big model  
- Reports: gateway often **200–500 MB** if lean  
- Needs: wakelock, battery unrestricted, disable heavy skills  
- Still fails if user expects local 7B  

**Tier C — Local weights on 4GB (toy / demo, not “heavy”)**  
- ~0.5B–0.6B class (Qwen2 0.5B, Qwen3 0.6B ~0.5–0.6 GB files)  
- LiteRT-LM: even Gemma E2B peaks ~1.7 GB CPU on flagships — **marginal on 4GB phones with Android overhead**  
- Expect slow tokens, heat, LMK when user switches apps  

**Tier D — “Heavy LLM” (7B+) **  
- Needs **another computer** (desktop/VPS) or **8–12GB+ phone** dedicated  
- Phone is thin client or terminal — DOS remote / mainframe pattern  

### 7.4 If you insist on HenWorks-style packaging *and* 4GB

Do **not** copy Opclaw’s full Node bundle as the only mode.

Copy HenWorks **commerce shell**, ship **two engines**:

1. **Default (4GB):** LiteChat path — native chat, BYOK, instant  
2. **Optional “Lab” (6GB+/spare phone):** download agent runtime after free-space + RAM check; big red warning  

That is CONFIG.SYS multi-boot productized.

Ultra-light agent cores people discuss when OpenClaw OOMs:
- **Nanobot** (~4k LOC Python, MCP) — smaller than OpenClaw behemoth  
- **PicoClaw** (Go, &lt;10 MB RAM claims) — extreme lean, less hackable  

Still: **brain should stay cloud/LAN** on 4GB.

---

## 8. Android-specific “conventional memory” today

| DOS concept | Android concept |
|-------------|-----------------|
| Conventional 640K | App heap + native RSS before LMK |
| TSRs | Sticky services, GMS, overlay apps |
| EMM386 crashes Ultima | proot/Node + vendor battery killers |
| MEMMAKER | Android Studio Profiler / `adb shell dumpsys meminfo` |
| Boot disk | Safe mode / second user / work profile for agent |
| Phantom process? | Android 12+ phantom process killer for child procs |
| SoftRAM | Fake “booster” Play apps |

4GB phone free RAM is the new “I have 8 MB but only 540K conventional.”

---

## 9. Design principles distilled (for LiteChat + any future agent SKU)

1. **Resident set is the product.** Everything else is overlay/network.  
2. **Never market miracles.** SoftRAM destroyed trust; be explicit about tiers.  
3. **Multiple boots &gt; one obese binary.** Chat mode vs Agent mode.  
4. **Smaller drivers win.** Measure APK and RSS like VOGONS measured KB.  
5. **Page frames beat full loads.** Stream tokens, mmap weights, page chat history.  
6. **Generate &gt; store** when quality allows (tools, retrieval, procedural).  
7. **Put the heavy brain where RAM is cheap** (API/VPS/desktop).  
8. **Dedicated weak device for agents** is a feature, not a failure (old phone as server).  
9. **Ads are a TSR** — load carefully or sell Pro.  
10. **If it needs 200 MB install before first token, it’s not a 4GB daily-driver chat app.**

---

## 10. Implications for *this* repo (`byok-chat-android` / LiteChat)

Already aligned with deep history:
- Native Kotlin, no WebView/RN shell  
- R8, arm64-only, largeHeap=false  
- Streaming OpenAI-compatible client  
- Encrypted key, Room history  
- HenWorks-style ads + Pro **without** bundling agent runtime  

Do **not** pivot LiteChat into Opclaw-class runtime if 4GB smoothness is the north star.

Optional future (separate module/flavor):
- `chat` flavor (default)  
- `agent` flavor or on-demand Dynamic Feature with RAM gate  

---

## 11. Further reading (anchor links)

- https://www.filfre.net/2017/04/the-640-k-barrier/  
- https://en.wikipedia.org/wiki/Conventional_memory  
- https://www.vogons.org/viewtopic.php?t=26435  
- https://www.vogons.org/viewtopic.php?t=39326  
- https://news.ycombinator.com/item?id=29127098  
- https://en.wikipedia.org/wiki/SoftRAM  
- https://hackaday.com/2025/12/23/surviving-the-ram-apocalypse-with-software-optimizations/  
- https://developers.google.com/edge/litert-lm/overview  
- Repo: `RESEARCH.md` (HenWorks / thin clients)

---

## 12. Bottom line

Weak-RAM computing never got a free lunch. It got **overlays, page frames, smaller drivers, boot menus, procedural compression, and remote brains**.  

A 4GB phone running a **heavy** LLM *and* a **full** agent stack is asking for 1991 “not enough conventional memory” with a touchscreen.  

**Make heavy work by not keeping it resident:** thin LiteChat on the phone; big model on the network; optional agent only when the device and user opt into the fat boot.

### Doc trilogy (read in order of depth)

1. `docs/WEAK-RAM-DEEP-HISTORY.md` — eras, forums, machines  
2. `docs/THE-MUD-FUNDAMENTALS.md` — recurring laws & product civilizations  
3. `docs/THE-ABYSS-MATHEMATICS.md` — working sets, Bélády, Landauer, LLM inequalities  

---

## 13. Deeper layer: other weak-RAM *machines* (not just DOS PCs)

### 13.1 Amiga — Chip RAM vs Fast RAM (typed memory)

Amiga didn’t just have “how much RAM” — it had **kinds** of RAM:

| Type | Who can use it | Practical effect |
|------|----------------|------------------|
| **Chip RAM** | CPU + custom chips (video, audio, blitter) | Shared bus; graphics/audio **must** live here |
| **Fast RAM** | CPU only | Faster code/data; never blocked by copper/blitter |
| **Slow RAM** | CPU only, still on slow bus | Worst of both worlds (trapdoor expansions) |

Lessons still alive:
- **Not all memory is equal.** On Android: Java heap vs native vs GPU vs ION/ashmem vs compressed zRAM.
- Putting the wrong thing in the wrong pool kills you: huge bitmaps on CPU heap ≈ putting textures only in Chip RAM until the bus chokes.
- **FastMemFirst** culture: allocate precious pools last for the things that *must* use them.
- LLM map: KV-cache and activations are “Chip RAM” (hot, scarce); frozen weights can be “Fast/mmap/storage-backed.”

### 13.2 Palm OS — dynamic heap vs storage heap

Official Palm companion docs (classic 68K era):

- RAM split: **dynamic** (working set) vs **storage** (databases, survives reset).
- Dynamic heap was tiny by modern standards:
  - Early personal units: **~32 KB** total dynamic area
  - With TCP/IP: **64–96 KB** class
  - OS 3.5 scaling: 64 / 128 / **256 KB** dynamic depending on device RAM
- Chunks often capped near **64 KB** historically.
- Apps locked/unlocked handles so the Memory Manager could **compact** (move) chunks — explicit cooperation with the OS.
- **HotSync** = truth lives on the PC; handheld is a cache. Replace ROM/RAM module → resync.

This is one of the cleanest historical analogies to mobile AI:

| Palm | LiteChat / 4GB AI |
|------|-------------------|
| Dynamic heap | Active chat UI + stream buffer |
| Storage heap / DB | Room encrypted prefs, on-disk history |
| HotSync to desktop | Cloud/LAN model + optional backup |
| Don’t put big stuff in dynamic | Don’t hold full model or full history in process RSS |
| Compactible handles | Pagination, weak refs, recycle lists |

Palm programmers who allocated big structs on the dynamic heap shipped crashy apps. Same fate awaits “load 7B into the chat process.”

### 13.3 J2ME / MIDP — minimums measured in *kilobytes*

MIDP 1.0 class devices (spec folklore + era writeups):
- **128 KB** nonvolatile for MIDP components  
- **8 KB** nonvolatile for app persistent data  
- **32 KB** volatile for Java runtime (floor; real phones varied)

Developer folklore that still maps:
- Obfuscate to shrink classes (ProGuard lineage → R8 today)
- Few colors, few images, tile reuse
- **RMS** (Record Management System) not a filesystem fantasy
- Network: Generic Connection Framework — small static footprint; protocols optional
- Heavy logic **off device** when possible

If your Android chat app can’t beat a 2003 MIDlet on *discipline*, you’ve lost the plot even with gigabytes installed.

### 13.4 Opera Mini — the purest “remote brain” product

Opera Mini (from ~2005, huge on feature phones):
- Phone runs a **thin client**
- **Opera servers** fetch + pre-render/compress pages → OBML-like payload
- Complex web becomes affordable on weak CPUs and tiny heaps
- Tradeoff: fidelity, JS limits, privacy (traffic via proxy)

**This is the architectural twin of BYOK cloud chat:**
- User-facing app stays small
- “Intelligence” (page render then; LLM now) runs where RAM is cheap
- Product still feels local (one tap, offline-ish UI chrome)

HenWorks/Opclaw is the opposite bet: **move the server into the pocket**. Elite DOS/Palm/Mini tradition says: **keep the pocket thin**.

### 13.5 Demoscene 4K / 64K intros — size as a creative constraint

64K intro rules (inherited from COM size culture):
- Final binary ≤ 65536 bytes (4K intros ≤ 4096)
- Techniques: executable packers (kkrunchy, Crinkler), **procedural textures/meshes/music**, synth not samples, distance fields, GPU shaders as dense programs
- Example postmortems (~62 KB intros): thousands of lines of C++/GLSL that *expand* at runtime into worlds

Philosophy:
> You can’t ship assets. You ship **generators**.

LLM product map:
- Don’t ship a knowledge base; ship tools + prompts + retrieval  
- Don’t ship three UI frameworks; ship one generator of UI state  
- Optional: on-device **tiny** model as a “synth,” big model as studio orchestra offsite  

### 13.6 Android Low Memory Killer — the modern MEMMAKER enforcer

Android doesn’t politely thrash forever like a desktop with a huge swap. It **kills**:

- Historical **lowmemorykiller** kernel driver → userspace **lmkd**
- Monitors pressure; kills by `oom_score_adj` priority
- Cached apps die first; under critical pressure even more important processes
- Side effects before kill: jank, kswapd churn, launch slowdowns

Implications for agent-on-phone:
- A fat Node/proot stack + browser skill + chat UI is multiple kill targets
- Background gateway without careful foreground/service design **will** die (same as Termux without wakelock — different mechanism, same user story)
- `largeHeap=true` is a DOS conventional memory myth for most apps — doesn’t fix system-wide pressure and can make you a fatter kill target later

Treat LMK as the platform saying: **your resident set is a lie if the device is hungry**.

---

## 14. Universal pattern language (compressed)

Across BBC Micro, DOS, Amiga, Palm, J2ME, demoscene, Opera Mini, Android:

```
RESIDENT = must be here for latency/UX
OVERLAY  = loaded when needed, discarded after
PAGE     = window into large corpus (EMS, mmap, KV page-out)
GENERATE = code/seeds instead of data (Elite, 64k, tools+LLM)
REMOTE   = brain on richer machine (HotSync PC, Mini proxy, BYOK API)
LIE      = fake free memory (SoftRAM) — do not ship
```

**Heavy LLM on 4GB** only works if weights are OVERLAY/PAGE/REMOTE, never fully RESIDENT next to a full agent runtime and a chat UI.

---

## 15. Product recipes distilled from deep history

### Recipe A — “Palm + Opera Mini” (LiteChat default) ✅
- Small dynamic working set  
- Storage for history/keys  
- Remote model  
- Instant launch  
- Ads as optional TSR; Pro removes  

### Recipe B — “DOS multi-boot” (honest HenWorks-class)
- Boot menu in onboarding: Chat / Agent Lab  
- Agent Lab checks free RAM + storage; refuses on weak devices  
- Agent process isolated; can die without killing chat  

### Recipe C — “Amiga pools”
- UI process (Fast-ish)  
- Optional inference process (Chip-like scarce)  
- Never one process owns UI + 2GB weights + Node  

### Recipe D — “64k intro”
- Measure APK like sceners measure bytes  
- Procedural/minimal assets  
- One feature deeply, not ten frameworks shallowly  

### Recipe E — SoftRAM (forbidden)
- “AI booster doubles your RAM”  
- Silent swap growth marketed as capacity  

---

## 16. Extra sources (deeper pass)

- Palm OS Programmer’s Companion — Memory: https://palm.wiki/development/docs/601/PalmOSCompanion/Memory.html  
- Amiga Chip RAM: https://en.wikipedia.org/wiki/Amiga_Chip_RAM  
- 64k intro deep dive: https://www.lofibucket.com/articles/64k_intro.html  
- Demoscene / 64K: https://en.wikipedia.org/wiki/Demoscene  
- Opera Mini architecture: https://en.wikipedia.org/wiki/Opera_Mini  
- Android lmkd: https://source.android.com/docs/core/perf/lmkd  
- J2ME MIDP minimum memory class (era docs / InfoWorld performance pieces)  
- VOGONS conventional memory threads (practical CONFIG.SYS culture)

---

## 17. Closing (deeper)

The forums never discovered a way to run **arbitrarily large** programs in **arbitrarily small** RAM without changing the problem. They discovered **how to change the problem**:

- less resident  
- more paging  
- more generation  
- more remote  
- more honesty about modes  

That is the entire strategy for heavy LLM + weak phones. Everything else is SoftRAM with better marketing.
