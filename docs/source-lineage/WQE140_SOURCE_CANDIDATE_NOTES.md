# WQE140 — v0.1.8 source candidate
Derived from WQC037 v0.1.7 ZIP (SHA256 76c495f5c41cd139530a5f52f160fa99718312b44344eac12f23bc70f362ad36).

Change only MainActivity Start/resume: after a successful persistence commit, catch synchronous alarm scheduling RuntimeException and warn without changing the committed deadline/generation. A subsequent Start/resume press while active invokes existing reconcileActive, retrying the same generation and deadline; launch/boot recovery remains unchanged. Failed commit does not schedule. This is a candidate, not an installed or Android-tested release.

Remaining concerns: asynchronous alarm delivery, notification durability/permission, concurrent receiver/activity state changes, multiple pending notifications. No owner devices or repositories changed.
