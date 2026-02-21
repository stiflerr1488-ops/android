#!/usr/bin/env bash
set -euo pipefail

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 <command> [args...]"
  exit 2
fi

LOG_DIR="${INFRA_RETRY_LOG_DIR:-app/build/outputs/androidTest-results/connected/infra-retry}"
mkdir -p "$LOG_DIR"

LAST_LOG_FILE=""

run_once() {
  local attempt="$1"
  shift
  local log_file="${LOG_DIR}/attempt_${attempt}.log"
  echo "[infra-retry] attempt ${attempt}: $*"
  set +e
  "$@" 2>&1 | tee "$log_file"
  local status=${PIPESTATUS[0]}
  set -e
  LAST_LOG_FILE="$log_file"
  return "$status"
}

is_infra_failure() {
  local log_file="$1"
  if grep -Eiq "device .+ not found|device offline|AdbCommandRejectedException|No connected devices" "$log_file"; then
    return 0
  fi
  if grep -Eiq "INSTALL_FAILED_[A-Z0-9_]+" "$log_file" && \
     grep -Eiq "transport|device|offline|not found|adb" "$log_file"; then
    return 0
  fi
  return 1
}

if run_once 1 "$@"; then
  echo "[infra-retry] success on first attempt"
  exit 0
fi
first_status=$?

if is_infra_failure "$LAST_LOG_FILE"; then
  echo "[infra-retry] infrastructure failure signature detected, retrying once"
  if run_once 2 "$@"; then
    echo "[infra-retry] success on retry"
    exit 0
  fi
  second_status=$?
  echo "[infra-retry] retry failed"
  exit "$second_status"
fi

echo "[infra-retry] non-infrastructure failure detected, no retry"
exit "$first_status"
