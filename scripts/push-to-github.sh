#!/data/data/com.termux/files/usr/bin/bash
# Push 91Writing Android to GitHub and trigger Actions APK build.
#
# Usage:
#   export GITHUB_TOKEN=ghp_xxxx
#   ./scripts/push-to-github.sh [username] [repo-name]
set -euo pipefail

USER_NAME="${1:-ahov520}"
REPO_NAME="${2:-91Writing-Android}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  if [[ -f "$HOME/.github_token_tmp" ]]; then
    GITHUB_TOKEN="$(cat "$HOME/.github_token_tmp")"
    export GITHUB_TOKEN
  fi
fi

if [[ -z "${GITHUB_TOKEN:-}" ]]; then
  echo "错误: 请设置 GITHUB_TOKEN"
  exit 1
fi

echo "目标: https://github.com/${USER_NAME}/${REPO_NAME}"

if command -v gh >/dev/null 2>&1; then
  echo "$GITHUB_TOKEN" | gh auth login --with-token 2>/dev/null || true
fi

if [[ ! -d .git ]]; then
  git init
  git checkout -b main 2>/dev/null || git branch -M main
fi

git add -A
git status
if ! git diff --cached --quiet; then
  git -c user.email="${GIT_EMAIL:-ahov520@users.noreply.github.com}" \
      -c user.name="${GIT_NAME:-ahov520}" \
      commit -m "feat: 91Writing Android shell + GitHub Actions APK build"
fi

git remote remove origin 2>/dev/null || true
git remote add origin "https://${GITHUB_TOKEN}@github.com/${USER_NAME}/${REPO_NAME}.git"

# Create repo if missing
if command -v gh >/dev/null 2>&1; then
  if ! gh repo view "${USER_NAME}/${REPO_NAME}" >/dev/null 2>&1; then
    gh repo create "${USER_NAME}/${REPO_NAME}" --public \
      --description "91写作 Android 版 — AI 智能小说创作工具（完整功能 WebView + GitHub Actions APK）" \
      --source=. --remote=origin --push || true
  fi
fi

git push -u origin main --force

echo ""
echo "完成！"
echo "  仓库:   https://github.com/${USER_NAME}/${REPO_NAME}"
echo "  Actions: https://github.com/${USER_NAME}/${REPO_NAME}/actions"
echo "  构建完成后在 Artifacts 下载 writing91-debug-apk"
