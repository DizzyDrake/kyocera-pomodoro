# Dura Pomodoro v0.2.1 — correction validation

**Status:** SOURCE-TESTED / REAL GITHUB BUILD PENDING / PHONE TEST PENDING. The earlier `VALIDATION_REPORT_20261115.md` remains historical evidence for v0.2.0.

## Corrections

1. `SessionTimeline.resolve()` derives future stage boundaries from the old absolute deadline, with bounded catch-up over any overdue automatic stages. Manual modes do not skip stages. Receiver persists completed progress before scheduling the next alarm. One notification covers any accumulated completions. The original classic sequence adds up to **130 minutes**.
2. `TimerEditPolicy` and an explicit `timerPaused` flag protect paused/running manual remainders, but replace the stored remainder with the newly selected duration when Settings is saved for a ready manual timer. For pre-v0.2.1 installations without the flag, partial remainders are conservatively assumed paused.
3. 500 ms Activity display polling now runs only while the Activity is started; when resumed it checks an overdue alarm before refreshing UI. AlarmReceiver continues to operate when UI is stopped.

## Validation and limits

- `tests/run_java_tests.sh`: tests the existing planner, anchored timeline and ready/paused policy.
- `python3 -m unittest discover -s tests -v`: tests source contracts and integrity of the SHA256 manifest.
- Host `javac --release 8` with temporary Android API stubs: source syntax/type contract approximation only.
- YAML parse, shell syntax, ZIP CRC, full vs overlay comparisons and SHA256 manifest need verification on the final artifact.
- **Not done:** genuine Android SDK compilation or signed APK, CI execution in owner's repository, real background timing, actual notification sound in v0.2.1, device sleep and partial-update trials. Android Doze can still delay audible notifications.
