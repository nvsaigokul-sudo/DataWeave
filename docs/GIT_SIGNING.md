# Git commit signing for DataWeave

GitHub **Verified** on a commit means the signature checks out **and** the signing key is registered for the GitHub user that matches the commit **author email**. This doc explains what we observed on `nvsaigokul-sudo/DataWeave`, how to fix future commits, and what Cloud Agents can and cannot do.

See also [GIT_IDENTITY.md](./GIT_IDENTITY.md) for name and email.

## What we saw on `main` (2026-10-24)

Investigation used local `git show --show-signature` and the GitHub REST API (`commit.verification` on each SHA).

| Commit | Author email | Signature present? | API `verified` | API `reason` |
|--------|----------------|-------------------|----------------|--------------|
| `74c7193` | `ruthwikuniversity@gmail.com` | No | `false` | `unsigned` |
| `bfb3e0f` (example) | `cursoragent@cursor.com` | Yes (SSH) | `true` | `valid` |
| `45f8fb8` | `nvsaigokul@gmail.com` | Yes (SSH) | `false` | `unknown_key` |
| `b09dc7d` | `nvsaigokul@gmail.com` | Yes (SSH) | `false` | `unknown_key` |

**Takeaways**

1. **`45f8fb8` and `b09dc7d` are not unsigned.** GitHub stores an SSH signature on both. Local `git show --show-signature` may print “No signature” on Cloud Agent VMs until `gpg.ssh.allowedSignersFile` is configured; trust the API payload when debugging GitHub’s badge.
2. **`unknown_key` means the SSH public key that signed the commit is not on the GitHub account tied to the author email** (`nvsaigokul@gmail.com` → `nvsaigokul-sudo`). Public API showed **no SSH signing keys** on that account (`GET /users/nvsaigokul-sudo/ssh_signing_keys` → `[]`).
3. **Older “Verified” agent commits** use the same Cursor-managed ed25519 key (`…IPqiJsBjAsv4KymedFcUR891X1lgC90DW8yMtjcHJ/p0`) but author **`cursoragent@cursor.com`**, which GitHub accepts as **valid** for that platform key.
4. This is **not** “email not on GitHub” (author email is correct), **not** GPG, and **not** missing signature on those two SHAs—it is **SSH signing with a key that `nvsaigokul-sudo` has not uploaded**.

## Recommended setup for `nvsaigokul-sudo`

Pick **one** signing method and register the **public** key at [GitHub → Settings → SSH and GPG keys → Signing keys](https://github.com/settings/keys) (SSH signing) or [GPG keys](https://github.com/settings/keys).

Use a **personal** key you control. **Do not** copy Cloud Agent ephemeral private keys into the repo or environment secrets.

### Option A — SSH commit signing (recommended)

On your **local machine** (or a trusted environment where you generate keys):

```bash
# 1. Generate a dedicated signing key (no passphrase optional but less secure)
ssh-keygen -t ed25519 -C "nvsaigokul@gmail.com" -f ~/.ssh/dataweave_signing -N ""

# 2. Add the PUBLIC key to GitHub → Signing keys
cat ~/.ssh/dataweave_signing.pub
# Paste at https://github.com/settings/ssh/new → Key type: Signing Key

# 3. Git config
git config --global gpg.format ssh
git config --global user.signingkey "$(cat ~/.ssh/dataweave_signing.pub)"
git config --global commit.gpgsign true

# 4. Identity (must match verified email on nvsaigokul-sudo)
git config --global user.name "Sai Gokul N V"
git config --global user.email "nvsaigokul@gmail.com"
```

Optional: `~/.ssh/config`

```sshconfig
Host github.com
  IdentityFile ~/.ssh/id_ed25519          # authentication
  IdentityFile ~/.ssh/dataweave_signing   # if git uses ssh for signing via agent
```

Verify after a signed commit and push:

```bash
git commit -S -m "test: verify signing"
git push github main
gh api repos/nvsaigokul-sudo/DataWeave/commits/$(git rev-parse HEAD) \
  --jq '.commit.verification | {verified, reason}'
# Wait until verified == true before relying on the badge
```

### Option B — GPG signing

```bash
gpg --full-generate-key
# Real name: Sai Gokul N V
# Email: nvsaigokul@gmail.com (must be verified on GitHub)

gpg --list-secret-keys --keyid-format=long
# Use the sec key id

git config --global user.signingkey <KEYID>
git config --global commit.gpgsign true
git config --global gpg.program gpg

# Upload public key
gpg --armor --export nvsaigokul@gmail.com
# Paste at https://github.com/settings/gpg/new
```

Unset SSH format if switching from SSH: `git config --global --unset gpg.format`.

## Cursor Cloud Agents

On a typical Cloud Agent VM:

- `commit.gpgsign=true`, `gpg.format=ssh`, and `user.signingkey` point at **Cursor’s managed** `cursor-git-ssh-keygen` key.
- Default author is **`Cursor Agent <cursoragent@cursor.com>`** → commits can show **Verified** with `reason: valid` even when the human owner did not add a key.
- If the agent sets author to **`nvsaigokul@gmail.com`** (correct identity) but still signs with the Cursor key → GitHub returns **`unknown_key`** until **your** signing public key is on `nvsaigokul-sudo`.

**Signed commits as the owner from Cloud Agents** require one of:

1. **Human commits locally** with your SSH or GPG key (simplest), or  
2. **Environment secrets** (Cursor environment / personal setup): store **your** private signing key and `allowed_signers` / GPG material in secrets the environment injects at boot—never commit private keys to the repo. The platform must not generate and publish new keys on your behalf without you uploading the public half to GitHub.

Do **not** treat agent `Verified` on `cursoragent@cursor.com` as proof that **your** identity commits are signed correctly.

## Disabling unwanted `Co-authored-by` trailers

Cursor injects co-authors via a **managed hook outside the repo** (audit: `commit-msg.cursor.co-author` under the agent `core.hooksPath`). The repo cannot remove that hook by itself.

**What you should do**

1. Cursor **Settings** → Agent / Git (wording may vary) → disable co-author injection or remove **`ruthwikuniversity`** from the co-author list.
2. Link the DataWeave Project to **`nvsaigokul-sudo/DataWeave`**, not a mirror under another account, so the hook does not pick the wrong user.
3. For one-off agent commits where policy allows: `git commit --no-verify` skips hooks (including co-author); use sparingly and never to bypass signing policy you intend to enforce.

**Repo-only options (limited)**

- Document policy here and in [GIT_IDENTITY.md](./GIT_IDENTITY.md); `.github/` cannot change Cursor’s global `hooksPath`.
- A repo `.githooks/prepare-commit-msg` only runs if contributors set `git config core.hooksPath .githooks` locally—it **does not override** Cursor’s agent hook path on Cloud VMs. We document only; no fake “disable hook” file that agents ignore.

Details: [HISTORY_REWRITE_PLAN.md](./HISTORY_REWRITE_PLAN.md) § “Cursor co-author hook”.

## Retroactive “Verified” on old commits

GitHub verification is computed from **commit objects** (hash, author, tree, **signature**). You **cannot** make an existing SHA verified without changing the commit:

- **Amend / rebase / filter-repo** → new SHAs, new signatures on each rewritten commit, then force-push (see rewrite plan—**owner approval required**).
- **Upload a key today** does **not** retroactively verify old commits that were signed with a different key or were unsigned.

**Do not force-push `main`** to “fix” verification without following the backup and approval steps in [HISTORY_REWRITE_PLAN.md](./HISTORY_REWRITE_PLAN.md).

## Quick diagnostic commands

```bash
# Local (may need allowedSignersFile for SSH)
git show --show-signature <sha>

# Authoritative for GitHub UI badge
gh api repos/nvsaigokul-sudo/DataWeave/commits/<sha> \
  --jq '.commit.verification'

# Keys on your account (authenticated)
gh auth refresh -h github.com -s admin:ssh_signing_key
gh ssh-key list

# Secret keys on this machine
gpg --list-secret-keys
```

Only claim **Verified** when the API returns `"verified": true`.
