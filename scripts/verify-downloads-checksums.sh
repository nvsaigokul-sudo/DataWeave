#!/usr/bin/env bash
# Verify SHA256SUMS.txt entries under downloads/ for files that exist in the repo checkout.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/downloads"
if [[ ! -d "$ROOT" ]]; then
  echo "downloads/ not found at $ROOT" >&2
  exit 1
fi

fail=0
checked=0
skipped=0

while IFS= read -r -d '' sums_file; do
  sums_dir="$(dirname "$sums_file")"
  while read -r line || [[ -n "$line" ]]; do
    line="${line%%#*}"
    line="$(echo "$line" | xargs)"
    [[ -z "$line" ]] && continue
    hash="${line%% *}"
    file="${line#* }"
    [[ -z "$file" || "$file" == "$hash" ]] && continue
    target="$sums_dir/$file"
    if [[ ! -f "$target" ]]; then
      echo "SKIP (not in repo): $target"
      skipped=$((skipped + 1))
      continue
    fi
    if head -c 40 "$target" 2>/dev/null | grep -q 'git-lfs.github.com'; then
      echo "FAIL $target is a Git LFS pointer (run: git lfs pull). Do not distribute or install pointer files." >&2
      fail=1
      continue
    fi
    actual="$(sha256sum "$target" | awk '{print $1}')"
    if [[ "$actual" != "$hash" ]]; then
      echo "FAIL checksum $target (expected $hash, got $actual)" >&2
      fail=1
    else
      echo "OK $target"
      checked=$((checked + 1))
    fi
  done < "$sums_file"
done < <(find "$ROOT" -name 'SHA256SUMS.txt' -print0)

echo "Verified $checked file(s); skipped $skipped missing artifact(s)."
exit "$fail"
