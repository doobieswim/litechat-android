# Five-agent collaboration — 2026-08-21

Parent looked at the five **independent** reports, then this page. Not a sixth hunt.

Sources: REVIEW full-tree Issues · DEBUG B-005–B-010 · DIG unseen · PROOF Approve R-021–R-024 · WIRE unseen audit.

---

## Where they agree

| Topic | Who | What |
|-------|-----|------|
| Overlay ViewTree | REVIEW, WIRE | Working copy is the extension API. **HEAD still old `.set`.** Uncommitted. Next bake from git without this file dies. |
| Overlay type/Send | REVIEW Issue 2, DEBUG B-009 | `FLAG_NOT_FOCUSABLE`. REVIEW = Issue. DEBUG = Research (no phone proof). |
| `/video` poll sleep | DEBUG B-007 Ready, WIRE, REVIEW nit | `Thread.sleep` + Veo ignores HTTP. Stop is late. IO so not ANR today. |
| Backup empty pass | DEBUG B-005 Ready, old REVIEW leftover E nit | Blank pass `copyTo` live SQLite. |
| Named-key / backup dots | DEBUG B-006 Ready, old REVIEW nit | Main key already masked (B-004). Extra boxes still plain. |
| Do not re-open | all | B-001, B-004, leftover 1–8, A–I, R-019, R-020. |
| Static 222/222 ≠ compile | WIRE, REVIEW | Python green. Overlay HEAD would still fail Gradle. |

---

## Where they disagree (keep both; do not flatten)

- **Overlay IME:** REVIEW wants a code fix now. DEBUG wants a phone tap first (B-009 Research).
- **Fetch-models LAN steal:** REVIEW Issue 1 only. Others did not hunt Settings Fetch.
- **Cleartext whole-app:** REVIEW Issue 6 only.
- **Clear memory ungated:** DEBUG B-008 Ready. REVIEW did not list it.
- **Docs vs picker / cost labels:** DIG + PROOF Approve. REVIEW/WIRE did not own copy.

---

## Ranked next (if human says WIRE)

Phone / data-loss first, then bake-safety, then copy.

1. **Commit OverlayService ViewTree** (WIRE) — already on disk; bake-blocker if lost.
2. **B-005** plaintext backup
3. **B-006** mask extra secret boxes
4. **B-007** video poll `delay` + fail HTTP
5. **B-008** Clear memory Pro + confirm
6. REVIEW **Fetch models LAN** + **`/browse` Jsoup Stop** + **observeForever leak**
7. Overlay flags (after a phone check, or take REVIEW’s word)
8. Cleartext LAN-only
9. After PROOF Approve: human/DIG may Ready **R-021–R-024** (docs/cost/Sora date). Not phone crashes.

Do **not** Pro-gate `/imagine` or `/video`. Do **not** re-open B-001/B-004.

APK **1.0.8-wire** already uploaded. New code is **not** in that file except Overlay ViewTree if that bake used the working tree (it did). B-005–B-008 are **not** in that APK.
