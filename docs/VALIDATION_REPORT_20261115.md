# Dura Pomodoro v0.2.0 — source validation, November 15, 2026

## Positive checks performed in the packaging environment

- Baseline source: unpacked actual GitHub-buildable v0.1.11 full-project ZIP (SHA-256 `20ed92b6f04366834c5e020e9a3fcb43c3e31c149a431561c032e46b645b1b8e`). Historical `.github` workflow, package identity, receiver migration and original retained records were inspected.
- `SessionPlan.java` compiled with `javac --release 8` (the code's source/target compatibility level). Pure Java test harness executed **65 assertions**, including the precise classic sequence, custom read/write, end-of-session stop, independent advance/start options and invalid data handling.
- Python packet/unit suite: **13 tests passed** after checksum manifest creation.
- All four Android Java app sources (MainActivity, TimerReceiver, BootReceiver, SessionPlan) **compiled with javac against 36 synthetic temporary Android class stubs**. This checks source syntax and approximate method/type contracts; it is **NOT** a real Android SDK build and does not prove framework API correctness.
- GitHub Actions YAML parses, preserved Java 17 / Gradle 7.6.4 / SDK 33 source and signature job, added Java session tests, and unique preview debug artifact identifier.
- `SHA256SUMS.txt` is refreshed over every shipped file except itself and tested during fresh extraction. ZIP integrity and full/overlay comparisons were checked separately when packages were created.

## Important boundaries

**Not performed:** Android SDK Gradle compile, GitHub Actions execution, installation of v0.2, actual clock/notification/alarm test with phone closed, signature comparison with installed v0.1.11, reading or changing owner's live GitHub repository (GitHub connector returned no accessible repo). App stays a **source-tested candidate**.

## Physical test priority

1. Install the new `org.roarin.durapomodoro.preview` debug build beside the working v0.1.11 app; keep the old app installed.
2. Run a four-stage 1-minute custom session, screen first open and then closed. Confirm sound/vibrate AND that next stages actually start automatically.
3. Navigate Settings/custom editor with only D-pad and center key; confirm bottom sound picker is reachable.
4. Review manual advance/start settings and final-stage session stop.

The owner can later choose to test an in-place signed release after its certificate is verified to match the installed app. Do not uninstall the working original to bypass a signature error.
