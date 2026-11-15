package org.roarin.durapomodoro;
import android.app.*;import android.content.*;import android.media.RingtoneManager;import android.net.Uri;import android.os.*;
public class TimerReceiver extends BroadcastReceiver {
 public static final String CHANNEL="pomodoro_finished";
 private static final int ALARM_REQUEST=7;
 private static Intent alarmIntent(Context c,long generation){
  Intent i=new Intent(c,TimerReceiver.class);
  i.setData(Uri.parse("durapomodoro://alarm/"+generation));
  i.putExtra("generation",generation);
  return i;
 }
 private static PendingIntent token(Context c,long generation,int flags){return PendingIntent.getBroadcast(c,ALARM_REQUEST,alarmIntent(c,generation),flags|PendingIntent.FLAG_IMMUTABLE);}
 public static void schedule(Context c,long at,long generation){
  AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,token(c,generation,PendingIntent.FLAG_UPDATE_CURRENT));
 }
 public static void cancel(Context c,long generation){
  AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  PendingIntent pi=token(c,generation,PendingIntent.FLAG_NO_CREATE);
  if(pi!=null){a.cancel(pi);pi.cancel();}
 }
 public static void cancelLegacy(Context c){
  AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  PendingIntent pi=PendingIntent.getBroadcast(c,ALARM_REQUEST,new Intent(c,TimerReceiver.class),PendingIntent.FLAG_NO_CREATE|PendingIntent.FLAG_IMMUTABLE);
  if(pi!=null){a.cancel(pi);pi.cancel();}
 }
 // Idempotent active-timer migration shared by ordinary Activity launch and BOOT_COMPLETED.
 // A failed commit leaves the old deadline intact; do not cancel the legacy alarm.
 public static boolean migrateLegacyActive(Context c){
  SharedPreferences p=c.getSharedPreferences("timer",0);
  long end=p.getLong("end",0);
  if(end<=0||p.getLong("generation",0)>0)return false;
  long counter=p.getLong("generationCounter",0);
  long next=Math.max(1,counter+1);
  if(next<=0)return false; // counter overflow: preserve legacy state
  if(!p.edit().putLong("generation",next).putLong("generationCounter",next).commit())return false;
  // The persisted generation is recoverable even if scheduling fails here.
  // Do not cancel the legacy token until the replacement has been scheduled.
  if(!reconcileActive(c))return false;
  cancelLegacy(c);
  return true;
 }
 // Idempotent reconciliation on ordinary launch and reboot. Rescheduling the
 // same generation uses the same PendingIntent identity; no new generation is made.
 // Returns false on scheduling failure, leaving persisted deadline recoverable.
 public static boolean reconcileActive(Context c){
  SharedPreferences p=c.getSharedPreferences("timer",0);
  long end=p.getLong("end",0), generation=p.getLong("generation",0);
  if(end<=0||generation<=0)return false;
  if(end<=System.currentTimeMillis()){
   new TimerReceiver().onReceive(c,alarmIntent(c,generation));
   return true;
  }
  try{schedule(c,end,generation);return true;}
  catch(RuntimeException failure){
   android.util.Log.w("DuraPomodoro","Alarm scheduling deferred; will retry on launch/boot",failure);
   return false;
  }
 }
 @Override public void onReceive(Context c,Intent i){
  SharedPreferences p=c.getSharedPreferences("timer",0);
  long end=p.getLong("end",0),generation=p.getLong("generation",0);
  if(end==0||i==null||System.currentTimeMillis()+1000<end)return;
  if(generation==0){
   // A failed legacy migration commit must not disable the still-scheduled legacy alarm.
   // Accept only the original legacy PendingIntent identity while persisted generation is zero.
   Intent legacy=new Intent(c,TimerReceiver.class);
   if(!legacy.filterEquals(i))return;
  }else{
   // Once migration commits, no old unversioned alarm may complete the new timer.
   if(!alarmIntent(c,generation).filterEquals(i)||i.getLongExtra("generation",-1)!=generation)return;
  }
  // Commit completion before posting notification; an unsuccessful commit leaves the deadline recoverable.
  // Keep a durable delivery marker until notify() returns successfully.
  boolean requested=p.getBoolean("notify",true);
  if(!p.edit().putLong("end",0).putLong("remaining",0).putBoolean("completionNotificationPending",requested).commit())return;
  retryPendingNotification(c);
 }
 // At-least-once delivery: a crash between notify and clearing this flag may
 // repeat the sound on relaunch. A fixed notification ID avoids stacked cards.
 public static boolean retryPendingNotification(Context c){
  SharedPreferences p=c.getSharedPreferences("timer",0);
  if(!p.getBoolean("completionNotificationPending",false))return true;
  try{
  NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);
  String selected=p.getString("sound",RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
  Uri sound="silent".equals(selected)?null:Uri.parse(selected);boolean vibration=p.getBoolean("vibrate",true);
  String channelId=CHANNEL+"_"+Integer.toHexString((selected+vibration).hashCode());
  NotificationChannel ch=new NotificationChannel(channelId,"Pomodoro completion",NotificationManager.IMPORTANCE_HIGH);
  ch.enableVibration(vibration);ch.setSound(sound,new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION).build());nm.createNotificationChannel(ch);
  PendingIntent open=PendingIntent.getActivity(c,8,new Intent(c,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new Notification.Builder(c,channelId).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("Pomodoro finished").setContentText("Time for the next stage").setAutoCancel(true).setContentIntent(open).build();nm.notify(11,n);
   // A failed clear retains retry eligibility; no claim of exactly-once delivery.
   return p.edit().putBoolean("completionNotificationPending",false).commit();
  }catch(RuntimeException failure){
   android.util.Log.w("DuraPomodoro","Completion notification deferred",failure);
   return false;
  }
 }
}