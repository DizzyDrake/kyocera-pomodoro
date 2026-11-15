# WQA493 — v0.1.9 source-only UI synchronization candidate
Derived from WQE140 v0.1.8 without altering alarm scheduling, receiver persistence, notification handling, or settings. In MainActivity.tick(), when persisted `end` is zero, read persisted `remaining` rather than leaving the previous in-memory countdown. This permits the visible clock to reflect receiver completion (00:00) without requiring Activity relaunch, and reflects paused/reset durations. The normal active-timer branch remains unchanged.

Concurrency caveat: WQA491 and WQE141 model a possible interleaving of independent reads and writes, but in this manifest Activity UI callbacks and BroadcastReceiver.onReceive are ordinarily dispatched on the same app main thread, with no separate process declared. The synthetic model is not an Android reproduction. This candidate does not claim cross-process synchronization, a race fix, or guaranteed notifications.

No Android SDK compilation, emulator, handset, or installed-state test. Requires device/build validation. Preserve v0.1.8 as source baseline.
