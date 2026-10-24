# DataWeave v0.3.0 — Windows owner acceptance guide

Use this guide **after** installing the **official v0.3.0 release** MSI. It turns scenarios **#1–#9** from [WINDOWS_RELEASE_ACCEPTANCE.md](./WINDOWS_RELEASE_ACCEPTANCE.md) into plain steps with clear pass/fail rules.

**Related:** [V0.3.0_RELEASE.md](./V0.3.0_RELEASE.md) · [VERSIONING.md](./VERSIONING.md) · [INSTALL.md](./INSTALL.md)

---

## 1. Which installer to use

| Field | Value |
|--------|--------|
| **Release** | [v0.3.0](https://github.com/nvsaigokul-sudo/DataWeave/releases/tag/v0.3.0) |
| **File** | **`windows-DataWeave-Setup.msi`** (same bits as `windows-DataWeave-1.3.0.msi` on the release page) |
| **SHA-256** | `6d631d33ac73112a314166ffa5bdff345d2ac269d6425863914f180319ea128a` |
| **Product version (in-app / Gradle)** | **0.3.0** |
| **Windows Apps & Settings version** | **1.3.0** (jpackage naming — not a downgrade from 0.3.0) |

**Before install:** Verify the downloaded file hash (PowerShell):

```powershell
Get-FileHash -Path "$env:USERPROFILE\Downloads\windows-DataWeave-Setup.msi" -Algorithm SHA256
```

The hash must match **`6d631d33…`** above. If it does not, do not install — you may have an old TEST artifact or a v0.2.9 (**1.2.9**) build from the wrong branch.

**Do not use** MSI zips named `TEST-windows-msi-3dbb41c…` or any installer showing version **1.2.9** when testing v0.3.0.

---

## 2. Prerequisites

- **Windows 10 or 11** (64-bit), desktop session (not RDP-only headless server unless you can see the GUI).
- **PostgreSQL** reachable on your machine (you previously used **PostgreSQL 18.x** — any supported PG version you use in daily work is fine).
- Optional: uninstall **DataWeave 1.2.9 (v0.2.9)** first if you want a clean test; upgrade-in-place is also supported (see §3).

---

## 3. Install, upgrade, and reinstall

### Fresh install (recommended for first v0.3.0 acceptance pass)

1. If an older DataWeave is installed: **Settings → Apps → DataWeave → Uninstall**.
2. Remove stale **Start Menu** shortcuts if they remain.
3. Install **`windows-DataWeave-Setup.msi`** from the v0.3.0 release page.
4. Launch **DataWeave** from the Start Menu (not `gradlew run`).

### Upgrade from v0.2.9 (1.2.9 MSI)

1. Install **v0.3.0** MSI **over** the existing 1.2.9 install (same upgrade path).
2. Local SQLite metadata should migrate **V1–V5** (backup runs before migrate).
3. **Re-enter the database password once** if you see “missing stored credential” — v0.2.9+ uses persistent Windows Credential Manager; older session-only vault entries may not survive.

### If launch fails

- Check **`%LOCALAPPDATA%\DataWeave\launch-error.log`** for the real Java error (the dialog “Failed to launch JVM” is generic).
- For v0.3.0, **`HeadlessException`** on normal launch should **not** recur if you use this official MSI (fix since v0.2.3). If it does, note the log in your results template.
- Optional: run **`DataWeave.exe --diagnose`** from the install folder and attach output to your report.

Default install folder: **`C:\Program Files\DataWeave\`**

---

## 4. Acceptance scenarios (#1–#9)

For each scenario, record **PASS**, **FAIL**, or **PARTIAL** and one sentence of evidence.

| # | What you do | Pass | Fail |
|---|-------------|------|------|
| **1** | **Install & first launch** — Install MSI, open app from Start Menu. Window appears within ~30s; no “Failed to launch JVM”; main shell visible (nav + workspace). | GUI opens | Dialog/crash/no window |
| **2** | **Restart & theme** — **Settings** → switch **Light / Dark / System**; close app fully; reopen. Theme matches last choice. | Persists | Resets or crash |
| **3** | **Connections** — Create/save profile (host, port, DB, user, password, SSL). Scroll to bottom: **Save**, **Test**, **Connect** all reachable. **Test** succeeds; **Connect** shows connected status. Disconnect and reconnect once. After full app restart, connect again (re-save password if recovery panel appears). | Full lifecycle works | Scroll blocked, cred dead-end, cannot connect |
| **4** | **Explorer** — After connect, expand **database → schema → table**. Expand **Columns**, **Primary keys**, **Foreign keys**, **Indexes** where present. Use **Refresh** if offered. Open a **table** and a **view** into the **Data** tab if you have a view. | Tree loads; no endless “Loading…”; metadata sections show data | Stuck Loading, empty tree, errors with no recovery |
| **5** | **Data grid** — Open a table with enough rows to paginate. Change **page size** (50/100/200), **Next/Previous**, **search**, **column filter**. Select cells; **Ctrl+C** (TSV) and **Ctrl+Shift+C** (CSV) copy to clipboard. Resize a column. **Cancel** if a long load is in progress. | Paging/filter/copy work; cancel stops spinners | Wrong data, freeze, copy empty |
| **6** | **Dataset Builder** — Add tables to canvas (drag or add control). Configure a **join**, join keys, **Preview** SQL/data. **Save**, close dataset, **reopen** from list. | Preview runs; save/reopen restores graph | Preview always errors; save lost |
| **7** | **SQL** — Run a **SELECT**; see results. Run an **INSERT** (or UPDATE) — must be **rejected** with clear message. Start a long **SELECT** and **Cancel**. | SELECT ok; write blocked; cancel works | Write runs; cancel ignored |
| **8** | **Projects** — Create project; link a dataset or query; **reorder** items; trigger **unsaved** dialog (edit then navigate away); **Open in Datasets** / **Open in SQL** from a link. | Links and navigation work | Buttons no-op; data lost |
| **9** | **Clean exit** — File → exit or close window; process ends (Task Manager: no stuck `DataWeave.exe`). Relaunch once more — app starts normally. | Clean exit & relaunch | Zombie process or crash on second launch |

---

## 5. Report your results

Copy the block below into email, chat, or a GitHub issue. **Do not leave sections blank** — use `NOT RUN` if you skipped a scenario.

```markdown
## DataWeave v0.3.0 — Windows owner acceptance

- **Tester:**
- **Date:**
- **MSI SHA-256 verified:** YES / NO
- **PostgreSQL version:**
- **Windows version:**

| # | Result (PASS/FAIL/PARTIAL/NOT RUN) | Notes |
|---|-------------------------------------|-------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
| 7 | | |
| 8 | | |
| 9 | | |

**Blockers for daily use (if any):**

**Screenshots / logs attached:** (launch-error.log, --diagnose)
```

Send results to your DataWeave project chat or open an issue on [GitHub](https://github.com/nvsaigokul-sudo/DataWeave/issues).

---

## 6. What happens after you report

- **All PASS (or approved PARTIAL):** v0.3.0 remains the supported release; docs will be updated to record acceptance date.
- **Any FAIL:** Engineering will reproduce on Windows CI where possible, fix root cause, and ship **v0.3.1** only for confirmed defects — not for cosmetic-only feedback unless agreed.

No test outcomes are assumed until you submit the table above.
