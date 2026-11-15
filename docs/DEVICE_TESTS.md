# First Kyocera Dura phone trial checklist

The source has not yet passed an Android SDK build, emulator run or handset test. After an Actions **successful** run and installation:

- Open app. Verify work stage is green, breaks red, and initial durations are 25/5/15.
- Use the keypad/D-pad to focus and activate Start, Pause, Reset, Next stage and Settings. Confirm the clock is legible on the real display.
- Change the durations and reopen Settings. Try invalid duration, correct it, then Save; verify dialog behavior.
- Start a one-minute timer with app visible; ensure exactly one completion alert with correct sound/vibration.
- Repeat with display off; verify wake/notification and manual next stage.
- Pause a partially elapsed timer, change vibration only, Save, resume; confirm the paused remainder persists.
- While running, kill/reopen the app; ensure the timer continues with the original end time.
- With a timer pending, reboot the device and verify timer re-scheduling; check with the real system clock.
- Try choosing a completion sound then Cancel Settings. **Known source behavior:** the new sound remains saved. Decide if future behavior should be transactional.
- If testing update installation, keep app package ID and signing certificate unchanged; verify previous durations survive the upgrade.

Record phone model, Android version, APK signer identity, relevant system notification permissions/settings, screen on/off, what you observed, and any logcat output when a failure occurs. This packet does not modify Dumbplayer or its signing key.
