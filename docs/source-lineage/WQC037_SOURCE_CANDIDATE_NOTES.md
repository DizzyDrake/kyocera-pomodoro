# WQ-C-20261114-037 — v0.1.7 source-only experimental candidate

Input: v0.1.6 WQA481 SHA-256 996b3fccbfb7f602adfa884355916d40101b67c2d6ebe5129c4c8aa3c644a1b7. Preserved unchanged.

Change: On successful completion state commit, store `completionNotificationPending` in the same SharedPreferences transaction as end=0. If notifications are enabled, retry delivery on ordinary Activity launch or BOOT_COMPLETED even when no timer remains active. After `NotificationManager.notify()` returns, clear pending using commit(). RuntimeException retains the pending flag for later retry. Disabled notifications create no pending delivery.

Semantics: at-least-once attempt, not exactly-once. A crash after notification posting but before clearing may repeat sound/vibration on relaunch; the fixed notification ID replaces the prior card. Notification permission/system channel suppression can silently prevent user-visible delivery even when notify() returns normally. A later completion may overwrite an earlier pending marker. Activity and receiver state transitions are not serialized; this change does not resolve concurrency, exact-alarm permission, scheduling failure warning, or failed preference-commit handling elsewhere. No compile or handset test.

Suggested tests: inject notify() exception and relaunch, boot with end=0 and pending=true, failed pending-clear commit, notifications disabled, repeated broadcast, concurrent Start/receiver transitions. Source-only candidate, not an accepted release.
