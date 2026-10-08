#!/usr/bin/env bash
set -euo pipefail

source "$(dirname "$0")/paths.sh"

base="${1:-}"
head="${2:-HEAD}"

if [ -z "$base" ] || ! git cat-file -e "$base^{commit}" 2>/dev/null; then
  echo "backend=true"
  echo "frontend=true"
  exit 0
fi

touches() {
  [ -n "$(git diff --name-only "$base" "$head" -- "$@")" ]
}

echo "backend=$(touches "${BACKEND_PATHS[@]}" && echo true || echo false)"
echo "frontend=$(touches "${FRONTEND_PATHS[@]}" && echo true || echo false)"
