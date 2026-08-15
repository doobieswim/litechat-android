# BYO AI — F-Droid Build-System Audit

**Repo:** `/opt/data/workspace/byok-chat-android` (byok-chat-android / LiteChat → BYO AI)
**Date:** 2026-08-15
**Method:** Static analysis + research only. **No gradle/build was run** (F-Droid builds on their servers; task rule). Every load-bearing claim below carries a file:line reference (repo) or a URL (F-Droid docs / fdroidserver source). Supporting evidence: local git state, tracked-file inventory, and the repo's green GitHub Actions history (`assembleFossRelease` runs `lintVitalRelease`, so Android-Lint-ERROR-severity issues are already excluded — see `.github/workflows/build.yml:47-52`).
**Scope:** Will the `foss` flavor pass F-Droid's build system (`fdroidserver` + `fdroiddata` review + build-server build)?

---

## Headline verdict: **NEEDS FIXES**

The **foss flavor itself is F-Droid-clean** (zero GMS/AdMob/billing code compiles into it; MIT; no proprietary deps; no tracked binaries). But the **metadata recipe as written would fail or be rejected**: `subdir: app` runs Gradle in the wrong directory (build break), `commit: main` violates the metadata reference (review blocker), the repo has **zero tags** so `UpdateCheckMode: Tags` can never find an update, and the mandatory F-Droid description files (`short_description.txt`, `full_description.txt`) don't exist. All fixes are small (<1 hour of work + a tag).

---

## 1. Requirement-by-requirement verdict

| # | Requirement | Verdict | Evidence |
|---|-------------|---------|----------|
| 1.1 | FOSS flavor contains no proprietary code | ✅ PASS | `app/build.gradle.kts:139-140` — GMS deps are `add("playImplementation", …)` only; `app/src/foss/**` has 3 pure stubs with zero `com.google` imports (`AdMobLazyInit.kt`, `BillingRepository.kt`, `BannerAd.kt`); grep of `app/src/main` + `app/src/foss` shows GMS references only in comments; AdMob manifest entries live solely in `app/src/play/AndroidManifest.xml`. Inclusion Policy: "proprietary tracking or advertising libraries … are strictly forbidden … must implement … a build flavour that does not require these dependencies" — https://f-droid.org/en/docs/Inclusion_Policy/ |
| 1.2 | License | ✅ PASS | `LICENSE` = MIT; metadata `License: MIT` (`fastlane/metadata/android/en-US/fdroid.yml:5`). SPDX-recognized. https://f-droid.org/en/docs/Build_Metadata_Reference/#License |
| 1.3 | No prebuilt binaries / binary blobs | ✅ PASS | `git ls-files` shows only source, docs, scripts + `gradle/wrapper/gradle-wrapper.jar`. `fdroidserver/scanner.py:985-991` explicitly whitelists `gradle-wrapper.jar`/`gradlew`/`gradlew.bat` — the wrapper jar does **not** trip the binary scanner. No `.so`, `.a`, `.class`, `.jar` (other than wrapper), no APK/AAB tracked. https://f-droid.org/en/docs/Inclusion_Policy/ |
| 1.4 | Flavor build command | ✅ PASS | `gradle: - foss` (fdroid.yml:36-41) → fdroidserver runs `assembleFossRelease`. Confirmed twice: Build Metadata Reference "gradle:" field ("The Gradle task will be `assemble<flavors>Release`") — https://f-droid.org/en/docs/Build_Metadata_Reference/#build_gradle; and source `fdroidserver/build.py:533-539` (`gradletasks += ['assemble' + flavors_cmd + 'Release']`). Matches the docs' own example (`gradle: - full` → `assembleFullRelease`). |
| 1.5 | Gradle version resolution | ✅ PASS | F-Droid does **not** execute the app's `gradlew`; it runs the build-server wrapper `gradlew-fdroid` (`fdroidserver/common.py:204`; `build.py:690`). `gradlew-fdroid` parses `gradle/wrapper/gradle-wrapper.properties` from cwd **and parent dirs** (`gradlew_fdroid.py:351-359`), so our repo-root wrapper (`gradle/wrapper/gradle-wrapper.properties:3` → `gradle-8.11.1-bin.zip`) is honored even when building in a subdir. The downloaded distribution is checksum-verified against the Gradle transparency log (`gradlew_fdroid.py:344`, `411-422`) — this is why no `distributionSha256Sum` in the wrapper is required (still best practice to add one). https://gitlab.com/fdroid/gradlew-fdroid |
| 1.6 | `subdir: app` | ❌ **FAIL (build break)** | `common.prepare_source()` sets `root_dir = build_dir + build.subdir` (`fdroidserver/common.py:2610-2613`) and Gradle runs with `cwd=root_dir` (`build.py:696`). Our Gradle **project root is the repo root**: `settings.gradle.kts`, `build.gradle.kts`, `gradlew`, `gradle.properties` are all at repo root; `app/` contains only the `:app` module. Running Gradle in `app/` finds no `settings.gradle.kts` (undefined-build mode) and — critically — no `gradle.properties`, so `android.useAndroidX=true` (`gradle.properties:5`) is never loaded → hard error for any AndroidX app. **Fix: delete the `subdir: app` line** (or restructure the repo to put the Gradle root in `app/`). |
| 1.7 | `commit: main` | ❌ **FAIL (review blocker)** | Build Metadata Reference, `commit:` field: "**Note: Don't use branch name or tag name. The full commit hash should be used.**" — https://f-droid.org/en/docs/Build_Metadata_Reference/#build_commit. A moving branch means the build changes when HEAD moves (unreproducible), and every real fdroiddata recipe uses a full SHA (e.g. Agora: `commit: b57923350e8627eb0d9069e47529f7325e4139c5` — https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.newoether.agora.yml). **Fix: create tag `v1.0.0` at the release commit and use its full SHA.** |
| 1.8 | SDK/build-tools/NDK pinning | ✅ N/A (auto) | Current fdroidserver has **no `sdkVersion`/`buildTools` fields** (they were removed; only legacy `target:`/`ndk:` exist — https://f-droid.org/en/docs/Build_Metadata_Reference/#build_target). The build server pre-accepts SDK licenses and lets Gradle auto-install missing platforms/build-tools (`buildserver/provision-android-sdk:154`: "allow gradle to install newer build-tools and platforms"). `compileSdk 36` / `targetSdk 36` (`app/build.gradle.kts:11,16`) will be auto-installed. |
| 1.9 | JDK / AGP / Kotlin compatibility | ✅ PASS (verify in CI) | AGP 8.7.3 (`build.gradle.kts:2`) requires JDK 17 + Gradle 8.9+ (https://developer.android.com/build/releases/gradle-plugin); F-Droid's server image provides openjdk-17 and Gradle 8.11.1 via our wrapper. Kotlin 2.2.0 + KSP 2.2.0-2.0.2 (`build.gradle.kts:3-6`) resolve from Google Maven / Maven Central / Plugin Portal — all trusted sources per Inclusion Policy. Room 2.7.1 via KSP (`app/build.gradle.kts:131-134`) is a standard pattern on F-Droid. |
| 1.10 | R8 / minify | ✅ PASS | `isMinifyEnabled=true` + resource shrink (`app/build.gradle.kts:28-33`); `proguard-rules.pro` has the standard `-dontwarn`/`-keep` set incl. the documented tink/errorprone fix (line 8-10, see skill `android-byok-chat-apps` pitfalls). Repo CI builds `assembleFossRelease` green (workflow `build.yml:47-52`), which includes AGP's `lintVitalRelease` — so Android-Lint ERROR-severity issues (the only kind that fail a release build) are already excluded. |
| 1.11 | ABI splits (`arm64-v8a` only) | ⚠️ **RISK** | `splits { abi { isEnable = true; include("arm64-v8a"); isUniversalApk = false } }` (`app/build.gradle.kts:83-90`). F-Droid's guidance: "Currently there is no special support for ABI split. So every apk should be added as a build block, with different version code and build steps" + `VercodeOperation` — https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#setup-abi-split. Today the app has **no native code**, so AGP likely emits a single APK (verifiable only by building; we did not run gradle) — but the config is a latent trap: the moment any `.so` is added, F-Droid publishes an arm64-only APK. **Fix: gate the split behind a `-P` property (or delete it) so the F-Droid build always produces a universal APK** (~2-3 MB, per the project's small-APK law). Do **not** set `isUniversalApk=true` while splits are enabled — 2 APKs in the same flavor dir trigger fdroidserver's `More than one resulting apks found` BuildException (`build.py:762-764`). |
| 1.12 | gradle.properties heap | ⚠️ **RISK** | `org.gradle.jvmargs=-Xmx768m -XX:MaxMetaspaceSize=512m` + `kotlin.compiler.execution.strategy=in-process` + `workers.max=2` + `parallel=false` (`gradle.properties:4-11`) were tuned for a 4GB VPS. F-Droid's default build VM is **2 GB / 1 CPU** (`buildserver/Vagrantfile:8,11`; production farm overrides the memory via config). CI needs 2560m (`build.yml:45`) — 768m for Kotlin 2.2 + KSP + Compose + R8 is tight and may OOM or crawl. **Fix (insurance): `prebuild: echo 'org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8' >> gradle.properties`** in the fdroiddata recipe, then verify on fdroiddata's CI. Default `timeout: 7200` (https://f-droid.org/en/docs/Build_Metadata_Reference/#build_timeout) covers slow builds. |
| 1.13 | APK output discovery + version check | ✅ PASS | Flavor dir under `build/outputs/apk/foss/release/` is globbed (`build.py:740-769`, glob order `*-release-unsigned.apk` → `*-unsigned.apk` → `*.apk`). Unsigned release APK is exactly what F-Droid expects; versionCode 1 / versionName 1.0.0 are static in `app/build.gradle.kts:17-18` and will match the metadata (post-build check `build.py:819-825`). No `output:` field needed. |
| 1.14 | Signing / "Signed by unknown key" | ✅ PASS (myth addressed) | Release build stays **unsigned** when `KEYSTORE_FILE` env is absent (`app/build.gradle.kts:36-44`), which is F-Droid's requirement — fdroidserver signs with F-Droid's own key (docs: signing step, https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#what-to-expect). "Signed by unknown key" is not a rejection check; `AllowedAPKSigningKeys` only applies to operator-owned *binary* repos (https://f-droid.org/en/docs/Build_Metadata_Reference/#AllowedAPKSigningKeys). |
| 1.15 | Metadata field validity (`fdroid lint`) | ✅ PASS | Every key in `fdroid.yml` exists in the reference (Categories, License, AuthorName/Email, WebSite, SourceCode, IssueTracker, Changelog, AutoName, Summary, Description, RepoType, Repo, Builds{versionName,versionCode,commit,subdir,gradle}, AntiFeatures, ArchivePolicy, AutoUpdateMode, UpdateCheckMode, CurrentVersion/Code). `ArchivePolicy: 0` valid. Schema-warnings from editors are expected (see skill `play-launch-pack.md` fdroid.yml pitfalls). |
| 1.16 | Categories | ✅ PASS | `AI Chat` and `Internet` both exist in fdroiddata's category list (https://gitlab.com/fdroid/fdroiddata/-/blob/master/config/categories.yml). Peer precedent: Agora, a BYOK LLM client, is live under `AI Chat` (https://f-droid.org/packages/com.newoether.agora/). |
| 1.17 | Mandatory description files in app repo | ❌ **FAIL (review gap)** | F-Droid pulls descriptions/graphics from the app repo's fastlane structure: `fastlane/metadata/android/en-US/short_description.txt` (≤80 chars) and `full_description.txt` (≤4000) are **mandatory** ("All metadata files are completely optional, except for the short summary description as well as the longer full description") — https://f-droid.org/en/docs/All_About_Descriptions_Graphics_and_Screenshots/#fastlane-structure. Our `fastlane/metadata/android/en-US/` contains **only `fdroid.yml`** (no .txt, no `images/`, no `changelogs/`). Without icon + ≥1 graphic + changelog the app won't appear on the **Latest tab** (same page, "Latest tab criteria"). **Fix: add the files.** |
| 1.18 | Where the recipe actually lives | ❌ **FAIL as submission** | `fdroidserver` does **not** read `fastlane/metadata/android/en-US/fdroid.yml`. The authoritative recipe lives in the **fdroiddata** repo as `metadata/<applicationId>.yml` (docs: "The file name of the metadata … corresponds to the Application ID" — https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#understand-the-build-metadata). fdroidserver also merges a dot-prefixed **`.fdroid.yml` at the repo root** if present (`fdroidserver/metadata.py:713-729`), and fastlane **description files** (not the yml) from the app repo. Our FOSS APK's applicationId = `com.byoai.chat` + `.foss` suffix (`app/build.gradle.kts:14,62`) → **the fdroiddata file must be named `metadata/com.byoai.chat.foss.yml`**. Keep the fastlane `fdroid.yml` as documentation, but it is not the submission. |
| 1.19 | Description accuracy | ⚠️ **RISK (review friction)** | The description (fdroid.yml:27) says "One-time $4.99 Pro removes ads and unlocks premium features" and (line 28) "FOSS flavor available" — but the F-Droid build **is** the foss flavor: no ads, no Play Billing, no Pro purchase exists in it (foss `BillingRepository.kt:25-27` reports "Billing unavailable"). Reviewers read the description against the built APK and flag mismatches ("The application should be functional and implement all the features described in the description" — Inclusion Policy, Quality Control #2). **Fix: rewrite the F-Droid description** (BYOK, providers, offline Ollama/LAN, no ads, no tracking) and drop the Play-flavor monetization copy. |
| 1.20 | Tags / auto-update | ❌ **FAIL today** | `AutoUpdateMode: Version` + `UpdateCheckMode: Tags` (fdroid.yml:47-48) require tags; **the repo has zero tags** (`git tag` empty, single branch `main`). checkupdates scans `build.gradle.kts` files (source `fdroidserver/checkupdates.py:377`) and the docs say versionCode in the android block needs no special setup (https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#autoupdate-configuration), but with no tag there is nothing to check. **Fix: tag `v1.0.0`**; optionally add `UpdateCheckData: app/build.gradle.kts|versionCode\s*=\s*(\d+)||` (Kotlin DSL `versionCode = 1` needs the `\s*=\s*` regex — see reference: https://f-droid.org/en/docs/Build_Metadata_Reference/#UpdateCheckData) and verify with `fdroid checkupdates`. |
| 1.21 | AntiFeature: NonFreeNet | ✅ PASS (judgment call) | `NonFreeNet` = "promotes or depends entirely on a proprietary network service" (https://f-droid.org/en/docs/Anti-Features/#non-free-network-services). BYO AI connects to OpenAI-compatible cloud APIs by default, so declaring it is defensible and shows good faith. Note the peer Agora (same category, BYOK, 8+ providers) ships with **no** NonFreeNet flag — ours is the conservative choice. **Fix the reason wording** (fdroid.yml:44): "Play flavor uses Google Play Services for billing" describes the *other* flavor; state the network-service rationale instead. |
| 1.22 | Cleartext network security config | ✅ PASS (note) | `network_security_config.xml:4` allows cleartext for LAN Ollama — legitimate for a BYOK/LAN app (skill: "Cleartext only if needed for LAN Ollama"). This is not a rejection criterion; Android Lint's `InsecureBaseConfiguration` is a **warning**, and only lint ERRORS fail `lintVitalRelease` (already green in repo CI). No action needed. |
| 1.23 | Reproducible build | ✅ PASS (not required) | "Reproducible builds are not a requirement for apps being on F-Droid" — https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#setup-reproducible-build. Optional future work; the peer Agora does it via `Binaries:` + `postbuild` (zipalign), see its metadata. |
| 1.24 | Dependency provenance | ✅ PASS | All deps come from Google Maven / Maven Central (androidx, compose BOM, okhttp, kotlinx, room, coil, jsoup, security-crypto) — all in the trusted-repos allowlist (Inclusion Policy: "Maven Central, Google Maven, OSS Sonatype, …"). `settings.gradle.kts:1-14` declares no other repos. |

---

## 2. Exact fixes needed (ordered)

1. **Tag the release**: `git tag v1.0.0 <release-sha> && git push origin v1.0.0` (repo currently has no tags — required by `UpdateCheckMode: Tags` and by reviewers).
2. **Fix the build recipe** (apply to the fdroiddata metadata; mirror into `fastlane/metadata/android/en-US/fdroid.yml` as documentation):
   ```yaml
   Builds:
     - versionName: 1.0.0
       versionCode: 1
       commit: <full-SHA-of-the-v1.0.0-tag>     # NOT "main" — no branch names (Build Metadata Reference #build_commit)
       gradle:
         - foss                                    # → gradle assembleFossRelease (verified, build.py:533-539)
   ```
   **Remove `subdir: app`** — the Gradle project root is the repo root (`settings.gradle.kts`, `gradle.properties` live there); with `subdir: app` Gradle runs in `app/` where no settings/gradle.properties exist → `android.useAndroidX` never loads → build fails.
3. **Add the mandatory + recommended fastlane files** in the app repo:
   - `fastlane/metadata/android/en-US/short_description.txt` (≤80 chars)
   - `fastlane/metadata/android/en-US/full_description.txt` (≤4000 chars; **rewrite** — remove "Pro removes ads"/"FOSS flavor available" copy that is false for this build)
   - `fastlane/metadata/android/en-US/images/icon.png` (+ `featureGraphic.png`, `phoneScreenshots/1.png`, `2.png`)
   - `fastlane/metadata/android/en-US/changelogs/1.txt` (≤500 chars; filename = versionCode)
   These come from the app repo automatically (no MR needed for content changes) and unlock the Latest tab.
4. **Author the fdroiddata metadata**: fork https://gitlab.com/fdroid/fdroiddata, add `metadata/com.byoai.chat.foss.yml` (applicationId of the foss APK = `com.byoai.chat.foss` — `app/build.gradle.kts:14,62`). Content: Categories [AI Chat, Internet], License MIT, AutoName BYO AI, RepoType git, Repo `https://github.com/flamingspade1995-coder/litechat-android`, the fixed Builds block, AntiFeatures [NonFreeNet] with corrected reason, AutoUpdateMode Version, UpdateCheckMode Tags, optional `UpdateCheckData: app/build.gradle.kts|versionCode\s*=\s*(\d+)||`, optional `prebuild: echo 'org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=768m -Dfile.encoding=UTF-8' >> gradle.properties` (heap insurance, #1.12).
5. **Kill the ABI-split trap**: gate `splits { abi { … } }` (`app/build.gradle.kts:83-90`) behind a property, e.g. `if (project.hasProperty("abiSplit")) { … }`, and never pass `-PabiSplit` on F-Droid → universal APK for F-Droid, arm64-only still available for sideload/Play splits. (Alternative: delete the block — the APK is ~2-3 MB.)
6. **(Optional) `distributionSha256Sum`** in `gradle/wrapper/gradle-wrapper.properties` for supply-chain hygiene; not required (server verifies via transparency log).
7. **(Optional) Reproducible build** later: `Binaries: https://github.com/flamingspade1995-coder/litechat-android/releases/download/v%v/app-foss-arm64-v8a-release.apk` + `postbuild` alignment, like Agora. Not a requirement.

---

## 3. Submission checklist (step by step)

1. Apply fixes 1-5 above; commit; push. Confirm CI green (workflow runs `assembleFossRelease`).
2. Fork `gitlab.com/fdroid/fdroiddata`; branch `com.byoai.chat.foss`.
3. Create `metadata/com.byoai.chat.foss.yml` (content per §2.4). The metadata file name **must** be the built APK's applicationId `com.byoai.chat.foss` — not `com.byoai.chat` and not `fdroid`.
4. (Recommended) `fdroid lint com.byoai.chat.foss` + `fdroid checkupdates --allow-dirty com.byoai.chat.foss` locally (or in the buildserver container per the Quick Start) to catch YAML/field issues before review.
5. Push and open the MR to `fdroiddata` (label "New App"). **fdroiddata's GitLab CI pipeline builds the app from your metadata automatically** — a green pipeline is the first real proof the recipe works (https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#test-the-metadata).
6. Watch the MR for packager questions; reply fast (reviewers verify license, source, no binaries, description accuracy, and run their own build — https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#application-review-process).
7. **Timeline expectations** (docs): new-app review in the submission queue is volunteer-paced — **days to weeks**; after the fdroiddata merge, **~24-48 hours** until the app appears in the main repo (build + human keystore step) — https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/#what-to-expect.
8. After inclusion: every future release = bump `versionCode`/`versionName`, tag `vN.N.N`; auto-update + your fastlane changelog drive the rest. (Keep the fastlane `fdroid.yml` updated as the documented recipe, but the fdroiddata copy is authoritative.)
9. Optional faster lane: submit the same foss APK to **IzzyOnDroid** meanwhile (days, same client compatibility — `docs/DISTRIBUTION-FOSS.md:18,63-70`).

---

## 4. Reference URLs

- Build Metadata Reference (commit note, gradle field, subdir, output, timeout, UpdateCheckData, AllowedAPKSigningKeys): https://f-droid.org/en/docs/Build_Metadata_Reference/
- Inclusion Policy (proprietary SDK ban, prebuilt binaries, quality control): https://f-droid.org/en/docs/Inclusion_Policy/
- Submitting Quick Start Guide (example recipe, ABI split, tags, autoupdate, reproducible, review process, 24-48h): https://f-droid.org/en/docs/Submitting_to_F-Droid_Quick_Start_Guide/
- Descriptions, Graphics, Screenshots (fastlane structure, mandatory short/full description, Latest tab): https://f-droid.org/en/docs/All_About_Descriptions_Graphics_and_Screenshots/
- Anti-Features (NonFreeNet): https://f-droid.org/en/docs/Anti-Features/
- fdroidserver source (build.py, common.py, metadata.py, scanner.py, checkupdates.py, Vagrantfile): https://gitlab.com/fdroid/fdroidserver
- gradlew-fdroid (wrapper resolution + transparency-log checksum): https://gitlab.com/fdroid/gradlew-fdroid
- fdroiddata categories: https://gitlab.com/fdroid/fdroiddata/-/blob/master/config/categories.yml
- Peer precedent — Agora (MIT, AI Chat, BYOK, full-SHA commits, reproducible postbuild): https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.newoether.agora.yml and https://f-droid.org/packages/com.newoether.agora/
- AGP release notes (AGP 8.7 ↔ Gradle 8.9+ ↔ JDK 17): https://developer.android.com/build/releases/gradle-plugin
- F-Droid category page (AI Chat): https://f-droid.org/categories/ai-chat/

---

*Appendix A — key repo file:line evidence*
- `app/build.gradle.kts:11,16,17-18` compileSdk/targetSdk 36, versionCode 1 / versionName 1.0.0
- `app/build.gradle.kts:14,62` applicationId `com.byoai.chat` + foss suffix `.foss`
- `app/build.gradle.kts:52-64,139-140` flavor dimensions; GMS deps play-only
- `app/build.gradle.kts:83-90` ABI split arm64-only, no universal
- `app/build.gradle.kts:36-44` conditional signing (unsigned when no keystore env)
- `settings.gradle.kts:16-17` root project LiteChat, include(":app") → Gradle root = repo root
- `gradle.properties:4-11` 768m heap, in-process Kotlin, workers.max=2
- `gradle/wrapper/gradle-wrapper.properties:3` Gradle 8.11.1, no distributionSha256Sum
- `app/src/foss/…/{AdMobLazyInit,BillingRepository,BannerAd}.kt` GMS-free stubs
- `app/src/play/AndroidManifest.xml:9-18` AdMob app-id + provider strip (play only)
- `app/src/main/AndroidManifest.xml:5-11,23-25` permissions; no GMS entries
- `fastlane/metadata/android/en-US/fdroid.yml:35-48` Builds (commit: main, subdir: app, gradle foss), AntiFeatures, AutoUpdate/UpdateCheck
- `.github/workflows/build.yml:47-52` CI builds assembleFossRelease (proof lintVital passes)
