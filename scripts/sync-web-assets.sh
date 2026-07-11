#!/usr/bin/env bash
# Build Vue app and copy into Android assets (for local builds)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT/web"
npm ci || npm install
npm run build
rm -rf "$ROOT/android/app/src/main/assets/www"
mkdir -p "$ROOT/android/app/src/main/assets/www"
cp -a dist/. "$ROOT/android/app/src/main/assets/www/"
echo "Synced web/dist → android/app/src/main/assets/www"
ls -la "$ROOT/android/app/src/main/assets/www" | head
