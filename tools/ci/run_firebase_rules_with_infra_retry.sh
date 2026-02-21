#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

if [[ $# -eq 0 ]]; then
  set -- \
    ./gradlew \
    :app:connectedDebugAndroidTest \
    "-PandroidTestClass=com.example.teamcompass.FirebaseRulesEmulatorTest" \
    --stacktrace
fi

FIREBASE_PROJECT="${FIREBASE_PROJECT:-demo-teamcompass}"
FIREBASE_CONFIG="${FIREBASE_CONFIG:-firebase.json}"
FIREBASE_LOG="${FIREBASE_LOG:-firebase-emulators.log}"

firebase emulators:start \
  --project "$FIREBASE_PROJECT" \
  --config "$FIREBASE_CONFIG" \
  --only auth,database \
  --quiet > "$FIREBASE_LOG" 2>&1 &
EMULATORS_PID=$!

cleanup() {
  kill "$EMULATORS_PID" >/dev/null 2>&1 || true
  wait "$EMULATORS_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT

for _ in $(seq 1 60); do
  if (echo > /dev/tcp/127.0.0.1/9000) >/dev/null 2>&1 && \
     (echo > /dev/tcp/127.0.0.1/9099) >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

(echo > /dev/tcp/127.0.0.1/9000) >/dev/null 2>&1
(echo > /dev/tcp/127.0.0.1/9099) >/dev/null 2>&1

./tools/ci/run_connected_with_infra_retry.sh "$@"
