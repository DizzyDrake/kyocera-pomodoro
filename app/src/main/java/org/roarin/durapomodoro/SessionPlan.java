package org.roarin.durapomodoro;


/** Pure Java, deterministic session rules. No Android dependency. */
public final class SessionPlan {
    public static final String CLASSIC = "classic";
    public static final String CUSTOM = "custom";
    public static final String MANUAL = "manual";
    public static final int MAX_STEPS = 64;
    public static final String[] LABELS = {"Focus", "Short break", "Long break"};

    private SessionPlan() { }

    public static final class Step {
        public final int kind;
        public final int minutes;
        public Step(int kind, int minutes) {
            if (kind < 0 || kind > 2 || minutes < 1 || minutes > 240) {
                throw new IllegalArgumentException("Step must use type 0–2 and 1–240 minutes");
            }
            this.kind = kind;
            this.minutes = minutes;
        }
        public long durationMs() { return minutes * 60_000L; }
        public String label() { return LABELS[kind]; }
    }

    public static Step[] classic() {
        return new Step[] {
            new Step(0,25),new Step(1,5),new Step(0,25),new Step(1,5),
            new Step(0,25),new Step(1,5),new Step(0,25),new Step(2,15)
        };
    }

    public static Step[] defaultCustom() {
        return new Step[] {new Step(0,25),new Step(1,5),new Step(0,25),new Step(2,15)};
    }

    /** Invalid/truncated persisted data is rejected rather than partially executed. */
    public static Step[] decode(String source) {
        if (source == null || source.trim().isEmpty()) return null;
        String[] pieces = source.split(",", -1);
        if (pieces.length < 1 || pieces.length > MAX_STEPS) return null;
        Step[] result = new Step[pieces.length];
        try {
            for (int n=0; n<pieces.length; n++) {
                String[] fields = pieces[n].split(":", -1);
                if (fields.length != 2) return null;
                result[n] = new Step(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]));
            }
        } catch (IllegalArgumentException invalid) {
            return null;
        }
        return result;
    }

    public static String encode(Step[] steps) {
        if (steps == null || steps.length == 0 || steps.length > MAX_STEPS) {
            throw new IllegalArgumentException("Session must have 1–64 steps");
        }
        StringBuilder b = new StringBuilder();
        for (Step step : steps) {
            if (step == null) throw new IllegalArgumentException("Missing step");
            if (b.length() > 0) b.append(',');
            b.append(step.kind).append(':').append(step.minutes);
        }
        return b.toString();
    }

    public static Step[] selectedPlan(String kind, String activeSnapshot, String customTemplate) {
        if (MANUAL.equals(kind)) return null;
        Step[] snap = decode(activeSnapshot);
        if (snap != null) return snap;
        if (CUSTOM.equals(kind)) {
            Step[] custom = decode(customTemplate);
            return custom == null ? defaultCustom() : custom;
        }
        return classic();
    }

    public static String summary(Step[] steps) {
        StringBuilder b = new StringBuilder();
        for (int i=0;i<steps.length;i++) {
            if (i>0) b.append(" · ");
            b.append(steps[i].minutes).append('m').append(' ').append(steps[i].label());
        }
        return b.toString();
    }

    public static final class Completion {
        public final int nextIndex;
        public final int kind;
        public final long remainingMs;
        public final boolean advanced;
        public final boolean startAutomatically;
        public final boolean finished;
        private Completion(int nextIndex,int kind,long remainingMs,boolean advanced,boolean startAutomatically,boolean finished) {
            this.nextIndex=nextIndex;
            this.kind=kind;
            this.remainingMs=remainingMs;
            this.advanced=advanced;
            this.startAutomatically=startAutomatically;
            this.finished=finished;
        }
    }

    /** No implicit repeat after the final step. Auto-start matters only when advancing. */
    public static Completion completed(Step[] steps, int index, boolean autoAdvance, boolean autoStart) {
        if (steps == null || steps.length == 0 || index < 0 || index >= steps.length)
            throw new IllegalArgumentException("Invalid session position");
        if (!autoAdvance) return new Completion(index,steps[index].kind,0L,false,false,false);
        if (index == steps.length - 1)
            return new Completion(index,steps[index].kind,0L,false,false,true);
        Step next = steps[index+1];
        return new Completion(index+1,next.kind,next.durationMs(),true,autoStart,false);
    }
}
