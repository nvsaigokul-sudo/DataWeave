# DataWeave Git history rewrite plan

> **DO NOT RUN WITHOUT EXPLICIT OWNER APPROVAL**
>
> This document describes a **planned** history rewrite. Nothing in this file executes automatically. Do not run `git filter-repo`, force-push, or delete tags until **nvsaigokul-sudo** (repo owner) confirms in writing.

## Goals

1. Remove **ruthwikuniversity** / **ruthwikuniversity@gmail.com** / **kakumani Ruthwik** from:
   - **Author** and **Committer** fields (1 commit today)
   - **`Co-authored-by:`** trailers (33 commits today)
2. Preserve **file trees**, **Git LFS** blob content, and **release artifacts** semantics (tags re-pointed to equivalent commits).
3. Leave a **recoverable backup** on GitHub and locally before any force-push.

## Target identity

| Field | Value |
|-------|--------|
| GitHub user | `nvsaigokul-sudo` |
| Name | `Sai Gokul N V` |
| Email (primary) | `nvsaigokul@gmail.com` |
| Email (alternative) | `243963610+nvsaigokul-sudo@users.noreply.github.com` |

**Policy for rewritten commits**

| Current pattern | Planned change |
|-----------------|----------------|
| `74c7193…` — Author/Committer `kakumani Ruthwik <ruthwikuniversity@gmail.com>` | Set Author **and** Committer to target identity above |
| 33 commits — `Cursor Agent <cursoragent@cursor.com>` + `Co-authored-by: ruthwikuniversity …` | Remove the `Co-authored-by` line (and any blank line left behind). **Optional:** also map `Cursor Agent` to `Sai Gokul N V` — decide before rewrite; default recommendation is **strip co-author only** so agent vs human attribution stays honest |
| `45f8fb8…` — already `Sai Gokul N V`, no Ruthwik trailer | No metadata change; **SHA still changes** because it sits on top of rewritten history |

See also [GIT_IDENTITY.md](./GIT_IDENTITY.md) and the internal audit (`dataweave-git-identity-audit-2026-10-24.md` in Project Context).

## Scope

- **Branch:** `main` on `https://github.com/nvsaigokul-sudo/DataWeave.git` (remote `github`)
- **Through HEAD:** `45f8fb8897f15591699224c0e30a95956ed27ca8` (2026-10-24)
- **Commit count:** 35 (was 34 at audit time; `45f8fb8` added `docs/GIT_IDENTITY.md` with correct identity)
- **Commits after `45f8fb8` on `github/main`:** none (HEAD confirmed)

Every commit from the root **`74c7193…`** through **`45f8fb8…`** will receive a **new SHA** after rewrite. Parent links change at the first rewritten commit, so **all subsequent SHAs change** even when tree and author metadata are unchanged.

## Old SHA → new SHA (mapping)

New SHAs **cannot be known** until `git filter-repo` (or equivalent) runs locally. After rewrite, record the mapping (e.g. `git log --reverse --format='%H %s'` on old backup branch vs new `main`).

| # | Old SHA (full) | Short | Identity / message work | New SHA (after rewrite) |
|---|----------------|-------|-------------------------|-------------------------|
| 1 | `74c7193d4c95ed636b8f8435e9ddf9668f007b5a` | `74c7193` | Fix author + committer | *TBD* |
| 2 | `bfb3e0f25e2ccb954bd0b6e9191c498871279855` | `bfb3e0f` | Strip `Co-authored-by` | *TBD* |
| 3 | `9164fce8f820a15896b7fdf8550f2a83d4842817` | `9164fce` | Strip `Co-authored-by` | *TBD* |
| 4 | `c8a7a82830bb0766bf328c4982b872cb6bd5f3b5` | `c8a7a82` | Strip `Co-authored-by` | *TBD* |
| 5 | `11d98f1d8d2fd73c4f0b2cddba91d3d136c18e41` | `11d98f1` | Strip `Co-authored-by` | *TBD* |
| 6 | `f015e053d16168f0b8dce12d6439abc8c96798f9` | `f015e05` | Strip `Co-authored-by`; **tag `v0.2.0` today** | *TBD* |
| 7 | `9c478519b1fe1bd9771a9b72ad3c347bf69b9987` | `9c47851` | Strip `Co-authored-by` | *TBD* |
| 8 | `20b36a086ba909a5c455ac00903f530fa2564d1e` | `20b36a0` | Strip `Co-authored-by` | *TBD* |
| 9 | `bb07ab93c034b6da5cedf5543c387da2c58761eb` | `bb07ab9` | Strip `Co-authored-by` | *TBD* |
| 10 | `1ee174b51d775bde4f8d8ef5b3054f621457238f` | `1ee174b` | Strip `Co-authored-by` | *TBD* |
| 11 | `10f007e0f795ff38fbf9a62e916af65fcb60add9` | `10f007e` | Strip `Co-authored-by` | *TBD* |
| 12 | `7b7fb5d401bb02b55103076792b728ae47d283dd` | `7b7fb5d` | Strip `Co-authored-by` | *TBD* |
| 13 | `7f67b1f80ec46108be6fca48649738f8b53c0317` | `7f67b1f` | Strip `Co-authored-by` | *TBD* |
| 14 | `17119e25841bb3383613c1e669f277378bde7233` | `17119e2` | Strip `Co-authored-by` | *TBD* |
| 15 | `4b4b84af0a8b9b678b9ad4c4595712453dd7e3d8` | `4b4b84a` | Strip `Co-authored-by` | *TBD* |
| 16 | `62cb7138b45ab4c4a61f8378337e5a7e2f894a7f` | `62cb713` | Strip `Co-authored-by` | *TBD* |
| 17 | `5e158bc2c5d28e463c6f4520ebeaac0f10124936` | `5e158bc` | Strip `Co-authored-by` | *TBD* |
| 18 | `4ecdba28c9fda18d8049474830e8d29e9490a5e1` | `4ecdba2` | Strip `Co-authored-by` | *TBD* |
| 19 | `600beb4b8c9c6d9332bc9e1c9fdcbdf7dff7383a` | `600beb4` | Strip `Co-authored-by` | *TBD* |
| 20 | `ce8e76c7edfca605ed83c48dbf124768eadeaea1` | `ce8e76c` | Strip `Co-authored-by` | *TBD* |
| 21 | `7354c4777c6c89060e76d16b2bdd3890e3e1949e` | `7354c47` | Strip `Co-authored-by` | *TBD* |
| 22 | `7f0d8b7f5b089f30a05eabafc3cb9e74ab0d2da5` | `7f0d8b7` | Strip `Co-authored-by` | *TBD* |
| 23 | `98cdb3284cb89f90242e7db028043dcee90dcf67` | `98cdb32` | Strip `Co-authored-by` | *TBD* |
| 24 | `cea12d6f1c919aaa03ca921325ef77680fa3d614` | `cea12d6` | Strip `Co-authored-by` | *TBD* |
| 25 | `2ac8c22f6242801c6c28c097b8ac3bb522470ad8` | `2ac8c22` | Strip `Co-authored-by` | *TBD* |
| 26 | `914a44d47acadfab9761df3cdada5308abd049c6` | `914a44d` | Strip `Co-authored-by` | *TBD* |
| 27 | `91bb60a211b0e70a7ca985c1431c8a4636433dfc` | `91bb60a` | Strip `Co-authored-by` | *TBD* |
| 28 | `34fe1fa1f34f8909ef298b22a04bea047c1a8ec7` | `34fe1fa` | Strip `Co-authored-by` | *TBD* |
| 29 | `24ad9558c5f48c7d822cdb9648eb7d2180a65020` | `24ad955` | Strip `Co-authored-by`; **tag `v0.2.1` today** | *TBD* |
| 30 | `dd7e2d3d6b211daed943757fd96923e82c31bd06` | `dd7e2d3` | Strip `Co-authored-by` | *TBD* |
| 31 | `26f138b9cbc5fac36763e51cb470c2e5d471c975` | `26f138b` | Strip `Co-authored-by` | *TBD* |
| 32 | `93f5f3a44270cbf02ff30aaeff9a6cce54d3c709` | `93f5f3a` | Strip `Co-authored-by` | *TBD* |
| 33 | `637e55bc5cabfe6ffa95141360def6cb55440fee` | `637e55b` | Strip `Co-authored-by` | *TBD* |
| 34 | `de1fe8761b35ae87e4cdcf09fca690883df1d65b` | `de1fe87` | Strip `Co-authored-by` | *TBD* |
| 35 | `45f8fb8897f15591699224c0e30a95956ed27ca8` | `45f8fb8` | No Ruthwik (pass-through metadata) | *TBD* |

**Expected new HEAD:** *TBD* (replaces `45f8fb8…`).

## Tags

| Tag | Points to (old SHA) | Short | After rewrite |
|-----|---------------------|-------|----------------|
| `v0.2.0` | `f015e053d16168f0b8dce12d6439abc8c96798f9` | `f015e05` | Move tag to **new** commit at same chronological position (#6) |
| `v0.2.1` | `24ad9558c5f48c7d822cdb9648eb7d2180a65020` | `24ad955` | Move tag to **new** commit at same chronological position (#29) |

Procedure (only after local rewrite verified):

```bash
# Example — use actual new SHAs from mapping table
git tag -f v0.2.0 <NEW_SHA_FOR_f015e05>
git tag -f v0.2.1 <NEW_SHA_FOR_24ad955>
git push github refs/tags/v0.2.0 refs/tags/v0.2.1 --force
```

Deleting old tag objects on the remote may require `git push github :refs/tags/v0.2.0` only if force-updating tags fails; prefer `git push --force` on the tag ref.

## Git LFS

Current LFS-tracked file (as of HEAD):

| Pointer in tree | LFS OID (short) |
|-----------------|-----------------|
| `downloads/DataWeave-Setup.msi` | `b613ec64d2…` |

**What stays the same:** LFS **pointer files** in each commit tree (version, oid, size) and the **blob OIDs** in Git LFS storage are unchanged by an identity-only rewrite, because `filter-repo` does not alter file contents unless you run content filters.

**After rewrite:** Clones and CI should run:

```bash
git lfs install
git lfs fetch --all github
git lfs checkout
```

Verify: `git lfs ls-files` on new HEAD matches backup branch; `git cat-file -p HEAD:downloads/DataWeave-Setup.msi` pointer identical to pre-rewrite.

Do **not** run destructive `git lfs migrate` unless intentionally moving objects between remotes.

## GitHub Releases

Releases **v0.2.0** and **v0.2.1** are tied to annotated/lightweight tags on GitHub. After re-tagging:

- GitHub usually follows the tag to the new commit; **release assets** (MSI/DMG/etc. uploaded to the Release) remain attached to the Release entity.
- Confirm on the Releases UI that each release still points at the intended tag and that generated release notes (if any) still make sense.
- If a release appears detached, use GitHub **Edit release** → set tag to the **new** tag target (may require deleting/recreating the tag on the same name first).

No release asset re-upload should be needed if only commit SHAs and messages change.

## Backup (mandatory before force-push)

### Remote backup branch on GitHub

```bash
git fetch github main
git branch backup/main-before-rewrite-20261024 github/main
git push github backup/main-before-rewrite-20261024
```

Use the actual date when executing (format `backup/main-before-rewrite-YYYYMMDD`).

### Local mirror bundle

```bash
git fetch github --tags
git bundle create dataweave-main-backup-20261024.bundle github/main refs/tags/v0.2.0 refs/tags/v0.2.1
# Optional: include backup branch
git bundle create dataweave-full-backup-20261024.bundle github/main backup/main-before-rewrite-20261024 refs/tags/v0.2.0 refs/tags/v0.2.1
```

Store the bundle off-machine (owner workstation, encrypted storage).

## Recommended tooling

Install [git-filter-repo](https://github.com/newren/git-filter-repo) (not `git filter-branch`).

### 1. Strip `Co-authored-by: ruthwikuniversity …` and fix `74c7193` author

Use a single **`--commit-callback`** (Python) on a fresh clone:

```python
# commit_callback.py — illustrative; test on a clone first
import re

TARGET_NAME = b"Sai Gokul N V"
TARGET_EMAIL = b"nvsaigokul@gmail.com"
RUTHWIK_EMAIL = b"ruthwikuniversity@gmail.com"

def commit_callback(commit, metadata):
    # Fix initial commit author/committer
    if commit.author_email == RUTHWIK_EMAIL or b"ruthwikuniversity" in commit.author_name.lower():
        commit.author_name = TARGET_NAME
        commit.author_email = TARGET_EMAIL
    if commit.committer_email == RUTHWIK_EMAIL or b"ruthwikuniversity" in commit.committer_name.lower():
        commit.committer_name = TARGET_NAME
        commit.committer_email = TARGET_EMAIL

    # Strip Co-authored-by lines mentioning ruthwik
    msg = commit.message
    lines = msg.split(b"\n")
    filtered = [
        ln for ln in lines
        if not re.match(rb"^Co-authored-by:\s*.*ruthwikuniversity", ln, re.I)
    ]
    commit.message = b"\n".join(filtered)
```

```bash
git clone https://github.com/nvsaigokul-sudo/DataWeave.git dataweave-rewrite
cd dataweave-rewrite
git filter-repo --commit-callback "$(cat commit_callback.py)" --force
```

Alternatively combine **`.mailmap`** for author remapping with **`--message-callback`** for trailer stripping.

### 2. Separate check for `74c7193`

After filter run:

```bash
git log --reverse --format=fuller -1
git log --format=fuller | rg -i ruthwik || echo "OK: no ruthwik in log"
```

### 3. Do not run yet on GitHub

Work only in the clone until verification passes.

## Verification checklist

- [ ] `git diff $(git rev-parse backup/main-before-rewrite-20261024) HEAD` → **empty** (trees identical at tips; compare via `git diff-tree` if SHAs differ: `git diff-tree backup/... HEAD` → empty)
- [ ] Better: for each old commit index `i`, `git diff-tree OLD_SHA NEW_SHA` → empty for all 35 pairs (same trees)
- [ ] `git log --format=fuller | rg -i 'ruthwik|kakumani'` → no matches
- [ ] `git lfs ls-files` unchanged; LFS smudge works on fresh clone
- [ ] Tags `v0.2.0` / `v0.2.1` on **new** SHAs; trees at those tags match pre-rewrite tags (`git diff-tree v0.2.0^{} backup/main-before-rewrite-20261024:f015e05` style check using saved old tag SHAs)
- [ ] Push to a **test branch** first (`rewrite-preview/main`) and run GitHub Actions CI green
- [ ] Owner explicit approval recorded (issue comment or email)

## Force-push procedure (owner approval only)

```bash
git push github main --force-with-lease
git push github refs/tags/v0.2.0 refs/tags/v0.2.1 --force
```

Use `--force-with-lease` so an unexpected remote update aborts the push.

Notify collaborators: re-clone or `git fetch github && git reset --hard github/main`.

## Rollback

1. `git push github backup/main-before-rewrite-20261024:main --force-with-lease`
2. Restore tags from backup bundle or re-point tags to old SHAs (`f015e05…`, `24ad955…`) from the backup branch
3. Verify Releases UI and LFS

## Cursor co-author hook (prevent recurrence)

The audit found Ruthwik trailers injected by Cursor’s **commit-msg** hook (not in repo):

- Hook directory (example on Cloud Agent VM): `/home/ubuntu/.cursor/agent-hooks/L3dvcmtzcGFjZQ/`
- File: `commit-msg.cursor.co-author`

**Disable or fix before any post-rewrite commits:**

1. Cursor **Settings** → Agent / Git → co-author or commit attribution → remove `ruthwikuniversity` or set to **nvsaigokul-sudo**
2. Link the DataWeave Project to `github.com/nvsaigokul-sudo/DataWeave`, not the `kakumani-ruthwik` mirror on `origin.cursor.com`
3. `git commit --no-verify` skips hooks for one-off commits but is **not** a long-term policy

Document day-to-day identity in [GIT_IDENTITY.md](./GIT_IDENTITY.md).

## Execution status

| Step | Status |
|------|--------|
| Plan documented | Done (this file) |
| Backup branch | **Not done** — waiting approval |
| filter-repo | **Not done** — waiting approval |
| Force-push | **Not done** — waiting approval |

**Waiting for explicit user/owner approval before any rewrite or force-push.**
