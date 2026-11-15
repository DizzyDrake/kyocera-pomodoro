package org.roarin.durapomodoro;

/** Distinguishes a ready single-stage timer from an explicitly paused timer. */
public final class TimerEditPolicy {
    private TimerEditPolicy() { }

    public static boolean replaceReadyRemainder(boolean manualSession, boolean running,
                                                boolean paused) {
        return manualSession && !running && !paused;
    }

    /** An older installation has no paused marker; avoid discarding a partial remainder. */
    public static boolean inferLegacyPaused(long remainingMs, long oldSelectedDurationMs) {
        return remainingMs > 0 && remainingMs != oldSelectedDurationMs;
    }
}
