package org.roarin.durapomodoro;
import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.media.RingtoneManager;import android.net.Uri;import android.view.*;import android.widget.*;
public class MainActivity extends Activity {
 SharedPreferences p;TextView clock,stage;LinearLayout root;Handler handler=new Handler();int mode;long remaining;
 int[] colors={Color.rgb(30,110,45),Color.rgb(155,35,35),Color.rgb(155,35,35)};String[] labels={"Work","Short break","Long break"};
 int duration(int m){return p.getInt("minutes"+m,new int[]{25,5,15}[m])*60000;}
 long nextGeneration(){long n=Math.max(p.getLong("generationCounter",0),p.getLong("generation",0))+1;return n>0?n:1;}
 void cancelGeneration(long g){if(g>0)TimerReceiver.cancel(this,g);else TimerReceiver.cancelLegacy(this);}
 void cancelCurrent(){long g=p.getLong("generation",0);if(g>0)TimerReceiver.cancel(this,g);else TimerReceiver.cancelLegacy(this);}
 void save(){p.edit().putInt("mode",mode).putLong("remaining",remaining).apply();}
 @Override public void onCreate(Bundle b){super.onCreate(b);p=getSharedPreferences("timer",0);if(!TimerReceiver.migrateLegacyActive(this))TimerReceiver.reconcileActive(this);TimerReceiver.retryPendingNotification(this);mode=p.getInt("mode",0);remaining=p.getLong("remaining",duration(mode));render();handler.post(tick);}
 Runnable tick=new Runnable(){public void run(){long end=p.getLong("end",0);if(end>0){remaining=Math.max(0,end-System.currentTimeMillis());/* Expired UI display is read-only: TimerReceiver owns alarm completion. */}else{remaining=p.getLong("remaining",duration(mode));/* Receiver completion and Pause/Reset are persisted; refresh without writing state. */}if(clock!=null){long sec=(remaining+999)/1000;clock.setText(String.format(java.util.Locale.US,"%02d:%02d",sec/60,sec%60));}handler.postDelayed(this,500);}};
 TextView button(String label,Runnable action){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setOnClickListener(v->action.run());root.addView(b);return b;}
 void render(){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,12,18,12);root.setBackgroundColor(colors[mode]);ScrollView scroll=new ScrollView(this);scroll.addView(root);setContentView(scroll);stage=new TextView(this);stage.setText(labels[mode]);stage.setTextSize(24);stage.setTextColor(Color.WHITE);root.addView(stage);clock=new TextView(this);clock.setTextSize(40);clock.setTextColor(Color.WHITE);root.addView(clock);
 button("Start / resume",()->{
  // A second press retries the persisted timer without changing its identity.
  if(p.getLong("end",0)>0){
   if(!TimerReceiver.reconcileActive(this))Toast.makeText(this,"Alarm not scheduled; tap Start / resume to retry",Toast.LENGTH_LONG).show();
   return;
  }
  if(remaining<=0)remaining=duration(mode);
  long end=System.currentTimeMillis()+remaining,g=nextGeneration();
  if(!p.edit().putLong("end",end).putLong("generation",g).putLong("generationCounter",g).commit()){
   Toast.makeText(this,"Timer could not be saved",Toast.LENGTH_LONG).show();
   return;
  }
  // Keep the committed generation/deadline if scheduling throws; relaunch and
  // this button both retry the same alarm identity, never minting a new one.
  try{TimerReceiver.schedule(this,end,g);}
  catch(RuntimeException failure){
   android.util.Log.w("DuraPomodoro","Start alarm scheduling deferred",failure);
   Toast.makeText(this,"Timer saved, but alarm not scheduled; tap Start / resume to retry",Toast.LENGTH_LONG).show();
  }
 });
 // Persist the new state before cancelling the old alarm. If commit fails,
 // retain the old alarm and the in-memory UI state for a recoverable retry.
 button("Pause",()->{long end=p.getLong("end",0);if(end>0){long now=System.currentTimeMillis();if(now<end){long paused=end-now;long oldGeneration=p.getLong("generation",0);if(p.edit().putLong("end",0).putLong("remaining",paused).commit()){remaining=paused;cancelGeneration(oldGeneration);}}}});
 button("Reset",()->{long oldGeneration=p.getLong("generation",0);long fresh=duration(mode);if(p.edit().putLong("end",0).putLong("remaining",fresh).commit()){remaining=fresh;cancelGeneration(oldGeneration);}});
 button("Next stage",()->{long oldGeneration=p.getLong("generation",0);int next=(mode+1)%3;long fresh=duration(next);if(p.edit().putLong("end",0).putInt("mode",next).putLong("remaining",fresh).commit()){mode=next;remaining=fresh;cancelGeneration(oldGeneration);render();}});
 button("Settings",this::settings);
 }
 void settings(){LinearLayout panel=new LinearLayout(this);panel.setOrientation(1);final EditText[] fields=new EditText[3];for(int i=0;i<3;i++){TextView l=new TextView(this);l.setText(labels[i]+" minutes (1–240)");panel.addView(l);fields[i]=new EditText(this);fields[i].setInputType(2);fields[i].setText(String.valueOf(duration(i)/60000));panel.addView(fields[i]);}
 CheckBox notify=new CheckBox(this);notify.setText("Completion notification/sound");notify.setChecked(p.getBoolean("notify",true));panel.addView(notify);CheckBox vibrate=new CheckBox(this);vibrate.setText("Vibrate at completion");vibrate.setChecked(p.getBoolean("vibrate",true));panel.addView(vibrate);
 Button soundButton=new Button(this);soundButton.setText("Choose completion sound");soundButton.setOnClickListener(v->{Intent pick=new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);pick.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_NOTIFICATION);pick.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,true);startActivityForResult(pick,42);});panel.addView(soundButton);
 AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Timer settings").setView(panel).setPositiveButton("Save",null).setNegativeButton("Cancel",null).create();
 dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{SharedPreferences.Editor e=p.edit();for(int i=0;i<3;i++){int n;try{n=Integer.parseInt(fields[i].getText().toString().trim());}catch(NumberFormatException ex){fields[i].setError("Use 1–240 minutes");fields[i].requestFocus();return;}if(n<1||n>240){fields[i].setError("Use 1–240 minutes");fields[i].requestFocus();return;}e.putInt("minutes"+i,n);}e.putBoolean("notify",notify.isChecked()).putBoolean("vibrate",vibrate.isChecked()).apply();// Preserve a paused stage's persisted remainder when unrelated settings change.
 // A completed stage (zero remainder) may be initialized to the new duration.
 if(p.getLong("end",0)==0){long stored=p.getLong("remaining",duration(mode));if(stored<=0){remaining=duration(mode);save();}else{remaining=stored;}}render();dialog.dismiss();}));
 dialog.show();
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==42&&result==RESULT_OK&&data!=null){Uri u=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);p.edit().putString("sound",u==null?"silent":u.toString()).apply();Toast.makeText(this,"Completion sound saved",Toast.LENGTH_SHORT).show();}}
 @Override protected void onDestroy(){handler.removeCallbacks(tick);super.onDestroy();}
}
