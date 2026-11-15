package org.roarin.durapomodoro;
import android.content.*;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){
  SharedPreferences p=c.getSharedPreferences("timer",0);
  TimerReceiver.retryPendingNotification(c);
  long end=p.getLong("end",0);if(end<=0)return;
  long generation=p.getLong("generation",0);
  if(generation<=0){TimerReceiver.migrateLegacyActive(c);return;}
  TimerReceiver.reconcileActive(c);
 }
}
