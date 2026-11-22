# Dura Pomodoro

![Dura Pomodoro app icon: a red tomato with a dark-green plus-sign leaf on a light-green background](repo-assets/dura-pomodoro-tomato-icon.png)

**A simple focus and break timer made for real buttons.**

Dura Pomodoro is an Android Pomodoro timer designed around the physical keypad of the Kyocera Dura. Set a timer, take your breaks, and keep your routine moving without relying on a touchscreen.

**Designed and built by Aurora and GPT-6.**

## What it does

- **Manual timer:** start, pause, resume, and reset a focus or break countdown.
- **Classic Pomodoro session:** four 25-minute focus periods, with 5-minute breaks between them and a final 15-minute long break: **25–5–25–5–25–5–25–15**. The sequence ends after the last break (130 minutes total).
- **Custom sessions:** build and save a sequence of focus periods and breaks, adjust the duration of each step, and arrange the steps in the order you want.
- **Auto-advance and auto-start:** choose whether the app moves to the next session step automatically and whether that step starts on its own.
- **Sounds and vibration:** choose a completion sound and vibration settings.
- **Physical-key navigation:** use the D-pad and center button to move through settings, change durations, and control the timer. The settings and session editor scroll on the small Kyocera screen.
- **Closed-phone timer support:** alarms are scheduled independently of the visible screen. Android battery management may still delay notifications and sounds.

## Getting started

1. Install an APK built from this project for your phone.
2. Open **Dura Pomodoro** and choose **Manual**, **Classic automatic**, or **Custom automatic**.
3. Set your durations, completion sound, and automatic progression preferences.
4. Start the timer. Use the phone's keypad to pause, resume, or reset as needed.

## Build from GitHub

The project includes a GitHub Actions workflow at [`.github/workflows/build.yml`](.github/workflows/build.yml).

1. Open the repository's **Actions** tab and select **Build Dura Pomodoro**.
2. Run the workflow or open the workflow run triggered by your latest commit.
3. Download the appropriate APK artifact from the successful run.

The **debug** build of v0.2.1 is a separate app named *Dura Pomodoro Preview* (`org.roarin.durapomodoro.preview`), allowing it to coexist with the older installed app. The optional **personal signed release** retains the package ID `org.roarin.durapomodoro`. For an in-place update, Android requires the same package ID, compatible version code, and matching signing certificate. Never uninstall an existing app merely to bypass a signature mismatch without first preserving any settings you need.

For build, signing, and installation details, see [`docs/SIGNING_AND_UPDATES.md`](docs/SIGNING_AND_UPDATES.md) and [`docs/UPGRADING_INSTALLED_APP.md`](docs/UPGRADING_INSTALLED_APP.md). For an on-phone test checklist, see [`docs/DEVICE_TESTS.md`](docs/DEVICE_TESTS.md).

## Compatibility and project status

Dura Pomodoro targets the Kyocera Dura keypad experience on Android 10. The v0.2.1 source requires Android 8.0 (API 26) or newer.

The saved v0.2.1 source passed offline source and Java tests, but its GitHub build and physical-device behavior have not been independently verified in this documentation update. The previously installed v0.1.11 timer was reported working on the Kyocera, including a closed-phone completion alert.

The original source packet contains no explicit redistribution license. A public software license can be added once its terms are selected; source availability alone does not grant general reuse rights.
