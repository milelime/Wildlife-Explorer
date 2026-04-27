#!/usr/bin/env bash
# Compile (and optionally run) from any Unix-like shell (macOS, Linux, WSL).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT="$ROOT/build/classes"
SRC="$ROOT/src/main/java"

mkdir -p "$OUT"
mapfile -t FILES < <(find "$SRC" -name '*.java')
if [[ ${#FILES[@]} -eq 0 ]]; then
  echo "No Java sources under $SRC" >&2
  exit 1
fi
javac -d "$OUT" "${FILES[@]}"

case "${1:-}" in
  run)
    exec java -cp "$OUT" wildlifeexplorer.Main
    ;;
  "")
    echo "Built classes -> $OUT"
    echo "Run the app:  $0 run"
    ;;
  *)
    echo "Usage: $0 [run]" >&2
    exit 1
    ;;
esac
