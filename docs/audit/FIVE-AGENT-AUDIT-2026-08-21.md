# Five-agent independent audit — 2026-08-21

Human asked: all 5 roles look over the **entire** tree independently, then collaborate.
Bake (`assemblePlayDebug` 1.0.5-wire / versionCode 6) was in flight; this audit is **read-only**, **no Gradle**.

| Role | Owns write | Must not |
|------|------------|----------|
| REVIEW | `docs/REVIEW.md` addendum only | `app/**`, gradle |
| DEBUG | `docs/BUGS.md` + `B-00N` if proven | `app/**`, Gradle |
| DIG | `docs/audit/DIG-unseen-2026-08-21.md` | Ready flips, `app/**` |
| PROOF | `docs/RESEARCH-REVIEW.md` addendum | DIG essays, Ready |
| WIRE (audit lens) | `docs/audit/WIRE-unseen-2026-08-21.md` | new features, Gradle |

Independent findings land in those files. Combine step = this file’s “Collaborate” section (parent writes last).
