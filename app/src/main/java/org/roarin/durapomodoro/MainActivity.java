package org.roarin.durapomodoro;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Physical D-pad first: the number editor has explicit +/- controls and center-to-exit. */
public class MainActivity extends Activity {
    private SharedPreferences p;
    private TextView clock, stage, state;
    private LinearLayout root;
    private final Handler handler = new Handler();
    private int mode;
    private long remaining;
    private String displayKey = "";
    private final int[] colors = {Color.rgb(30,110,45),Color.rgb(155,35,35),Color.rgb(155,35,35)};

    private int manualMinutes(int type) { return p.getInt("minutes"+type,new int[]{25,5,15}[type]); }
    private long manualDuration(int type) { return manualMinutes(type)*60_000L; }
    private String sessionKind() { return p.getString("sessionKind", SessionPlan.CLASSIC); }
    private SessionPlan.Step[] plan() {
        return SessionPlan.selectedPlan(sessionKind(), p.getString("activePlan",null),p.getString("customPlan",null));
    }
    private long selectedDuration() {
        SessionPlan.Step[] steps=plan();
        int pos=p.getInt("sessionStep",0);
        return steps==null||pos<0||pos>=steps.length?manualDuration(mode):steps[pos].durationMs();
    }
    private long nextGeneration() {
        long n=Math.max(p.getLong("generationCounter",0),p.getLong("generation",0))+1;
        return n>0?n:0;
    }
    private void cancelGeneration(long g) {
        if(g>0)TimerReceiver.cancel(this,g);
        else TimerReceiver.cancelLegacy(this);
    }
    private boolean saved(boolean success) {
        if(!success)Toast.makeText(this,"Unable to save timer changes; try again",Toast.LENGTH_LONG).show();
        return success;
    }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        p=getSharedPreferences("timer",0);
        // Preserve the v0.1.11 user's current running/paused manual timer on upgrade.
        // New installations choose the complete automatic classic preset.
        if(!p.contains("sessionKind")) {
            p.edit().putString("sessionKind", p.getAll().isEmpty()?SessionPlan.CLASSIC:SessionPlan.MANUAL).commit();
        }
        if(!TimerReceiver.migrateLegacyActive(this))TimerReceiver.reconcileActive(this);
        TimerReceiver.retryPendingNotification(this);
        mode=p.getInt("mode",0);
        remaining=p.getLong("remaining",selectedDuration());
        if(!p.contains("timerPaused")){
            // Existing installations do not identify ready versus paused. Preserve
            // a partial manual remainder; an untouched full duration is ready.
            boolean legacyPaused=SessionPlan.MANUAL.equals(sessionKind())&&p.getLong("end",0)==0
                &&TimerEditPolicy.inferLegacyPaused(remaining,manualDuration(Math.max(0,Math.min(2,mode))));
            p.edit().putBoolean("timerPaused",legacyPaused).commit();
        }
        render();
    }

    @Override protected void onStart(){
        super.onStart();
        // If the UI was only stopped (not destroyed), an overdue alarm may be
        // awaiting Doze delivery. Reconcile when returning to the foreground.
        if(p.getLong("end",0)>0&&p.getLong("end",0)<=System.currentTimeMillis())
            TimerReceiver.reconcileActive(this);
        handler.removeCallbacks(tick);
        handler.post(tick);
    }

    @Override protected void onStop(){
        // AlarmManager and BroadcastReceiver keep time while the Activity is hidden;
        // there is no need for a twice-per-second UI refresh in the background.
        handler.removeCallbacks(tick);
        super.onStop();
    }

    private String currentDisplayKey() {
        return sessionKind()+":"+p.getInt("mode",0)+":"+p.getInt("sessionStep",0)+":"+p.getBoolean("sessionDone",false);
    }
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            String key=currentDisplayKey();
            if(!key.equals(displayKey)) {
                mode=p.getInt("mode",0);
                render();
            }
            long end=p.getLong("end",0);
            remaining=end>0?Math.max(0,end-System.currentTimeMillis()):p.getLong("remaining",selectedDuration());
            if(clock!=null) {
                long secs=(remaining+999)/1000;
                clock.setText(String.format(Locale.US,"%02d:%02d",secs/60,secs%60));
            }
            if(state!=null) {
                if(p.getBoolean("sessionDone",false)&&!SessionPlan.MANUAL.equals(sessionKind()))state.setText("Session finished — Reset to begin again");
                else if(end>0)state.setText("Running · alarm active with screen closed");
                else if(remaining<=0)state.setText("Stage finished — Next stage or Reset");
                else state.setText("Ready / paused");
            }
            handler.postDelayed(this,500);
        }
    };

    private Button addButton(LinearLayout parent,String label,Runnable onClick) {
        Button b=new Button(this);
        b.setAllCaps(false);
        b.setText(label);
        b.setOnClickListener(v->onClick.run());
        parent.addView(b);
        return b;
    }
    private TextView addText(LinearLayout parent,String value,int size) {
        TextView t=new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        parent.addView(t);
        return t;
    }
    private String profileLabel() {
        String kind=sessionKind();
        if(SessionPlan.MANUAL.equals(kind))return "Manual timer";
        return SessionPlan.CUSTOM.equals(kind)?"Custom automatic session":"Classic automatic session";
    }

    private void render() {
        displayKey=currentDisplayKey();
        mode=p.getInt("mode",0);
        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16,10,16,10);
        root.setBackgroundColor(colors[Math.max(0,Math.min(2,mode))]);
        ScrollView sc=new ScrollView(this);
        sc.setFillViewport(true);
        sc.addView(root);
        setContentView(sc);
        addButton(root,profileLabel()+"  [Change]",this::pickProfile);
        String label=SessionPlan.LABELS[Math.max(0,Math.min(2,mode))];
        SessionPlan.Step[] steps=plan();
        if(steps!=null){
            int n=p.getInt("sessionStep",0);
            label=p.getBoolean("sessionDone",false)?"Session complete":label+"  ·  "+(n+1)+" / "+steps.length;
        }
        stage=addText(root,label,24);
        stage.setTextColor(Color.WHITE);
        clock=addText(root,"00:00",40);
        clock.setTextColor(Color.WHITE);
        state=addText(root,"",16);
        state.setTextColor(Color.WHITE);
        addButton(root,"Start / resume",this::start);
        addButton(root,"Pause",this::pause);
        addButton(root,steps==null?"Reset":"Reset full session",this::reset);
        addButton(root,"Next stage",this::nextStage);
        addButton(root,"Edit custom session",this::editCustomSession);
        addButton(root,"Settings",this::settings);
    }

    private void start() {
        if(p.getLong("end",0)>0) {
            if(!TimerReceiver.reconcileActive(this))Toast.makeText(this,"Alarm not scheduled; tap Start to retry",Toast.LENGTH_LONG).show();
            return;
        }
        if(p.getBoolean("sessionDone",false)&&plan()!=null) {
            Toast.makeText(this,"Session complete — choose Reset full session",Toast.LENGTH_SHORT).show();
            return;
        }
        remaining=p.getLong("remaining",selectedDuration());
        if(remaining<=0)remaining=selectedDuration();
        long g=nextGeneration();
        if(g<=0){Toast.makeText(this,"Timer generation overflow",Toast.LENGTH_LONG).show();return;}
        long end=System.currentTimeMillis()+remaining;
        SharedPreferences.Editor e=p.edit().putLong("end",end).putLong("remaining",remaining)
            .putLong("generation",g).putLong("generationCounter",g)
            .putBoolean("timerPaused",false);
        if(plan()!=null&&p.getString("activePlan",null)==null)e.putString("activePlan",SessionPlan.encode(plan()));
        if(!saved(e.commit()))return;
        try{TimerReceiver.schedule(this,end,g);}
        catch(RuntimeException ex) {
            android.util.Log.w("DuraPomodoro","Start alarm scheduling deferred",ex);
            Toast.makeText(this,"Timer saved; tap Start to retry scheduling",Toast.LENGTH_LONG).show();
        }
    }
    private void pause() {
        long end=p.getLong("end",0);
        if(end<=0)return;
        long now=System.currentTimeMillis();
        if(now>=end){TimerReceiver.reconcileActive(this);return;}
        long g=p.getLong("generation",0),rest=end-now;
        if(saved(p.edit().putLong("end",0).putLong("remaining",rest)
                .putBoolean("timerPaused",true).commit())){
            remaining=rest;cancelGeneration(g);
        }
    }
    private void reset() {
        long old=p.getLong("generation",0);
        String kind=sessionKind();
        SessionPlan.Step[] steps=SessionPlan.MANUAL.equals(kind)?null:
            SessionPlan.selectedPlan(kind,null,p.getString("customPlan",null));
        int nextType=steps==null?mode:steps[0].kind;
        long fresh=steps==null?manualDuration(nextType):steps[0].durationMs();
        SharedPreferences.Editor e=p.edit().putLong("end",0).putLong("remaining",fresh)
            .putInt("mode",nextType).putInt("sessionStep",0).putBoolean("sessionDone",false)
            .putBoolean("timerPaused",false);
        if(steps!=null)e.putString("activePlan",SessionPlan.encode(steps));
        if(saved(e.commit())){cancelGeneration(old);mode=nextType;remaining=fresh;render();}
    }
    private void nextStage() {
        long old=p.getLong("generation",0);
        SessionPlan.Step[] steps=plan();
        int nextType, nextIndex=0;
        boolean finished=false;
        long fresh;
        if(steps==null) {
            nextType=(mode+1)%3;
            fresh=manualDuration(nextType);
        }else{
            if(p.getBoolean("sessionDone",false)){
                Toast.makeText(this,"Session finished — Reset to restart",Toast.LENGTH_SHORT).show();return;
            }
            nextIndex=p.getInt("sessionStep",0)+1;
            finished=nextIndex>=steps.length;
            if(finished){nextIndex=steps.length-1;nextType=steps[nextIndex].kind;fresh=0;}
            else {nextType=steps[nextIndex].kind;fresh=steps[nextIndex].durationMs();}
        }
        SharedPreferences.Editor e=p.edit().putLong("end",0).putInt("mode",nextType)
            .putInt("sessionStep",nextIndex).putBoolean("sessionDone",finished).putLong("remaining",fresh)
            .putBoolean("timerPaused",false);
        if(saved(e.commit())){cancelGeneration(old);mode=nextType;remaining=fresh;render();}
    }
    private void pickProfile() {
        String[] choices={"Classic automatic · 25–5–25–5–25–5–25–15","Custom automatic · your saved steps","Manual single-stage timer"};
        new AlertDialog.Builder(this).setTitle("Choose session")
            .setItems(choices,(dialog,which)->{
                String kind=which==0?SessionPlan.CLASSIC:which==1?SessionPlan.CUSTOM:SessionPlan.MANUAL;
                if(kind.equals(sessionKind()))return;
                if(p.getLong("end",0)>0){
                    new AlertDialog.Builder(this).setTitle("Switch session?")
                        .setMessage("This will stop the current countdown. Your saved custom plan is kept.")
                        .setPositiveButton("Switch",(d,w)->selectProfile(kind))
                        .setNegativeButton("Keep timer",null).show();
                }else selectProfile(kind);
            }).show();
    }
    private void selectProfile(String kind) {
        long old=p.getLong("generation",0);
        SessionPlan.Step[] steps=SessionPlan.MANUAL.equals(kind)?null:
            SessionPlan.selectedPlan(kind,null,p.getString("customPlan",null));
        int nextType=steps==null?0:steps[0].kind;
        long fresh=steps==null?manualDuration(nextType):steps[0].durationMs();
        SharedPreferences.Editor e=p.edit().putString("sessionKind",kind)
            .putInt("sessionStep",0).putInt("mode",nextType)
            .putBoolean("sessionDone",false).putLong("end",0).putLong("remaining",fresh)
            .putBoolean("timerPaused",false);
        if(steps!=null)e.putString("activePlan",SessionPlan.encode(steps));
        else e.remove("activePlan");
        if(saved(e.commit())){cancelGeneration(old);mode=nextType;remaining=fresh;render();}
    }

    /** A number field can always be exited with D-pad center/Enter. */
    private EditText numberField(int value) {
        EditText e=new EditText(this);
        e.setSingleLine(true);
        e.setSelectAllOnFocus(true);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        e.setImeOptions(EditorInfo.IME_ACTION_DONE);
        e.setText(String.valueOf(value));
        e.setLayoutParams(new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1));
        e.setOnEditorActionListener((v,action,event)->{
            if(action==EditorInfo.IME_ACTION_DONE || (event!=null&&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)){
                exitNumberField(e);return true;
            }
            return false;
        });
        e.setOnKeyListener((view,key,event)->{
            if(event.getAction()==KeyEvent.ACTION_UP&&
                    (key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER)){
                exitNumberField(e);return true;
            }
            return key==KeyEvent.KEYCODE_DPAD_CENTER&&event.getAction()==KeyEvent.ACTION_DOWN;
        });
        return e;
    }
    private void exitNumberField(EditText e) {
        InputMethodManager input=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if(input!=null)input.hideSoftInputFromWindow(e.getWindowToken(),0);
        View target=e.focusSearch(View.FOCUS_DOWN);
        e.clearFocus();
        if(target!=null)target.requestFocus();
    }
    /** On a tiny screen use +/- without needing a virtual keyboard; long-press = five minutes. */
    private EditText durationEditor(LinearLayout parent,int value) {
        LinearLayout row=new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button down=new Button(this);down.setText("−");down.setAllCaps(false);
        EditText field=numberField(value);
        Button up=new Button(this);up.setText("+");up.setAllCaps(false);
        down.setOnClickListener(v->nudge(field,-1));
        up.setOnClickListener(v->nudge(field,1));
        down.setOnLongClickListener(v->{nudge(field,-5);return true;});
        up.setOnLongClickListener(v->{nudge(field,5);return true;});
        row.addView(down);row.addView(field);row.addView(up);
        parent.addView(row);
        return field;
    }
    private void nudge(EditText field,int change) {
        int n=readMinutes(field);
        if(n<1)n=1;
        n=Math.min(240,Math.max(1,n+change));
        field.setText(String.valueOf(n));
    }
    private int readMinutes(EditText e) {
        try {
            int n=Integer.parseInt(e.getText().toString().trim());
            if(n>=1&&n<=240)return n;
        }catch(NumberFormatException ignored){}
        return -1;
    }
    private ScrollView scrollPanel(LinearLayout panel) {
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setPadding(12,4,12,4);
        scroll.addView(panel);
        return scroll;
    }

    private void settings() {
        LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);
        addText(panel,"Manual stage lengths (1–240 minutes)",16);
        EditText[] fields=new EditText[3];
        for(int i=0;i<3;i++){
            addText(panel,SessionPlan.LABELS[i],16);
            fields[i]=durationEditor(panel,manualMinutes(i));
        }
        CheckBox notify=new CheckBox(this);
        notify.setText("Completion notification / sound");
        notify.setChecked(p.getBoolean("notify",true)); panel.addView(notify);
        CheckBox vibrate=new CheckBox(this);
        vibrate.setText("Vibrate at completion");
        vibrate.setChecked(p.getBoolean("vibrate",true)); panel.addView(vibrate);
        addButton(panel,"Choose completion sound",()->{
            Intent picker=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);
            picker.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,true);
            startActivityForResult(picker,42);
        });
        addText(panel,"Automatic-session behavior",16);
        CheckBox autoAdvance=new CheckBox(this);
        autoAdvance.setText("Advance to next stage automatically");
        autoAdvance.setChecked(p.getBoolean("autoAdvance",true));panel.addView(autoAdvance);
        CheckBox autoStart=new CheckBox(this);
        autoStart.setText("Start next stage automatically");
        autoStart.setChecked(p.getBoolean("autoStart",true));panel.addView(autoStart);
        addText(panel,"Auto-start applies when auto-advance is enabled. The final stage always ends the session.",13);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Timer settings")
            .setView(scrollPanel(panel)).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(ignored->{
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                int[] vals=new int[3];
                for(int i=0;i<3;i++){
                    vals[i]=readMinutes(fields[i]);
                    if(vals[i]<1){fields[i].setError("Use 1–240 minutes");fields[i].requestFocus();return;}
                }
                long before=p.getLong("remaining",selectedDuration());
                boolean running=p.getLong("end",0)>0;
                boolean paused=p.getBoolean("timerPaused",TimerEditPolicy.inferLegacyPaused(before,manualDuration(mode)));
                boolean refreshReady=TimerEditPolicy.replaceReadyRemainder(
                    SessionPlan.MANUAL.equals(sessionKind()),running,paused);
                SharedPreferences.Editor e=p.edit();
                for(int i=0;i<3;i++)e.putInt("minutes"+i,vals[i]);
                e.putBoolean("notify",notify.isChecked()).putBoolean("vibrate",vibrate.isChecked());
                e.putBoolean("autoAdvance",autoAdvance.isChecked()).putBoolean("autoStart",autoStart.isChecked());
                if(refreshReady){
                    // Apply new duration atomically with Settings to a ready timer.
                    e.putLong("remaining",vals[mode]*60_000L).putBoolean("timerPaused",false);
                }
                if(!saved(e.commit()))return;
                remaining=p.getLong("remaining",selectedDuration());
                d.dismiss();render();
            });
        });
        d.show();
    }

    private static final class StepEditor {
        final Spinner type;
        final EditText minutes;
        StepEditor(Spinner type,EditText minutes){this.type=type;this.minutes=minutes;}
    }
    private void editCustomSession() {
        SessionPlan.Step[] initial=SessionPlan.decode(p.getString("customPlan",null));
        if(initial==null)initial=SessionPlan.defaultCustom();
        final ArrayList<SessionPlan.Step> draft=new ArrayList<>();
        Collections.addAll(draft,initial);
        final LinearLayout panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);
        final LinearLayout rows=new LinearLayout(this);rows.setOrientation(LinearLayout.VERTICAL);
        final ArrayList<StepEditor> editors=new ArrayList<>();
        addText(panel,"Build a sequence of focus and break stages (1–64 steps, each 1–240 minutes). +/− changes by 1; long-press changes by 5.",14);
        panel.addView(rows);
        final Runnable[] rebuild=new Runnable[1];
        final java.util.concurrent.Callable<Boolean> collect=()->{
            ArrayList<SessionPlan.Step> updated=new ArrayList<>();
            for(StepEditor row:editors){
                int n=readMinutes(row.minutes);
                if(n<1){row.minutes.setError("Use 1–240 minutes");row.minutes.requestFocus();return false;}
                updated.add(new SessionPlan.Step(row.type.getSelectedItemPosition(),n));
            }
            draft.clear();draft.addAll(updated);
            return true;
        };
        rebuild[0]=()->{
            rows.removeAllViews();editors.clear();
            for(int i=0;i<draft.size();i++){
                final int at=i;
                addText(rows,"Stage "+(i+1),17);
                Spinner type=new Spinner(this);
                ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,SessionPlan.LABELS);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                type.setAdapter(adapter);type.setSelection(draft.get(i).kind);
                rows.addView(type);
                EditText minutes=durationEditor(rows,draft.get(i).minutes);
                editors.add(new StepEditor(type,minutes));
                LinearLayout operations=new LinearLayout(this);
                operations.setOrientation(LinearLayout.HORIZONTAL);
                rows.addView(operations);
                addButton(operations,"↑",()->{
                    try {if(at>0&&collect.call()){Collections.swap(draft,at,at-1);rebuild[0].run();}}
                    catch(Exception ex){android.util.Log.w("DuraPomodoro","Invalid step editor",ex);}
                });
                addButton(operations,"↓",()->{
                    try {if(at<draft.size()-1&&collect.call()){Collections.swap(draft,at,at+1);rebuild[0].run();}}
                    catch(Exception ex){android.util.Log.w("DuraPomodoro","Invalid step editor",ex);}
                });
                addButton(operations,"Remove",()->{
                    if(draft.size()<=1){Toast.makeText(this,"Keep at least one step",Toast.LENGTH_SHORT).show();return;}
                    try {if(collect.call()){draft.remove(at);rebuild[0].run();}}
                    catch(Exception ex){android.util.Log.w("DuraPomodoro","Invalid step editor",ex);}
                });
            }
        };
        addButton(panel,"Add focus stage",()->appendStep(draft,collect,rebuild,new SessionPlan.Step(0,25)));
        addButton(panel,"Add short break",()->appendStep(draft,collect,rebuild,new SessionPlan.Step(1,5)));
        addButton(panel,"Add long break",()->appendStep(draft,collect,rebuild,new SessionPlan.Step(2,15)));
        rebuild[0].run();
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Edit custom session")
            .setView(scrollPanel(panel)).setPositiveButton("Save session",null).setNegativeButton("Cancel",null).create();
        d.setOnShowListener(ignored->{
            d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
                try {
                    if(!collect.call())return;
                    if(!saved(p.edit().putString("customPlan",SessionPlan.encode(draft.toArray(new SessionPlan.Step[0]))).commit()))return;
                    d.dismiss();
                    Toast.makeText(this,"Custom plan saved. Choose Custom or Reset to apply it.",Toast.LENGTH_LONG).show();
                }catch(Exception ex){Toast.makeText(this,"Could not save session",Toast.LENGTH_LONG).show();}
            });
        });
        d.show();
    }
    private void appendStep(List<SessionPlan.Step> draft,java.util.concurrent.Callable<Boolean> collect,Runnable[] rebuild,SessionPlan.Step step) {
        if(draft.size()>=SessionPlan.MAX_STEPS){Toast.makeText(this,"Maximum 64 stages",Toast.LENGTH_LONG).show();return;}
        try{if(collect.call()){draft.add(step);rebuild[0].run();}}
        catch(Exception ex){android.util.Log.w("DuraPomodoro","Invalid step editor",ex);}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);
        if(request==42&&result==RESULT_OK&&data!=null){
            Uri uri=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            p.edit().putString("sound",uri==null?"silent":uri.toString()).apply();
            Toast.makeText(this,"Completion sound selected",Toast.LENGTH_SHORT).show();
        }
    }
    @Override protected void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
}
