# Dura Pomodoro v0.2.1 — corrected automatic-session candidate

**November 15, 2026 · GitHub Actions source update built from the installed v0.1.11 repository-root packet**

This retains the original Android app's Java package (`org.roarin.durapomodoro`), Android 10 compatibility, GitHub workflow, and sound/vibration alarm mechanism. It implements two automatic session styles and fixes phone-keypad editing issues. No part of Dumbplayer is modified.

## What is new

- **Classic automatic:** exactly **25 Focus → 5 Short break → 25 Focus → 5 Short break → 25 Focus → 5 Short break → 25 Focus → 15 Long break**. It **stops** after the final break. No silent repeat.
- **Custom automatic:** edit, save, reorder, add, or remove any combination of Focus, Short break and Long break steps. Each step has its own 1–240-minute duration; up to 64 steps (a practical UI limit, not an owner-mandated restriction). Changes to the saved custom template apply to the **next Reset or profile selection** so a running plan cannot change underneath an alarm.
- **Automatic stage controls:** independent settings for Auto-advance and Auto-start. Both are enabled by default for an automatic session. Auto-start only applies when Auto-advance is enabled. Manual timer mode remains available.
- **Hardware keypad:** editable minutes have visible ±1 buttons (long press ±5); press D-pad center or Enter to exit an entry field. Settings and the custom session editor have their own scrollable panels, keeping the bottom sound selector reachable.
- **Closed-phone continuity:** each completed stage is acknowledged in persistent preferences, followed by the next scheduled exact alarm. Failure to schedule is recoverable on next launch/boot. Completion notifications still use the chosen ringtone and vibration settings.

**v0.2.1 corrections:** automatic deadlines are anchored to the original stage boundary, even when a phone idle alarm is late. Multiple stages elapsed during device sleep are resolved in one state change with one combined completion notification; Android may still delay actual alert delivery. A ready manual timer now immediately reflects newly saved minutes, but a running or explicitly paused timer keeps its remainder. UI refreshing stops in `onStop()` and resumes in `onStart()`. The specified eight-stage classic session totals **130 minutes** (100 focus + 15 short breaks + 15 long break), not 120.

**The owner previously confirmed v0.1.11 completed a one-minute timer with the phone closed and delivered sound/vibration. v0.2.1 has not yet been installed or physically tested.**

## Important: this is an UPDATE to the existing GitHub project

The owner already uploaded the entire **v0.1.11 GitHub folder's contents** into an existing GitHub repository. **Do not create another repository or upload a second top-level project directory.** The files here are already arranged as repository-root files.

There are two archives:

- **Full-project ZIP** — complete source tree for backup or a clean checkout. Extract and copy its **contents**, not the containing directory, into the existing repository root, overwriting corresponding files. Keep any separately added files unless you deliberately want to change them.
- **Changed-files overlay ZIP** — simplest update. Extract and upload the files **using the relative paths shown inside the ZIP**, replacing the existing repository files at those same locations. Include `.github/workflows/build.yml` and the new `SessionPlan.java` and Java test. Do not leave old and new versions next to each other or nest a `DuraPomodoro...` folder within the repository.

A GitHub web folder upload or a local Git checkout both work as long as the resulting repository paths are correct. The Actions workflow still runs on `main` and `master`, on push and manual dispatch; under **Actions → Build Dura Pomodoro**.

## APKs and protection of the installed working app

**The default `DuraPomodoro-preview-debug-apk` is now a _separate preview app_** with application ID `org.roarin.durapomodoro.preview` and label **Dura Pomodoro Preview**. Installing it **will not overwrite or remove your working v0.1.11 app or its settings**. Both icons can coexist. This is intentional because GitHub-hosted debug builds normally have a different ephemeral signing certificate on each run.

The optional `DuraPomodoro-personal-signed-apk` is a **release** with the original application ID `org.roarin.durapomodoro` and version code **13** (v0.1.11 had version code 11). **Only install it as an in-place update if its signing certificate matches your currently installed app.** Otherwise Android will reject it; do not uninstall the working version just to get past a signature mismatch. See [docs/UPGRADING_INSTALLED_APP.md](docs/UPGRADING_INSTALLED_APP.md).

## Build with GitHub Actions

1. Merge the root-level update into your **existing** GitHub repo; commit/push the changes including hidden `.github`.
2. Go to **Actions → Build Dura Pomodoro → Run workflow**, or inspect the run automatically triggered by the push.
3. Workflow runs static Python checks and pure-Java session/timing tests first, then Gradle `:app:assembleDebug` using JDK 17, Gradle 7.6.4 and Android SDK/build-tools 33.
4. Download `DuraPomodoro-preview-debug-apk` from the run artifacts. Extract to get `app-debug.apk` (the **preview**, safe to install alongside v0.1.11).
5. Test automatic transitions on your phone with sound/vibration and closed lid, as described in `docs/DEVICE_TESTS.md`.

When configured with the original, **matching** personal signing identity, the workflow also produces `DuraPomodoro-personal-signed-apk`. No secrets are needed for preview builds.

## Tested / not tested

- Static packet checks, Java 8-compatible session planner compile/tests, YAML and shell workflow checks, source/ZIP/hash verification: performed by packaging agent; see `docs/SOURCE_AND_VERIFICATION.md`.
- **Not performed:** real Android SDK Gradle compile, GitHub workflow execution, emulator test, on-phone v0.2.1 trial, independent inspection of the owner's live GitHub repo. The linked GitHub connector returned no accessible repos; this update is based on the exact prior integrated ZIP and owner report about uploading the entire repo root.

The older v0.1.11 sound-picker choice still saves immediately even if Settings is cancelled afterward; it has **not** been silently altered here. The original v0.1.11 source and historical notes remain preserved under `docs/source-lineage/` and in the Library.
