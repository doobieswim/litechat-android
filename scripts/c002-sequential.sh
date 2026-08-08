#!/usr/bin/env bash
# C-002 sequential flavor verification — one R8 pass at a time (memory-safe on 4GB box).
set -uo pipefail
cd /opt/data/workspace/byok-chat-android
source /opt/data/android-env.sh
LOG=/opt/data/logs/c002-verify.log
: > "$LOG"
log() { echo "[$(date -u +%H:%M:%S)] $*" | tee -a "$LOG"; }

log "=== [1/3] assembleFossRelease ==="
./gradlew :app:assembleFossRelease --console=plain >> "$LOG" 2>&1
F1=$?
log "foss exit=$F1"

log "=== [2/3] assemblePlayRelease ==="
./gradlew :app:assemblePlayRelease --console=plain >> "$LOG" 2>&1
P1=$?
log "play exit=$P1"

log "=== [3/3] unit tests (both flavors) ==="
./gradlew :app:testFossReleaseUnitTest :app:testPlayReleaseUnitTest --console=plain >> "$LOG" 2>&1
T1=$?
log "test exit=$T1"

log "=== APK outputs ==="
find app/build/outputs/apk -name "*.apk" -exec ls -la {} \; >> "$LOG" 2>&1
log "=== DONE: foss=$F1 play=$P1 test=$T1 ==="
exit $((F1 + P1 + T1))
