import org.roarin.durapomodoro.SessionPlan;
import java.util.Arrays;

public final class SessionPlanTest {
    private static int checked=0;
    private static void ok(boolean value,String message){checked++;if(!value)throw new AssertionError(message);}
    private static void rejected(String text){ok(SessionPlan.decode(text)==null,"should reject: "+text);}
    public static void main(String[] args) {
        SessionPlan.Step[] c=SessionPlan.classic();
        ok(c.length==8,"classic count");
        int[] kinds={0,1,0,1,0,1,0,2};
        int[] mins={25,5,25,5,25,5,25,15};
        int totalMinutes=0;
        for(SessionPlan.Step step:c)totalMinutes+=step.minutes;
        ok(totalMinutes==130,"classic eight-stage cycle is 130 minutes (not 120)");
        for(int i=0;i<8;i++){
            ok(c[i].kind==kinds[i],"classic type "+i);
            ok(c[i].minutes==mins[i],"classic minutes "+i);
            ok(c[i].durationMs()==mins[i]*60_000L,"classic duration "+i);
        }
        ok(SessionPlan.encode(c).equals("0:25,1:5,0:25,1:5,0:25,1:5,0:25,2:15"),"classic exact sequence");
        ok(SessionPlan.encode(SessionPlan.decode(SessionPlan.encode(c))).equals(SessionPlan.encode(c)),"roundtrip");
        ok(SessionPlan.selectedPlan(SessionPlan.MANUAL,null,"0:2")==null,"manual doesn't run a plan");
        ok(SessionPlan.selectedPlan(SessionPlan.CLASSIC,null,"2:1").length==8,"classic is fixed");
        ok(SessionPlan.selectedPlan(SessionPlan.CUSTOM,null,"2:1")[0].minutes==1,"custom loads template");
        ok(SessionPlan.selectedPlan(SessionPlan.CUSTOM,"0:3,1:4","2:2")[1].minutes==4,"active snapshot pins plan");
        ok(SessionPlan.selectedPlan(SessionPlan.CUSTOM,null,"bad").length==4,"invalid custom gets safe template");
        rejected("");rejected("0:0");rejected("3:20");rejected("0:241");
        rejected("0:25,");rejected("0:25:1");rejected("0:x");rejected("0:25 1:5");
        StringBuilder huge=new StringBuilder();
        for(int i=0;i<65;i++){if(i>0)huge.append(',');huge.append("0:1");}
        rejected(huge.toString());
        SessionPlan.Step[] custom={new SessionPlan.Step(2,1),new SessionPlan.Step(0,240),new SessionPlan.Step(1,3)};
        ok(SessionPlan.decode(SessionPlan.encode(custom)).length==3,"custom varied sequence");
        ok(SessionPlan.decode(SessionPlan.encode(custom))[1].minutes==240,"custom 240 minutes");
        SessionPlan.Completion x=SessionPlan.completed(c,0,true,true);
        ok(x.advanced&&x.startAutomatically&&!x.finished,"auto runs next");
        ok(x.nextIndex==1&&x.kind==1&&x.remainingMs==300_000L,"next short break");
        x=SessionPlan.completed(c,0,true,false);
        ok(x.advanced&&!x.startAutomatically&&!x.finished,"advance and wait");
        x=SessionPlan.completed(c,0,false,true);
        ok(!x.advanced&&!x.startAutomatically&&x.nextIndex==0&&x.remainingMs==0,"manual even if auto start set");
        x=SessionPlan.completed(c,7,true,true);
        ok(x.finished&&!x.advanced&&!x.startAutomatically&&x.remainingMs==0,"stops at final step");
        x=SessionPlan.completed(c,7,false,false);
        ok(!x.finished&&!x.advanced,"manual final waits for Next");
        for(int i=0;i<7;i++){
            x=SessionPlan.completed(c,i,true,true);
            ok(x.nextIndex==i+1,"sequential order "+i);
            ok(x.kind==c[i+1].kind,"stage kind "+i);
        }
        boolean threw=false;
        try{SessionPlan.completed(c,99,true,true);}catch(IllegalArgumentException ex){threw=true;}
        ok(threw,"invalid index rejected");
        ok(SessionPlan.completed(new SessionPlan.Step[]{new SessionPlan.Step(1,1)},0,true,true).finished,"one-step ends");
        System.out.println("SessionPlanTest: "+checked+" assertions PASSED");
    }
}
