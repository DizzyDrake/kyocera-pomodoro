# Preserve the installed v0.1.11 app during v0.2 trials

Your v0.1.11 app is confirmed installed and working on the Kyocera Dura; sound and vibration worked with the lid closed. Do not uninstall that version to install a test build.

## First test: install the new parallel preview

The v0.2.1 debug APK has **package `org.roarin.durapomodoro.preview`** and visible label **Dura Pomodoro Preview**. It installs independently of the original app (`org.roarin.durapomodoro`). Its own preferences and timer sessions are separate; it will not automatically copy prior app settings. This is the safe initial way to test before an in-place upgrade.

## If you later want to update the original app in place

Use the optional **personally signed release APK**. The APK needs **all three**:

1. Same application ID: `org.roarin.durapomodoro`.
2. Higher version code (v0.2.1 uses 12 vs v0.1.11 code 11).
3. **Exactly the same signing certificate** as the installed v0.1.11 app. A different GitHub runner's default debug key will almost certainly not match, even with the same code and package name.

If you keep a copy of the original working APK, compare its signer digest with the new release APK using Android SDK build-tools `apksigner verify --print-certs APK_PATH` on both APKs. Never upload signing credentials or personal keystores to the repository. To set up persistent signing, see `SIGNING_AND_UPDATES.md`.

If you do not have the original signing key, Android cannot do an in-place overwrite of that installation. Keep using the original app and run the preview alongside it. Uninstalling would lose the installed app's local data/settings.

**Do not alter the Dumbplayer package or its signing identity.**
