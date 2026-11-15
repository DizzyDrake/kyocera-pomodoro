# WQA479 source candidate (not a release)

Based on WQA477, preserving the original ZIP. Receiver accepts an unversioned legacy completion ONLY while persisted generation remains zero and the original deadline is due. Once a generation is persisted, it rejects legacy identity. This addresses the conditional migration-commit-failure case identified by E132.

Static checks only. Android compile, receiver runtime, notification durability, independent state writes and cancellation ordering remain unverified.
