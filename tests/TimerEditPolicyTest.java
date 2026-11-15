import org.roarin.durapomodoro.TimerEditPolicy;
public final class TimerEditPolicyTest {
    private static int count;
    private static void ok(boolean value,String msg){count++;if(!value)throw new AssertionError(msg);}
    public static void main(String[] args){
        ok(TimerEditPolicy.replaceReadyRemainder(true,false,false),"idle manual timer updated");
        ok(!TimerEditPolicy.replaceReadyRemainder(true,true,false),"running timer preserved");
        ok(!TimerEditPolicy.replaceReadyRemainder(true,false,true),"paused timer preserved");
        ok(!TimerEditPolicy.replaceReadyRemainder(false,false,false),"session timer preserved");
        ok(TimerEditPolicy.inferLegacyPaused(1,60_000),"partial legacy remainder");
        ok(TimerEditPolicy.inferLegacyPaused(120_000,60_000),"unusual legacy remainder");
        ok(!TimerEditPolicy.inferLegacyPaused(60_000,60_000),"ready legacy remainder");
        ok(!TimerEditPolicy.inferLegacyPaused(0,60_000),"complete legacy stage");
        System.out.println("TimerEditPolicyTest: "+count+" assertions PASSED");
    }
}
