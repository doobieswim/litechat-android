# THE MUD — Fundamental secrets of weak memory

*A voyager’s descent: not to list tricks, but to uncover what pressure always reveals.*

Companion to `WEAK-RAM-DEEP-HISTORY.md`.  
This document is the **geology** under the archaeology.

---

## Prologue: what “deeper” means here

Forums teach *config.sys*. Histories teach *eras*.  

Deeper is this: **memory scarcity is not a bug of old hardware. It is a permanent condition of computation**, because:

1. Interesting state grows faster than cheap, fast, local bits.  
2. Bits that are **fast and local** cost energy, area, and money.  
3. Programs that ignore this don’t fail politely — they enter a regime where **the machine works on the memory system instead of the problem**.

That regime has a name from 1968: **thrashing** (Denning).  
The cure has a name from 1968: **working set**.  
Everything else — EMS, overlays, Palm heaps, Opera Mini, LMK, BYOK chat, not loading 70B on a phone — is commentary.

---

## I. The physical secret: hierarchy is not optional

### I.1 Bits are not free

A usable memory bit must be:

- **Stored** (charge, spin, magnetic domain, trapped state)  
- **Addressed** (wires, decoders, row/column)  
- **Refreshed or held** (DRAM leaks; SRAM burns static power)  
- **Moved** (energy ≈ CV²; bandwidth is power × time)

So nature imposes a **ladder**:

```
registers  →  L1  →  L2/L3  →  DRAM  →  flash/disk  →  network  →  another building
   fastest                                              cheapest per bit
   tiniest                                              largest
```

There is no engineering path where the top of the ladder is as large as the bottom.  
If someone sells you that path, they are selling **SoftRAM**.

### I.2 Locality is the only reason computers work

Programs that randomly touch terabytes die.  
Programs that touch a **small, slowly changing neighborhood** live.

That neighborhood is the **working set** — Denning, 1968:

> W(t, τ) = pages touched in the last τ time units  

If W fits in real RAM, progress is computation.  
If W does not fit, progress is **traffic**.  
When traffic dominates, you are no longer running the program. You are running the **memory bus**.

**Secret #1:**  
*Capacity is fake; working set is real.*  
Installed GB is a brochure. W(t,τ) is the weather.

### I.3 Thrashing is phase transition, not slowdown

Thrashing is not “a bit slow.” It is a **collapse**:

- Each step faults  
- Faults evict pages needed next  
- CPU utilization looks high; useful work approaches zero  
- System may never recover without killing load  

Denning’s insight: thrashing is prevented by **not admitting** more multiprogramming than the sum of working sets allows.

Android’s **lmkd** is Denning with a knife:  
when Σ W exceeds RAM, **murder the least beloved process** rather than thrash forever.

**Secret #2:**  
*Under extreme pressure, civilized systems stop swapping and start executing.*  
(Execution of processes. Termination.)

---

## II. The economic secret: scarcity is chosen, then regretted

### II.1 1950s–70s: RAM cost more than programmers

Virtual memory was invented because **overlays ate human lives**.  
Atlas (Manchester) and successors automated the mud so programmers could pretend.

The bargain:

| Illusion sold | Tax collected |
|---------------|---------------|
| “Infinite memory” | Unpredictable traps, IO waits |
| “Just malloc” | Working set still binds |
| “Swap will save you” | Until thrash |

Embedded and avionics often **refuse** the illusion (Space Shuttle PASS used **explicit overlays**) because **determinism > comfort**.

**Secret #3:**  
*Virtual memory hides the mud from the programmer, not from physics.*  
On a phone with flash thrash + thermal limits, the mud returns as jank and LMK.

### II.2 Why 4GB phones exist

Not because 4GB is enough for 2026 software.  
Because **bill-of-materials**, battery, and market tier win auctions.

So the product designer inherits a **deliberate lie**:  
ship aspirations sized for 12GB devices onto 4GB silicon.

DOS users faced the same lie inverted:  
16MB installed, **540KB conventional** usable for the game.

**Secret #4:**  
*The cruel number is never “how much is installed.”  
It is “how much is left after the landlord (OS, OEM, ads, radios) takes rent.”*

---

## III. The informational secret: compress, generate, or leave

When state will not fit, only three honest moves exist:

### III.1 Compress (lossy or lossless)

- Quantize weights (Q4 GGUF)  
- Palette graphics, RLE  
- zRAM (compress RAM as RAM) — **pays CPU for capacity**

Compression is real. It is also **not magic**: entropy bounds still bind.  
You cannot losslessly hold a 70B model in 2GB.

### III.2 Generate (code as latent archive)

Elite galaxies from seeds.  
64k intros: meshes and music from algorithms.  
LLMs themselves are **extreme compression of text distribution** — but *running* them re-expands into activations + KV.

**Secret #5:**  
*Generators shrink storage; inference re-inflates working set.*  
A small checkpoint on disk can still demand a huge W at runtime.

### III.3 Leave (disaggregation)

Put the bulk elsewhere:

| Era | Elsewhere |
|-----|-----------|
| Mainframe | 3270 / green screen |
| Palm | Desktop HotSync |
| Opera Mini | Proxy farm |
| BYOK chat | API / home GPU |
| Agent | VPS |

**Secret #6:**  
*The oldest scalable UI is a thin terminal to a fat brain.*  
Calling it “cloud” or “BYOK” does not change the topology.

---

## IV. The control-theory secret: you must govern admission

### IV.1 Working set model (policy)

Admit a process to the running set **iff** its working set can be resident.  
Else swap the whole process out — better one finisher than ten thrashers.

### IV.2 Overlays (manual admission of *code*)

Tree of segments; siblings share a region; root stays.  
Programmer designs the **graph of mutual exclusion in memory**.

Modern forms:

- Dynamic feature modules  
- `dlopen` plugins  
- Agent “skills” loaded then freed  
- Separate processes for optional runtimes  

### IV.3 Page frames / EMS (admission of *data windows*)

You never needed all EMS at once — only the **64KB window** onto the warehouse.  
mmap of model weights is EMS with better PR.

### IV.4 Killers (admission failure handlers)

| System | Failure mode |
|--------|----------------|
| DOS | “Not enough memory” / refuse load |
| Desktop Linux | thrash or OOM killer |
| Android | **lmkd** proactive kill |
| iOS | jetsam |

**Secret #7:**  
*A mature weak-RAM product designs its own admission policy before the OS designs it with SIGKILL.*

LiteChat admission policy:

- Always admit: UI + network client + small DB page  
- Never admit by default: Node, proot Ubuntu, 7B weights, browser automation  
- Conditionally admit: tiny local model, only if free RAM gate passes  

---

## V. The stratified secret: not all RAM is the same mud

### V.1 Amiga teaching

Chip / Fast / Slow — **bandwidth and who may touch whom**.

### V.2 Modern phone teaching

| Pool | Role | Analogy |
|------|------|---------|
| CPU cache | µworking set | registers of the soul |
| App Java/Kotlin heap | managed objects | Palm dynamic |
| Native RSS | OkHttp, Room, bitmaps | conventional + XMS |
| GPU memory | if used | Chip RAM |
| zRAM | compressed desperation | SoftRAM’s honest cousin |
| Storage | Room, weights files | Palm storage / EMS warehouse |
| Network | API tokens stream | mainframe channel |

Putting a 2GB weight tensor in the wrong pool (fully faulted into app RSS on a 4GB device) is putting the copper blitter framebuffer in the only Fast RAM — everyone starves.

**Secret #8:**  
*Optimize placement before quantity.*  
Wrong pool with “enough” GB still dies.

---

## VI. The psychological secret: users buy capacity, feel working set

Marketing: “4GB RAM! 256GB storage! On-device AI!”  
Experience: freeze, reload, “app keeps stopping.”

DOS gamers learned to read `MEM /C`.  
Android power users learn to read which apps LMK reaped.  
Developers must learn to read **RSS + GC + page faults + binder** under load.

**Secret #9:**  
*Trust is working-set honesty.*  
SoftRAM failed because it sold capacity theater.  
HenWorks-style products that need 200MB bootstrap on a 4GB daily driver must set expectations like DOS boot menus — or inherit 1-star myths.

---

## VII. The LLM-specific secret: models have *two* sizes

| Size | Meaning |
|------|---------|
| **Checkpoint size** | Bytes on disk / download (compressed weights) |
| **Runtime working set** | Weights touched + activations + **KV cache** + allocator waste + runtime |

KV cache grows with **context length × layers × heads** — a second monster.  
Long chats are the enemy of weak RAM even when weights are quantized.

So “small model” can still thrash if:

- context is huge  
- batching is dumb  
- UI holds full transcript in RAM  
- multiple runtimes coexist  

**Secret #10:**  
*For edge LLM, context policy is memory policy.*  
Truncate, summarize, page history to disk (Palm storage), stream tokens, one active generation at a time.

---

## VIII. The product secret: choose your civilization

Three stable civilizations under the mud:

### Civilization T — Terminal (recommended for 4GB daily phone)

- Thin client  
- Fat remote brain  
- Instant launch  
- APK measured like a 64k intro  
- **LiteChat / Opera Mini lineage**

### Civilization A — Appliance (spare phone / 8GB+)

- Resident agent gateway  
- Cloud or LAN model  
- Wakelocks, foreground services, admission of skills  
- **HenWorks/Opclaw lineage** — honest about setup tax  

### Civilization H — Hybrid (dangerous if naive)

- One APK, two modes  
- Default T; unlock A after RAM/storage gate  
- Separate processes; A may die without killing T  
- **DOS multi-config civilization**

Unstable civilization:

### Civilization S — SoftRAM

- Claims H while shipping T’s marketing and A’s footprint  
- Users punish  

---

## IX. The fundamental equation (carry this)

```
UsefulProgress ∝  f( CPU )
                 / ( 1 + α · MemoryTraffic + β · KillRecover )

MemoryTraffic explodes when:
    Σ_i WorkingSet_i  >  EffectiveRAM

EffectiveRAM =
    Installed
  − Landlord(OS, OEM, radios, graphics)
  − Fragmentation
  − Compression tax
  − Safety margins
```

Design is the art of keeping **Σ WorkingSet** under **EffectiveRAM** for the target device tier — by shrinking, paging, generating, remoting, or refusing admission.

There is no fourth magic term.  
There is only SoftRAM’s fake term, which equals zero.

---

## X. Descent complete: what was locked in the mud

1. **Working set, not capacity**, decides survival.  
2. **Thrashing is a phase change**; killers are mercy.  
3. **Hierarchy is physics**; virtual memory is etiquette.  
4. **Compress / generate / remote** are the only honest escapes.  
5. **Admission control** is the adult form of CONFIG.SYS.  
6. **Pools matter** (Chip/Fast; heap/native/GPU).  
7. **LLMs inflate at runtime**; disk size lies.  
8. **Context is RAM**.  
9. **Thin terminal is the ancient scalable form.**  
10. **Product trust = matching civilization to device tier.**

For a 4GB Android phone that must feel good:

> Build Civilization T.  
> Offer A only as a separate boot.  
> Never sell S.

That is not a preference.  
That is what the pressure and the mud have always said, from Atlas to lmkd, from Elite’s seeds to a streaming chat token.

---

## XI. How this locks LiteChat’s spine

| Principle | Implementation already / next |
|-----------|--------------------------------|
| Minimize W | Native Compose, no WebView/RN, R8, arm64 |
| Page history | Room, not infinite LazyColumn state |
| Remote brain | OpenAI-compatible stream |
| Admission | No bundled agent runtime in default APK |
| Honest product | Ads + Pro shell without 200MB first-run tax |
| Future Hybrid | Optional feature module + RAM gate, separate process |

The voyager returns with one instruction carved on the bulkhead:

**Keep the resident set sacred. Exile the rest.**

---

## XII. Canonical references (bedrock)

- Denning, P.J. (1968). *The working set model for program behavior.* CACM.  
- Denning, P.J. (1968). *Thrashing: Its causes and prevention.* AFIPS.  
- Denning, P.J. (2021). *Working Set Analytics.* ACM Computing Surveys.  
- Virtual memory / Atlas history — overview literature  
- Overlay programming — OS/360 linkage editor culture; Shuttle PASS  
- Amiga Chip/Fast RAM architecture  
- Palm OS Memory Companion (dynamic vs storage)  
- Android lmkd documentation  
- Prior repo docs: `WEAK-RAM-DEEP-HISTORY.md`, `RESEARCH.md`

---

*End of descent.*  
The mud was never empty. It was full of working sets.
