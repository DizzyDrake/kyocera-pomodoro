# Dura Pomodoro — source candidate v0.1
Standalone Android 10-compatible Pomodoro timer, designed for D-pad focus navigation. Red break and green work screens, editable work/short/long durations, start/pause/reset/next, completion notification and vibration toggles. Uses Android AlarmManager RTC_WAKEUP to schedule completion while the screen sleeps. No account/network permissions.

## GitHub Actions build
Upload this directory as a GitHub repository, then run **Actions → Build Dura Pomodoro → Run workflow**. Download `DuraPomodoro-debug-unsigned-for-release` artifact, extract `app-debug.apk`. Debug APK is automatically signed with the GitHub runner's *ephemeral debug key*, so future runs may not install over previous versions; use a stable private signing key for durable updates (not included). Package ID: `org.roarin.durapomodoro` (separate from Dumbplayer).

## Known limitations / testing
No APK built or device-tested in this run. Notification sound can be selected from the device ringtone picker, including silent. Android notification channels retain their settings after first creation: new sound/vibration combinations use separate channels; Android settings may override each channel. Notification only marks stage finished; next stage requires manual selection. The timer uses wall-clock time; manually changing clock time can alter remaining time. Boot receiver reschedules a pending timer after reboot, but exact-alarm delivery, D-pad focus, and lock-screen behavior require real-device testing. Do not treat this as accepted release.
