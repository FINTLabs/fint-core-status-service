#!/usr/bin/env bash
set -euo pipefail

source "$(dirname "$0")/paths.sh"

last_commit() {
  git log -1 --abbrev=7 --format=%h -- "$@"
}

echo "backend=$(last_commit "${BACKEND_PATHS[@]}")"
echo "frontend=$(last_commit "${FRONTEND_PATHS[@]}")"
