#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [[ -f "$ROOT_DIR/.env" ]]; then
  set -a
  source "$ROOT_DIR/.env"
  set +a
fi

if [[ -z "${GEMINI_API_KEY:-}" ]]; then
  echo "Error: GEMINI_API_KEY is required." >&2
  echo "Set it in backend/gemini-api/.env or export it before starting." >&2
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "Error: Maven (mvn) is not installed." >&2
  exit 1
fi

exec mvn spring-boot:run
