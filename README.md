# NaukriAutomator

Windows desktop utility that refreshes the "last updated" date on one or more Naukri.com profiles by automating login → net-zero Resume-headline edit → resume rename + re-upload → **logout + browser close**.

**Author:** Adikarthik Gupta C B
**Version:** 0.1.0

> **2026-07-16 alignment pass.** Selectors + flow shape re-verified against a live Naukri browser session. Highlights: modal-based Resume-headline edit (`#resumeHeadlineTxt` inside `form[name='resumeHeadlineForm']`), resume card at `#lazyAttachCV` with `[data-title='download-resume']` icon, sniffed download URL under `/cloudgateway-mynaukri/resman-aggregator-services/v1/users/self/profiles/<hash>/resume`, single shared `.success-message-container` toast waited on after every save, direct-nav logout to `/nlogin/logout` with drawer-based `[data-type='logoutLink']` fallback, and browser context explicitly closed at end of LOGOUT step. Smart resume-rename preserves existing date patterns in the filename (e.g. `Arpitha S 15.07.2026 yahoo.pdf` → `Arpitha S 16.07.2026 yahoo.pdf`). Mock-naukri templates rewritten to mirror. See `docs/real-naukri-dom-2026-07-16.md`.

---

## Features

- Upload an Excel of Naukri emails OR enter them manually as chips
- One common password for the batch (in-memory only; never persisted)
- Toggle: run browser visibly (default) or headless
- Toggle: log in manually per account (auto-detects dashboard, 5-min timeout)
- Per-account live progress, retry policy (spec §5), and per-run CSV + JSON + log reports

---

## Prerequisites (for building from source)

| Requirement | Version |
|---|---|
| Windows | 10 / 11 |
| Java | 17 (Temurin/Azul recommended — path used in this project: `C:\Users\e182114\.jdks\azul-17.0.10`) |
| Node.js | 20 LTS |
| Maven | 3.9+ |
| PowerShell | 5.1+ |

---

## Build

```powershell
.\build\build.ps1
```

Outputs:

- `dist\NaukriAutomator Setup 0.1.0.exe` — NSIS installer (~397 MB)
- `dist\NaukriAutomator-0.1.0-win.zip` — zipped app folder (~472 MB)

The pipeline downloads a bundled JRE 17, installs Playwright Chromium, packages the Spring Boot backend JAR and React frontend, then invokes electron-builder. Each step is idempotent.

### Which one to use

Both ship the exact same app; the difference is only how it lands on disk:

| Distribution | Best for | One-time cost | Every launch |
|---|---|---|---|
| **NSIS installer** | Users who want a Start-menu + Desktop shortcut | ~22 s silent install | **~12 s** to first window |
| **ZIP** | Portable install / USB stick / no admin rights | ~38 s unzip anywhere | **~11 s** to first window |

Both modes read the bundled JRE + Playwright + backend jar directly from the installed / extracted location — nothing is re-extracted on launch. The ~11–12 s floor is JVM cold-start + Spring Boot context load; the shipped `NaukriAutomator-0.1.0-portable.exe` (self-extractor) from earlier builds is deliberately not produced anymore because it was re-extracting 397 MB to `%TEMP%` on every launch (60–120 s).

---

## Run tests

```powershell
.\build\test.ps1            # full: BE + Mock + FE + Electron + E2E
.\build\test.ps1 -SkipE2E   # everything except E2E
```

Individual layers:

```powershell
# Backend (Java 17 required)
$env:JAVA_HOME = 'C:\Users\e182114\.jdks\azul-17.0.10'
mvn -f backend\pom.xml verify

# Mock Naukri server
mvn -f mock-naukri\pom.xml test

# Frontend (Vitest)
npm --prefix frontend run test:ci

# Electron
npm --prefix electron test

# E2E (requires prior -Variant E2E build)
npm --prefix e2e install
npx --prefix e2e playwright install chromium
npm --prefix e2e test
```

---

## Reports layout

For each run the app writes under `<outputFolder>\<runTimestamp>\`:

| File | Contents |
|---|---|
| `report.csv` | Per-account rows: email, status, resume_old_name, resume_new_name, duration_ms |
| `report.json` | Same data plus step timings |
| `logs\<email>.log` | Plain-text per-account log |
| `screenshots\<email>.png` | Only for FAILED accounts |

---

## Manual login mode

Enable the toggle on the Setup screen. The browser opens; you complete login (including OTP or CAPTCHA). The app auto-detects the dashboard URL and resumes automation.

- **Continue now** — resumes automation immediately after you have logged in.
- **Skip this account** — marks the account as SKIPPED and moves on.
- **Timeout:** 5 minutes. An account that times out is marked `REQUIRES_MANUAL` in the report.

Manual-login mode forces the browser to run visibly; the headless toggle is ignored for affected accounts.

---

## Non-goals (v0.1.0)

- No CAPTCHA solving
- No automated OTP handling — use Manual login mode instead
- No credential persistence (password is in-memory only)
- No parallel accounts (sequential only)
- No cloud sync, telemetry, or auto-update

---

## Architecture (short)

Electron main → spawns bundled Java 17 + `naukri-be.jar` as a child process on a random localhost port → REST + WebSocket → Playwright Chromium (bundled). The renderer communicates with the backend exclusively over `http://127.0.0.1:<port>`. No traffic leaves the machine except to `naukri.com` via the Playwright browser.

See `docs/superpowers/specs/2026-07-14-naukri-utility-design.md` for the full design spec.

---

## Documentation

| File | Purpose |
|---|---|
| `docs/superpowers/specs/2026-07-14-naukri-utility-design.md` | Approved design spec |
| `docs/superpowers/plans/2026-07-14-naukri-utility.md` | Implementation plan |
| `docs/testing.md` | Test-layer breakdown |
| `docs/real-naukri-dom-2026-07-16.md` | Confirmed live-Naukri DOM reference (every selector traces back to a fragment here) |
| `COMPLETION_SUMMARY.md` | Project completion summary and review outcomes |

---

*Built by Adikarthik Gupta C B*
