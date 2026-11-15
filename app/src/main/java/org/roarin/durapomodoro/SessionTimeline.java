package org.roarin.durapomodoro;

/**
 * Resolve a due timer from its previously committed absolute deadline.
 * This is deliberately independent of Android and the wall clock source.
 *
 * With automatic start enabled, stage boundaries are anchored to the original
 * deadline, never to delayed Android alarm delivery. Several stages may have
 * elapsed while idle; they are represented by one durable state transition and
 * one completion notification. Manual/waiting modes never skip stages.
 */
public final class SessionTimeline {
    private SessionTimeline() { }

    public static final class Result {
        public final int index;
        public final int kind;
        public final long remainingMs;
        public final long nextDeadlineMs; // zero means do not schedule an alarm
        public final boolean finished;
        public final boolean advanced;
        public final int completedStages;
        public final int lastCompletedKind;

        private Result(int index, int kind, long remainingMs, long nextDeadlineMs,
                       boolean finished, boolean advanced, int completedStages,
                       int lastCompletedKind) {
            this.index = index;
            this.kind = kind;
            this.remainingMs = remainingMs;
            this.nextDeadlineMs = nextDeadlineMs;
            this.finished = finished;
            this.advanced = advanced;
            this.completedStages = completedStages;
            this.lastCompletedKind = lastCompletedKind;
        }
    }

    /**
     * @param steps currently pinned session, null for a manual single stage
     * @param index current step (ignored for a manual timer)
     * @param manualMode current manual timer mode
     * @param deadline absolute wall-clock deadline of the completed stage
     * @param now actual receive/reconciliation time, at least deadline
     */
    public static Result resolve(SessionPlan.Step[] steps, int index, int manualMode,
                                 long deadline, long now, boolean autoAdvance,
                                 boolean autoStart) {
        if (deadline <= 0 || now < deadline) throw new IllegalArgumentException("Timer not due");
        if (steps == null) {
            if (manualMode < 0 || manualMode >= SessionPlan.LABELS.length)
                throw new IllegalArgumentException("Invalid manual mode");
            return new Result(0, manualMode, 0, 0, false, false, 1, manualMode);
        }
        if (steps.length == 0 || index < 0 || index >= steps.length)
            throw new IllegalArgumentException("Invalid session index");

        int original = index;
        int completed = 0;
        long boundary = deadline;
        while (true) {
            int completedKind = steps[index].kind;
            completed++;
            if (!autoAdvance)
                return new Result(index, completedKind, 0, 0, false, false, completed, completedKind);
            if (index == steps.length - 1)
                return new Result(index, completedKind, 0, 0, true, index != original, completed, completedKind);

            index++;
            SessionPlan.Step next = steps[index];
            if (!autoStart)
                return new Result(index, next.kind, next.durationMs(), 0, false, true, completed, completedKind);

            long duration = next.durationMs();
            // A corrupt/far-future absolute deadline must not overflow into the past.
            if (Long.MAX_VALUE - boundary < duration)
                throw new IllegalArgumentException("Stage deadline overflow");
            boundary += duration;
            if (boundary > now)
                return new Result(index, next.kind, duration, boundary, false, true, completed, completedKind);
            // The next stage also elapsed while Android was idle; consume it
            // before committing an alarm that is already in the past.
        }
    }
}
