# Dura Pomodoro v0.1.10 — paused Settings Save preservation

Based on v0.1.9 WQA493 source candidate. Only MainActivity.settings() changes: saving sound/vibration or duration settings preserves a positive persisted paused remainder; a completed zero remainder is initialized to the configured stage duration. Running deadlines and generation identity are unchanged. No Android compilation or handset tests performed. Existing candidates remain preserved.

When editing stage duration while paused, this candidate intentionally preserves the current paused remainder rather than silently replacing elapsed progress. Owner has not specified alternate duration-edit behavior.
