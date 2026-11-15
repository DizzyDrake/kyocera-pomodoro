import org.roarin.durapomodoro.SessionPlan;
import org.roarin.durapomodoro.SessionTimeline;

public final class SessionTimelineTest {
    private static int checked;
    private static void ok(boolean condition, String why) {
        checked++;
        if (!condition) throw new AssertionError(why);
    }
    private static void expectBad(SessionPlan.Step[] steps,int index,int mode,long due,long now) {
        boolean thrown=false;
        try { SessionTimeline.resolve(steps,index,mode,due,now,true,true); }
        catch(IllegalArgumentException e) { thrown=true; }
        ok(thrown,"reject invalid timeline");
    }
    private static SessionTimeline.Result run(SessionPlan.Step[] steps, int index, long due,
                                              long now, boolean advance, boolean start) {
        return SessionTimeline.resolve(steps,index,0,due,now,advance,start);
    }
    public static void main(String[] args) {
        SessionPlan.Step[] three={new SessionPlan.Step(0,1),new SessionPlan.Step(1,1),new SessionPlan.Step(0,1)};
        long due=1_000_000L;
        SessionTimeline.Result r=run(three,0,due,due,true,true);
        ok(r.index==1 && r.kind==1 && r.nextDeadlineMs==due+60_000 && r.remainingMs==60_000,"on time transition");
        ok(r.completedStages==1 && !r.finished && r.advanced && r.lastCompletedKind==0,"one completion");
        r=run(three,0,due,due+30_000,true,true);
        ok(r.nextDeadlineMs==due+60_000,"30-second delay does not drift next boundary");
        r=run(three,0,due,due+60_000,true,true);
        ok(r.index==2 && r.nextDeadlineMs==due+120_000 && r.completedStages==2,"exact boundary consumed");
        r=run(three,0,due,due+90_000,true,true);
        ok(r.index==2 && r.nextDeadlineMs==due+120_000 && r.completedStages==2,"late alarm skips passed break");
        ok(r.lastCompletedKind==1,"last completed label follows late catchup");
        r=run(three,0,due,due+120_000,true,true);
        ok(r.finished && r.index==2 && r.completedStages==3 && r.nextDeadlineMs==0,"complete session at final boundary");
        r=run(three,0,due,due+10_000,false,true);
        ok(r.index==0 && r.remainingMs==0 && !r.advanced && !r.finished,"advance off suppresses auto start");
        r=run(three,0,due,due+10_000,true,false);
        ok(r.index==1 && r.remainingMs==60_000 && r.nextDeadlineMs==0,"advance but wait for start");
        r=run(three,0,due,due+12_000_000,true,false);
        ok(r.index==1 && r.completedStages==1,"manual-start does not skip even after long delay");
        r=run(three,2,due,due+80_000,true,true);
        ok(r.finished && r.completedStages==1,"last stage never repeats");
        r=run(three,2,due,due+80_000,false,true);
        ok(!r.finished && !r.advanced && r.index==2,"final manual stage waits for next");
        r=run(null,7,due,due+10_000,true,true);
        ok(r.completedStages==1 && r.nextDeadlineMs==0 && r.kind==0,"manual single timer remains stopped");
        expectBad(three,0,0,due,due-1);
        expectBad(three,8,0,due,due);
        expectBad(three,0,0,0,due);
        SessionPlan.Step[] classic=SessionPlan.classic();
        long original=123_456_789L;
        r=run(classic,0,original,original+105L*60_000,true,true);
        ok(r.finished && r.completedStages==8 && r.nextDeadlineMs==0,"four Pomodoros finish 105 minutes after first focus ends");
        r=run(classic,0,original,original+104L*60_000,true,true);
        ok(r.index==7 && r.nextDeadlineMs==original+105L*60_000,"late classic stage remains aligned to original clock");
        System.out.println("SessionTimelineTest: "+checked+" assertions PASSED");
    }
}
