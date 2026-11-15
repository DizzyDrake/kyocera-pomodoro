# v0.2.1 preview — bounded physical Kyocera trial

## Confirmed baseline: v0.1.11 (owner report, November 15)

- Installed on Kyocera Dura. Countdown works and an alert can be selected.
- A one-minute timer completed with phone closed, with audible completion and vibration both confirmed.
- UI defects: center key couldn't exit minutes editing, lower sound control inaccessible because the settings view did not scroll.
- Manual next-stage behavior occurred as designed; the automatic-session request prompted this candidate.

## Preview trial: do not uninstall working v0.1.11

1. Obtain GitHub Actions **debug preview** (`org.roarin.durapomodoro.preview`), confirm two distinct icons, launch the Preview app. Verify classic preset label and Focus stage 1/8. No other installed app should change.
2. **Keypad and settings:** enter Settings, navigate to each manual duration; use numeric keys or ±1 buttons, long-press to change by 5, press center to exit. Scroll down using D-pad until **Choose completion sound** is fully visible; select a tone. Cancel/Save behavior for sound is unchanged from v0.1.11.
3. **Custom editor:** Edit custom session; add and remove stages, reorder with up/down, set a one-minute Focus / one-minute Short break / one-minute Focus / one-minute Long break sequence; save. Select Custom automatic from the main mode chooser (or Reset if already selected).
4. **Open-phone automatic transition:** with Auto-advance and Auto-start both enabled, start custom stage 1. On completion, check sound/vibration, stage 2 begins without touching the phone, and the stage number/color and remaining time update. Continue all four stages; confirm it stops after final break, with no automatic restart.
5. **Closed-phone:** repeat with the phone closed throughout the first-to-second stage boundary. Check for completion sound/vibration AND whether stage 2 actually begins counting down on the closed device. Reopen and verify the expected stage and remaining time.
6. **Manual alternatives:** Auto-advance ON + Auto-start OFF should select next stage and wait. Auto-advance OFF + Auto-start ON should stay on the completed stage until Next; test the latter to verify independence.
7. **Pause/Reset:** pause during a later stage, reopen the app, verify saved remaining time and stage; Resume. Reset full session should return to stage 1 without auto-start. Change custom template mid-session; current active sequence should remain pinned until Reset/choose profile.
8. **Classic default:** verify the eight-step order is exactly `25/5/25/5/25/5/25/15` with Focus/Short/Long types. For a quick test, inspect the displayed sequence or change to a short custom plan; don't wait two hours just to validate programming.
9. **Reboot recovery (optional later):** with a running test timer, reboot and confirm the correct stage, alarm and notification. This may depend on Android device power settings and exact alarms.

## Report useful evidence

Version/APK filename, whether preview app installed alongside the original, current settings, which stage transition you tested, whether phone open/closed, what sound/vibration did, whether the next stage actually started, and any exact GitHub Actions compile error lines.

An Android Studio/Gradle build and actual installed v0.2.1 trial have **not** yet been performed here; the prior v0.1.11 phone evidence does not establish new automatic-stage reliability.

## v0.2.1 correction-focused checks (not yet device-verified)

- **Delayed alarm catch-up:** During a 4×one-minute fully automatic custom sequence, close the phone. If Android delivers a late alarm, the next countdown should reflect the **original stage boundaries**. A sufficiently late alarm may skip stages whose entire planned time has passed; it should post **one aggregated completion notification**, rather than replaying every missed tone. Record actual observed alert timing separately: Android Doze still controls delivery.
- **Ready manual duration:** Select Manual, Reset, change Focus from 25 to 1 minute in Settings, Save, and verify the ready display becomes 01:00 immediately. Then pause a separate running timer, change the Focus duration, and verify the paused remainder **does not change**. Reset should then pick up the new duration.
- **Background UI polling:** The screen must update after reopening, but no 500 ms UI refresh should run while the Activity is stopped. Test closing/reopening during an automatic stage and after an overdue stage.
- **Signing:** The preview remains `org.roarin.durapomodoro.preview`. A new Actions debug build may have a different signing certificate from a previously installed preview; do not uninstall the working original to resolve an update error.

The classic **25–5–25–5–25–5–25–15** sequence totals **130 minutes**.
