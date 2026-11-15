# WQA473 v0.1.2 generation-specific PendingIntent candidate
Derived from original v0.1.1 ZIP. Source-only; no Android compilation or device validation.
- Generation encoded in Intent data URI AND extra; both checked by receiver. Alarm cancellation reconstructs old identity.
- Boot re-arm uses committed generation; legacy active deadline migrates on boot only.
- Start writes generation/deadline with commit before scheduling. Pause after deadline leaves receiver completion pending.
- Reset/next cancel old identity; old broadcast cannot finalize new generation.
Known gaps: Activity onCreate does not migrate a legacy active deadline until boot; a legacy alarm firing without a boot migration will be ignored. Alarm cancellation and preference commit are not atomic; process-death, notification failure and concurrent receiver/UI race cases remain. No actual Android PendingIntent semantics or exact-alarm permissions tested. This is not a release candidate.
