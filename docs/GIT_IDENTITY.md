# Git identity for DataWeave

All commits to https://github.com/nvsaigokul-sudo/DataWeave should be attributed to the **nvsaigokul-sudo** GitHub account, not aliases used on other mirrors.

## Recommended git config (local)

```bash
git config --global user.name "Sai Gokul N V"
git config --global user.email "nvsaigokul@gmail.com"
```

Use an email shown on https://github.com/settings/emails that is verified for **nvsaigokul-sudo**.

Alternative (GitHub noreply, id `243963610`):

```bash
git config --global user.email "243963610+nvsaigokul-sudo@users.noreply.github.com"
```

Per-repo only:

```bash
git config user.name "Sai Gokul N V"
git config user.email "<your verified email>"
```

Verify before pushing:

```bash
git var GIT_AUTHOR_IDENT
git log -1 --format=fuller
```

## Cursor Cloud Agents

- Link the project to the **nvsaigokul-sudo** GitHub repo; avoid pushing only via mirrors under other accounts.
- In Cursor Agent git settings, disable or update co-author trailers so **ruthwikuniversity** is not appended (`commit-msg.cursor.co-author` on agent VMs).
- Cloud agents may use `Cursor Agent <cursoragent@cursor.com>` as author; that is fine if unwanted co-authors are not added.

## GitHub Actions

Workflows in this repo do not create commits today. If you add commit steps, set `user.name` and `user.email` explicitly (for example `github-actions[bot]` or the repo owner).

## Do not

- Do not use `ruthwikuniversity@gmail.com` or the `ruthwikuniversity` GitHub username on this repository.

## Existing history

Past commits may show **Co-authored-by: ruthwikuniversity** from Cursor’s agent hook, or **kakumani Ruthwik** on the initial commit. Fixing that requires an explicit history rewrite (not done automatically). See the internal audit notes or ask before any force-push.
