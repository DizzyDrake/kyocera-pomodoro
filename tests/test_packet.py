"""Offline source and workflow contract checks; deliberately not Android compile tests."""
from pathlib import Path
import hashlib, json, unittest, xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
PROV=json.loads((ROOT/'docs/PROVENANCE.json').read_text('utf-8'))
def read(p): return (ROOT/p).read_text('utf-8')
class PacketChecks(unittest.TestCase):
    def test_original_java_and_manifest_bytes(self):
        for name,expected in PROV['unchanged_source_hashes_sha256'].items():
            if name=='app/build.gradle':continue
            self.assertEqual(hashlib.sha256((ROOT/name).read_bytes()).hexdigest(),expected,name)
    def test_android_manifest_valid_xml_and_identity(self):
        root=ET.parse(ROOT/'app/src/main/AndroidManifest.xml').getroot()
        self.assertEqual(root.tag,'manifest')
        labels=' '.join(e.attrib.get('{http://schemas.android.com/apk/res/android}name','') for e in root.iter())
        self.assertIn('.MainActivity',labels)
        self.assertIn('.TimerReceiver',labels)
        self.assertIn('.BootReceiver',labels)
    def test_no_network_permission(self):
        self.assertNotIn('android.permission.INTERNET',read('app/src/main/AndroidManifest.xml'))
    def test_source_deadline_alarm_and_versioning(self):
        receiver=read('app/src/main/java/org/roarin/durapomodoro/TimerReceiver.java')
        for feature in ['setExactAndAllowWhileIdle','FLAG_IMMUTABLE','getLongExtra("generation"','completionNotificationPending','retryPendingNotification']:
            self.assertIn(feature,receiver)
    def test_saved_paused_remainder_and_settings_validation(self):
        activity=read('app/src/main/java/org/roarin/durapomodoro/MainActivity.java')
        for feature in ['setOnShowListener','setError("Use 1–240 minutes")','if(stored<=0)','dialog.dismiss()','TimerReceiver.reconcileActive']:
            self.assertIn(feature,activity)
        self.assertIn('Expired UI display is read-only',activity)
    def test_gradle_versions_and_identity(self):
        gradle=read('app/build.gradle');rootgradle=read('build.gradle')
        for feature in ['compileSdk 33','minSdk 26','targetSdk 29','versionCode 11','org.roarin.durapomodoro']:
            self.assertIn(feature,gradle)
        self.assertIn("version '7.4.2'",rootgradle)
    def test_workflow_has_actions_build_and_artifacts(self):
        workflow=read('.github/workflows/build.yml')
        for feature in ["workflow_dispatch:","java-version: '17'","build-tools;33.0.2","gradle-version: '7.6.4'",':app:assembleDebug','upload-artifact@v4','DuraPomodoro-debug-apk']:
            self.assertIn(feature,workflow)
    def test_signing_secrets_and_verify(self):
        workflow=read('.github/workflows/build.yml')
        for feature in ['DURAPOMODORO_KEYSTORE_B64','DURAPOMODORO_KEYSTORE_PASSWORD','DURAPOMODORO_KEY_ALIAS','DURAPOMODORO_KEY_PASSWORD','apksigner','zipalign','steps.signing.outputs.enabled']:
            self.assertIn(feature,workflow)
    def test_no_signing_material_in_package(self):
        self.assertFalse(list(ROOT.rglob('*.jks')))
        self.assertFalse(list(ROOT.rglob('*.keystore')))
    def test_original_lineage_preserved(self):
        self.assertTrue((ROOT/'docs/source-lineage/ORIGINAL_BUILD_WORKFLOW_v0.1.11.yml').exists())
        self.assertTrue((ROOT/'docs/source-lineage/ORIGINAL_README_v0.1.11.md').exists())
if __name__=='__main__': unittest.main()
