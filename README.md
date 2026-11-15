# Dura Pomodoro — GitHub Actions build packet

**Integrated from the existing Kyocera Dura Pomodoro v0.1.11 source candidate (November 15, 2026).** Standalone Android app with package ID `org.roarin.durapomodoro`; it does not replace or modify Dumbplayer. All three Java application source files and the Android manifest are byte-for-byte unchanged from v0.1.11. Gradle metadata, Actions packaging, tests and instructions were integrated here.

## Fastest build: GitHub Actions (no local Android SDK needed)

1. Extract this ZIP on your computer. The extracted project's **contents** must go into the **root** of a GitHub repository, including the hidden `.github` folder. Do not upload only the ZIP as a single repository file, or put these files one directory too deep.
2. Push the extracted files to a new or existing GitHub repository on branch `main` or `master`. Make sure the repository's **Actions** setting permits workflows. This action also runs on push, or can be run from **Actions → Build Dura Pomodoro → Run workflow**.
3. After the run completes, open its **Artifacts** section and download `DuraPomodoro-debug-apk`. Extract that artifact ZIP to obtain `app-debug.apk`.
4. For reliable upgrades without uninstalling and losing saved timer settings, configure **optional personal signing** before your first installation (see `docs/SIGNING_AND_UPDATES.md`) and install the `DuraPomodoro-personal-signed-apk` artifact instead.

No GitHub secrets are needed for a debug APK. The Actions workflow installs Java 17, Android SDK platform 33, build-tools 33.0.2 and Gradle 7.6.4. It builds a debug APK, checks the packet's source, and optionally builds/signs a release APK if four private secrets are present. Gradle is installed by the Action, so there is deliberately **no Gradle wrapper JAR** in this repository.

### App behavior retained

- Work/short/long stages; initial durations 25/5/15 minutes, customizable 1–240 minutes
- Green work background, red breaks, large clock, buttons usable through D-pad focus
- Start/resume, Pause, Reset and Next stage (manual transitions)
- AlarmManager exact RTC_WAKEUP timer completion and reboot rescheduling
- Completion notification/sound chooser, silent selection, optional vibration
- Stage duration and paused progress saved in Android SharedPreferences

### Versions and build settings

- `applicationId`: `org.roarin.durapomodoro` — stable identity
- Minimum SDK 26; target SDK 29 for the owner's Android 10 Kyocera; compile SDK 33
- Android Gradle Plugin 7.4.2, Gradle 7.6.4, JDK 17
- Android version code 11, version name 0.1.11 (the older source candidate's generic versionCode 1 was replaced; Java behavior unchanged)

## Important limitations

This is a **build-ready source handoff**, not a verified successful GitHub run or phone-tested APK. The environment used to assemble this packet has no Android SDK or external repository access, so native Android compilation is pending. The 0.1.11 source has an acknowledged Settings Cancel behavior: selecting a ringtone saves it immediately, even when the Settings dialog is later cancelled. The owner has not requested that behavior to change. See `docs/DEVICE_TESTS.md` for practical first-use checks. Debug APKs use a runner-generated debug signing identity that may differ across runs; persistent personal signing protects update compatibility.

**Source traceability:** original v0.1.11 docs and workflow in `docs/source-lineage/`; original archive and checksums in `docs/SOURCE_AND_VERIFICATION.md`.
