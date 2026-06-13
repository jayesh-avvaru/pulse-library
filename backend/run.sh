#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

mkdir -p out
find src -name '*.java' -print0 | xargs -0 javac -d out
exec java -cp out com.pulselibrary.Main
