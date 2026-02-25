#!/usr/bin/env bash
set -euo pipefail

echo "[1/7] Repo hygiene"
./tools/ci/check_repo_hygiene.sh

echo "[2/7] PendingIntent flags"
python3 ./tools/ci/check_pending_intent_flags.py

echo "[3/7] Android guardrails"
python3 ./tools/ci/check_android_guards.py

echo "[4/7] Deprecated APIs"
python3 ./tools/ci/check_deprecated_apis.py

echo "[5/7] Release hardening"
python3 ./tools/ci/check_release_hardening.py

echo "[6/7] Proguard rules"
python3 ./tools/ci/check_proguard_rules.py

echo "[7/7] Secret scan"
python3 ./tools/ci/check_secrets.py

echo "All guard checks passed."
