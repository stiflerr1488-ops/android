#!/usr/bin/env bash
set -euo pipefail

fail=0

check_not_tracked() {
  local path="$1"
  if git ls-files --error-unmatch "$path" >/dev/null 2>&1; then
    echo "::error file=$path::File must not be tracked in git."
    fail=1
  fi
}

check_glob_not_tracked() {
  local pattern="$1"
  local matches
  matches="$(git ls-files -- "$pattern")"
  if [[ -n "$matches" ]]; then
    echo "::error::Tracked files matched forbidden pattern: $pattern"
    echo "$matches"
    fail=1
  fi
}

check_not_tracked "app/google-services.json"
check_not_tracked "google-services.json"
check_not_tracked '$null'
check_glob_not_tracked "build_log.txt"
check_glob_not_tracked "*_compile_log*.txt"
check_glob_not_tracked "*_test_log.txt"
check_glob_not_tracked "_shots/*"

if [[ "$fail" -ne 0 ]]; then
  echo "Repository hygiene checks failed."
  exit 1
fi

echo "Repository hygiene checks passed."
