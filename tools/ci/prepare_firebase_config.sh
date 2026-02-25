#!/usr/bin/env bash
set -euo pipefail

if [[ -f app/google-services.json ]]; then
  echo "Using checked-out app/google-services.json."
  exit 0
fi

if [[ -f app/google-services.json.example ]]; then
  cp app/google-services.json.example app/google-services.json
  echo "Prepared app/google-services.json from template."
  exit 0
fi

echo "::error::Missing app/google-services.json and app/google-services.json.example"
exit 1

