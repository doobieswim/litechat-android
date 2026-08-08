# THE ABYSS — Below the mud: mathematics of scarcity

*Deeper than forums. Deeper than eras. Deeper than product recipes.*  
*Here the voyager finds equations, phase transitions, and thermodynamic floors.*

Companions:
- `WEAK-RAM-DEEP-HISTORY.md` — archaeology  
- `THE-MUD-FUNDAMENTALS.md` — geology  
- **This file** — bedrock mathematics and physics  

---

## 0. Orientation

| Layer | Question |
|-------|----------|
| Forums | How do I free 40KB conventional? |
| History | What did each era invent? |
| Mud | What patterns always recur? |
| **Abyss** | **Why must they recur? What is conserved?** |

Conserved things in the abyss:

1. **Locality** (statistical structure of reference)  
2. **Working set size** as a function of time scale  
3. **Miss curves** under capacity  
4. **Energy per irreversible bit** (Landauer floor)  
5. **Hierarchy latency ratios** (orders of magnitude)  
6. **Admission vs thrashing phase boundary**  

If a product plan violates these, no amount of packaging saves it.

---

## I. Locality: the statistical miracle that makes computers possible

### I.1 Definition

**Locality of reference**: the tendency of a process to touch a small, slowly moving subset of its address space.

Kinds:

| Kind | Claim | Hardware that harvests it |
|------|--------|---------------------------|
| **Temporal** | Touch x now → likely touch x soon | Caches, LRU, working set |
| **Spatial** | Touch x → likely touch x±Δ | Cache lines, pages, prefetch |
| **Sequential** | Linear scans | Stream prefetchers |
| **Branch** | Few control-flow futures | Predictors + speculative fill |
| **Equidistant** | Strided access | Prefetch with stride |

Without locality, **every** level of the hierarchy fails simultaneously.  
Random access to a terabyte at register latency is not an engineering problem — it is a **thermodynamic and economic impossibility** at consumer scale.

### I.2 Why programs have locality (not accident)

1. **Human-written control flow** is loops, not white noise.  
2. **Data structures** pack related fields.  
3. **Algorithms** that solve decidable problems explore constrained neighborhoods.  
4. **Stack discipline** reuses frames.  

Denning later called this the **Locality Principle**: systems and minds both exploit concentrated reference.

**Abyss secret A1:**  
*Performance is not primarily “more GHz.” It is “how much of W stays in the fast tier.”*

### I.3 When locality dies

- Random index into huge tables  
- Linked structures that chase pointers across RAM  
- Full model weight scans every token with no cache reuse pattern  
- Pathological page replacement + adversarial reference strings  

Then miss rate → 1 and the machine becomes an IO engine.

---

## II. Working set formalized

### II.1 Denning’s definition (1968)

\[
W(t,\tau) = \{\, \text{pages referenced in } (t-\tau,\, t] \,\}
\]

- \(t\): process time  
- \(\tau\): window (the “memory of the recent past”)  
- \(|W|\): working-set size  

**Policy:** keep \(W\) resident; if not enough frames, **do not run** the process (or reduce multiprogramming).

### II.2 Why \(\tau\) matters

- Too small \(\tau\): underestimate W → thrash  
- Too large \(\tau\): overestimate W → underutilize RAM, fewer jobs  

There is no universal \(\tau\). There is only **measurement under load**.

### II.3 Code W vs data W vs TLB W

Even if code+data fit in cache, **page table entries** for a scattered W may not fit in the **TLB** → **TLB thrashing** (a third working set).

Modern chat apps:

| Working set | Contents |
|-------------|----------|
| Code W | Compose runtime, OkHttp, app DEX/OAT |
| Data W | Visible messages, images, stream buffer |
| Allocator W | heap fragmentation, GC free lists |
| TLB/page W | many small mappings if careless |
| (Optional) Model W | weights + KV |

**Abyss secret A2:**  
*You can thrash without filling DRAM — TLB and cache associativity are smaller abysses inside the abyss.*

---

## III. Misses, stacks, and Bélády’s cruelty

### III.1 The three C’s of cache misses

1. **Compulsory** — first touch (cold)  
2. **Capacity** — W larger than cache  
3. **Conflict** — mapping collisions in set-associative caches  

Plus: **coherence** misses on multicore.

Product translation:

- Cold start APK extract + JIT/AOT = compulsory tax (HenWorks 200MB install is a **mountain of compulsory misses**)  
- Holding too much UI state = capacity  
- Bad data layout = conflict  

### III.2 Stack algorithms

A replacement policy is a **stack algorithm** if more memory never hurts hit rate for any reference string (inclusion property).

- **LRU** is a stack algorithm  
- **FIFO is not**

### III.3 Bélády’s anomaly (1969)

For **FIFO**, there exist reference strings where **more page frames → more faults**.

Later: the anomaly ratio is **unbounded** (Fornai & Iványi, 2010).

**Abyss secret A3:**  
*“Just add RAM” is not even monotonically helpful under bad policy.*  
This is the mathematical ghost behind SoftRAM marketing and behind naive Android “memory cleaner” apps.

### III.4 Reuse distance (stack distance)

For each access, **reuse distance** = distinct addresses since last access to same line.

- Hit in a fully associative LRU cache of size C iff reuse distance < C  
- Miss curve = CDF of reuse distances  

This is the **experimental instrument** of the abyss: profile reuse distance of your chat scroll, image decode, and model decode — you will see why 4GB dies.

---

## IV. Thrashing as a phase transition

### IV.1 Qualitative

As multiprogramming degree \(N\) grows:

- Throughput rises (more overlap)  
- Then **collapses** when \(\sum |W_i| > M\) (memory size)  

The collapse is sharp — classic Denning plots look like congestion collapse / traffic jam.

### IV.2 Critical inequality

\[
\sum_{i \in \text{Runnable}} |W_i(t,\tau)| \;\le\; M_{\text{eff}}
\]

If violated:

\[
\text{Time} \approx \text{PageFaultService} \gg \text{UsefulCompute}
\]

### IV.3 Android’s response function

Android rarely thrash-loops like a desktop with huge swap.  
It **discontinuous-jumps** to kill:

\[
\text{Pressure high} \Rightarrow \arg\min_{\text{proc}} \text{value}(\text{proc}) \text{ receives SIGKILL}
\]

So the phase transition on phones is often:

```
smooth → jank → kill → cold start → smooth → …
```

not endless swap storm.

**Abyss secret A4:**  
*Mobile doesn’t thrash forever; it amnesiates.*  
Your architecture must survive **being forgotten** (process death) — Palm storage / Room discipline, not giant dirty heaps.

---

## V. Hierarchy numbers: the shape of the ladder

Order-of-magnitude intuition (modern CPUs; exact numbers vary):

| Tier | Latency scale | Capacity scale |
|------|---------------|----------------|
| Register | 1 cycle | KB |
| L1 | ~4 cycles | tens of KB |
| L2 | ~10–20 cycles | ~1 MB |
| L3 | ~40–60 cycles | tens of MB |
| DRAM | ~100–300+ cycles | GB |
| NVMe | ~10⁵+ cycles | TB |
| Network RTT | ~10⁶–10⁸ cycles | “infinite” |

**Bandwidth-delay:** even with fat pipes, **latency** to the wrong tier dominates interactive UX.

**Abyss secret A5:**  
*Interactive products are latency products. RAM tier determines whether a gesture feels alive.*

Chat typing + token stream must live in DRAM working set.  
Model weights may live in mmap/storage if traffic is sequential and prefetched — still tax.  
Cross-network is fine if **streaming hides RTT** (tokens), which is why BYOK SSE feels local.

---

## VI. Thermodynamic floor: Landauer

### VI.1 The bound

Erasing one bit irreversibly requires heat:

\[
E \ge k_B T \ln 2
\]

At room temperature ≈ \(2.9\times 10^{-21}\) J/bit — tiny vs today’s logic, but **directional**:

- Computation has a **physical cost**  
- Memory that is written and discarded is not free  
- Reversible computing is the only theoretical escape, not consumer phones  

### VI.2 Why this matters to product people

You will never hit Landauer on a phone app.  
You **will** hit:

- thermal throttling  
- battery  
- DRAM refresh power  
- radio energy for remote tokens  

which are the **engineering grandchildren** of the same truth: **state changes cost**.

**Abyss secret A6:**  
*Keeping state resident is an energy policy, not only a capacity policy.*  
A fat always-on agent on a pocket device pays rent in joules and in LMK risk.

---

## VII. LLM memory: closed-form pressure

### VII.1 Two sizes again, with symbols

- \(S_{\text{ckpt}}\): bytes on disk (quantized weights)  
- \(S_{\text{w,rt}}\): runtime footprint of weights (may be ≈ ckpt if fully resident; less if paged)  
- \(S_{\text{act}}\): activations (batch, seq fragments)  
- \(S_{\text{KV}}\): key/value cache  

Roughly:

\[
S_{\text{KV}} \propto L \cdot H \cdot d \cdot T \cdot b
\]

where  
\(L\) layers, \(H\) KV heads, \(d\) dim, \(T\) sequence length, \(b\) bytes per element.

**Linear in context length \(T\).**  
This is why “just one more long chat” OOMs weak devices.

### VII.2 Decode traffic

Autoregressive decode touches large parts of weights **per token** (depending on arch and kernel).  
Even with quantization, **bytes moved per token** can dominate.

Effective:

\[
\text{Tokens/s} \lesssim \frac{\text{MemBandwidth}}{\text{BytesPerToken}}
\]

On phones, bandwidth and thermals, not “TOPS” marketing, often bind.

### VII.3 The 4GB inequality (engineering form)

\[
\begin{align*}
&S_{\text{Android}} + S_{\text{UI}} + S_{\text{net}} + S_{\text{w,rt}} + S_{\text{KV}}(T) + S_{\text{misc}} \\
&\quad \le M_{\text{installed}} - M_{\text{margin}}
\end{align*}
\]

For daily-driver 4GB:

- \(S_{\text{Android}}\) is large and not yours  
- \(S_{\text{UI}}+S_{\text{net}}\) can be small (LiteChat)  
- \(S_{\text{w,rt}}+S_{\text{KV}}\) for “heavy” models **does not fit**  

Hence **remote brain** is not a product preference. It is the only feasible region of the inequality.

**Abyss secret A7:**  
*Heavy local LLM on 4GB is not “hard.” It is outside the feasible set for W(t,τ) of real models + real Android.*  
Tiny models may sit on the boundary; 7B+ class sits outside.

---

## VIII. Information theory: compression’s promise and lie

### VIII.1 Promise

If data has low Kolmogorov complexity / high redundancy, compress hard (graphics palettes, quantized weights, zram).

### VIII.2 Lie

1. You cannot beat entropy on incompressible payloads.  
2. **Decompression expands W** at the moment of use.  
3. Neural nets are compressed *knowledge*, but **inference instantiates large temporary state**.  

Elite’s galaxy seeds: tiny \(S_{\text{ckpt}}\), tiny \(S_{\text{rt}}\) (because generation is local math).  
LLM weights: large \(S_{\text{ckpt}}\), larger \(S_{\text{rt}}\) under long \(T\).

**Abyss secret A8:**  
*Procedural generation is compression that stays small when run.  
LLMs are compression that becomes large when run.*

That single distinction separates demoscene magic from on-device 70B fantasy.

---

## IX. Control theory of admission (formal product)

Define for each feature \(f\):

- \(W_f(\tau)\) expected working set  
- \(V_f\) user value  
- \(R_f\) risk (kill, thermal, legal)

**Default APK** maximizes \(\sum V_f\) subject to

\[
\sum_{f \in \text{Default}} W_f \le M_{\text{tier}}
\]

for tier = 4GB daily driver.

Features that violate are:

- excluded  
- or moved to **overlay admission** (download + RAM gate + separate process)  
- or moved to **remote**

This is Denning multiprogramming control applied to **product scope**.

HenWorks full agent on first launch optimizes \(V\) for power users while **violating** the 4GB constraint for the median device — hence review patterns about setup pain.

---

## X. Cognitive parallel (not metaphor only)

Human working memory is famously small (order 4–7 chunks).  
Long-term memory is vast but slow to encode/retrieve.  
Expertise is **chunking** — compression of patterns into single tokens of thought.

Computers and minds share the Locality Principle (Denning’s later essays).  
Products that dump infinite context into the hot loop punish both silicon and wetware.

Chat UX that pages history to disk and summarizes old turns is not only an engineering trick — it is **aligned with how memory works at every substrate we know**.

---

## XI. The abyss map (one page)

```
                    ┌─────────────────────┐
                    │   User-visible UX    │  must ride L1–DRAM W
                    └──────────┬──────────┘
                               │ locality
                    ┌──────────▼──────────┐
                    │  Working set W(τ)   │  the true size
                    └──────────┬──────────┘
            ┌──────────────────┼──────────────────┐
            ▼                  ▼                  ▼
      fits in Meff      borderline          exceeds Meff
            │                  │                  │
         smooth            jank/kill          thrash/OOM
            │                  │                  │
            │         admission control        must:
            │         (Denning/LMK)            compress /
            │                                  page /
            │                                  generate /
            │                                  remote /
            │                                  refuse
            ▼
      Civilization T/A/H
```

---

## XII. Commandments from the abyss (carry upward)

1. **Measure W, not GB.**  
2. **Miss curves before features.**  
3. **Never assume more RAM helps under bad policy (Bélády).**  
4. **Design for kill/restart (mobile phase transition).**  
5. **Treat context length as a first-class memory allocator.**  
6. **Prefer generators that stay small at runtime.**  
7. **Remote heavy W; stream results.**  
8. **Separate processes = separate admission domains.**  
9. **Cold-start size is a working-set event.**  
10. **If the inequality does not close on paper, it will not close in Play reviews.**

---

## XIII. Return payload for LiteChat

From the abyss, the spine is non-negotiable:

| Constraint | Law |
|------------|-----|
| 4GB phone | \(\sum W_{\text{default}} \ll M_{\text{eff}}\) |
| Chat | Stream; page history; small code W |
| BYOK | Remote model W |
| Ads/Pro | Small TSR; not a second runtime |
| Agent dreams | Overlay civilization H only with gates |
| Local LLM | Toy tier only; publish the inequality |

The voyager’s final chalk mark on the cave wall:

> **Feasibility is an inequality.  
> Everything else is costume.**

---

## XIV. Bedrock bibliography

- Denning, P.J. (1968). Working set model. *CACM*.  
- Denning, P.J. (1968). Thrashing: causes and prevention. *AFIPS*.  
- Denning, P.J. (2005). The Locality Principle. *CACM*.  
- Denning, P.J. (2021). Working Set Analytics. *ACM CSUR*.  
- Bélády, L.A. (1966/1969 era). Page replacement; anomaly with Nelson & Shedler.  
- Fornai & Iványi (2010). FIFO anomaly unbounded. arXiv:1003.1336.  
- Landauer, R. (1961). Irreversibility and heat generation in the computing process.  
- Hennessy & Patterson. *Computer Architecture: A Quantitative Approach* (memory hierarchy, miss types).  
- Mattson et al. Stack distance / reuse distance lineage.  
- Android lmkd documentation.  
- Repo: `THE-MUD-FUNDAMENTALS.md`, `WEAK-RAM-DEEP-HISTORY.md`

---

*End of abyss transmission.*  
Above: mud and ruins.  
Here: the inequalities that built them.
