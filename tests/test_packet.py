"""Offline source/workflow contracts. These do NOT replace an Android SDK compile."""
from pathlib import Path
import json, hashlib, unittest, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
PROV=json.loads((ROOT/'docs/PROVENANCE.json').read_text('utf-8'))
def read(p): return (ROOT/p).read_text('utf-8')
class PacketChecks(unittest.TestCase):
    def test_sha256_manifest(self):
        source=read('SHA256SUMS.txt')
        found=set()
        for line in source.splitlines():
            expected,path=line.split('  ',1)
            self.assertNotIn(path,found)
            found.add(path)
            self.assertEqual(hashlib.sha256((ROOT/path).read_bytes()).hexdigest(),expected,path)
        self.assertIn('app/src/main/java/org/roarin/durapomodoro/SessionPlan.java',found)
        self.assertIn('.github/workflows/build.yml',found)

    def test_retained_unmodified_boot_and_manifest(self):
        for name,expected in PROV['retained_baseline_hashes_sha256'].items():
            self.assertEqual(hashlib.sha256((ROOT/name).read_bytes()).hexdigest(),expected,name)
    def test_manifest_identity_and_permissions(self):
        r=ET.parse(ROOT/'app/src/main/AndroidManifest.xml').getroot()
        labels=' '.join(e.attrib.get('{http://schemas.android.com/apk/res/android}name','') for e in r.iter())
        for label in ['.MainActivity','.TimerReceiver','.BootReceiver']:
            self.assertIn(label,labels)
        self.assertNotIn('android.permission.INTERNET',read('app/src/main/AndroidManifest.xml'))
    def test_persistent_identification_and_upgrade(self):
        gradle=read('app/build.gradle')
        for feature in ['versionCode 13','versionName \'0.2.1\'','org.roarin.durapomodoro','minSdk 26','targetSdk 29','compileSdk 33']:
            self.assertIn(feature,gradle)
    def test_classic_cycle_and_custom_codec(self):
        plan=read('app/src/main/java/org/roarin/durapomodoro/SessionPlan.java')
        for feature in ['new Step(0,25)','new Step(1,5)','new Step(2,15)','MAX_STEPS','decode(String source)','encode(Step[] steps)','completed(Step[] steps']:
            self.assertIn(feature,plan)
    def test_auto_alarm_state_transaction(self):
        receiver=read('app/src/main/java/org/roarin/durapomodoro/TimerReceiver.java')
        for feature in ['SessionTimeline.resolve(plan,position,currentMode,end,now,advance,autoStart)',
                        'generationCounter','pendingNotificationText','if(!e.commit())return',
                        'schedule(c,nextEnd,nextGeneration)','retryPendingNotification(c)',
                        'alarmIntent(c,generation).filterEquals(i)',
                        'nextEnd=out.nextDeadlineMs','putBoolean("timerPaused",false)']:
            self.assertIn(feature,receiver)
        self.assertLess(receiver.index('if(!e.commit())return'),receiver.index('schedule(c,nextEnd,nextGeneration)'))
        self.assertNotIn('System.currentTimeMillis()+out.remainingMs',receiver)
        self.assertIn('if(end<=0||i==null||now<end)return',receiver)
    def test_background_refresh_lifecycle(self):
        activity=read('app/src/main/java/org/roarin/durapomodoro/MainActivity.java')
        self.assertIn('protected void onStart()',activity)
        self.assertIn('protected void onStop()',activity)
        self.assertIn('handler.removeCallbacks(tick);',activity)
        self.assertEqual(activity.count('handler.post(tick);'),1)
    def test_ready_manual_duration_atomic_with_settings(self):
        activity=read('app/src/main/java/org/roarin/durapomodoro/MainActivity.java')
        self.assertIn('TimerEditPolicy.replaceReadyRemainder(',activity)
        self.assertIn('e.putLong("remaining",vals[mode]*60_000L)',activity)
        self.assertIn('putBoolean("timerPaused",true).commit()',activity)
        self.assertIn('if(refreshReady){',activity)
        self.assertIn('if(!saved(e.commit()))return;',activity)
    def test_timeline_pure_java(self):
        timeline=read('app/src/main/java/org/roarin/durapomodoro/SessionTimeline.java')
        self.assertIn('boundary += duration',timeline)
        self.assertIn('if (boundary > now)',timeline)
        self.assertIn('if (!autoStart)',timeline)
        self.assertIn('if (!autoAdvance)',timeline)
        self.assertIn('if (index == steps.length - 1)',timeline)
    def test_settings_and_dpad(self):
        activity=read('app/src/main/java/org/roarin/durapomodoro/MainActivity.java')
        for feature in ['scrollPanel(panel)','KEYCODE_DPAD_CENTER','IME_ACTION_DONE','SOFT_INPUT_ADJUST_RESIZE','setVerticalScrollBarEnabled(true)','durationEditor','setOnLongClickListener','autoAdvance','autoStart','editCustomSession']:
            self.assertIn(feature,activity)
        self.assertIn('Choose completion sound',activity)
    def test_preserve_existing_install_on_upgrade(self):
        main=read('app/src/main/java/org/roarin/durapomodoro/MainActivity.java')
        self.assertIn('p.getAll().isEmpty()?SessionPlan.CLASSIC:SessionPlan.MANUAL',main)
        self.assertIn('p.getLong("remaining",',main)
        self.assertIn('p.getLong("end",0)',main)
    def test_workflow(self):
        workflow=read('.github/workflows/build.yml')
        for feature in ['workflow_dispatch:',"java-version: '17'",'gradle-version: \'7.6.4\'',":app:assembleDebug",'run_java_tests.sh','DuraPomodoro-preview-debug-apk','apksigner','zipalign']:
            self.assertIn(feature,workflow)
    def test_no_signing_material(self):
        self.assertFalse(list(ROOT.rglob('*.jks')))
        self.assertFalse(list(ROOT.rglob('*.keystore')))
    def test_source_lineage(self):
        self.assertTrue((ROOT/'docs/source-lineage/ORIGINAL_README_v0.1.11.md').exists())
        self.assertIn('0fc9eb78',read('docs/PROVENANCE.json'))
    def test_unchanged_debug_and_signing_workflow(self):
        workflow=read('.github/workflows/build.yml')
        for term in ['DURAPOMODORO_KEYSTORE_B64','DURAPOMODORO_KEYSTORE_PASSWORD','DURAPOMODORO_KEY_ALIAS','DURAPOMODORO_KEY_PASSWORD','if: steps.signing.outputs.enabled']:
            self.assertIn(term,workflow)
    def test_recoverable_alarm(self):
        receiver=read('app/src/main/java/org/roarin/durapomodoro/TimerReceiver.java')
        for term in ['reconcileActive','migrateLegacyActive','completionNotificationPending','FLAG_IMMUTABLE','setExactAndAllowWhileIdle']:
            self.assertIn(term,receiver)
if __name__=='__main__': unittest.main()
