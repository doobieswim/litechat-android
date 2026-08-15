# Fastlane folder (metadata only)

This folder is the **store listing text** F-Droid and Play read. It is not the
Ruby Fastlane app.

**What we use (free, already in the repo):**

```
fastlane/metadata/android/en-US/
  title.txt                 Play/F-Droid title (≤30)
  short_description.txt     ≤80 — locked brand line
  full_description.txt      ≤4000 — FOSS copy (no ads, no billing)
  changelogs/<versionCode>.txt
  fdroid.yml                notes for the fdroiddata MR (not read by fdroidserver)
```

**What we do NOT use (on purpose):**

- The Ruby `fastlane` gem
- A `Fastfile` that uploads to Play (`supply`) — that needs the **$25** Play
  developer account + a Google service-account JSON. Do not add those without
  asking the human first.
- Screenshots / featureGraphic — still human work (real phone, real UI)

CI already runs `python3 scripts/verify_static.py` first. That script now
checks these files (length limits, locked brand line, measured 1.6 MB size,
no Play-billing talk in the FOSS listing). If the listing is wrong, the
build fails before Gradle starts.
