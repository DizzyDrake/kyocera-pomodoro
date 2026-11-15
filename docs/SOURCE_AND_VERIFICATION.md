# Source identity and verification boundaries

Canonical original: `/FOSS/Kyocera Dura Pomodoro/Source Candidates/DuraPomodoro_v0.1.11_settings_validation_WQE144_SOURCE_CANDIDATE.zip`
SHA-256 of original ZIP: `0fc9eb78de4072bd2a4ad784f7bef354cb20ad83945a1cd8ea663327a4425386` (20 original files).

Source from version **v0.1.11 WQE144**, independently reviewed by Worker E in WQE145. The last correction validated in that review prevents the Settings dialog from dismissing after invalid duration input. The selected sound is still immediately persisted when picked; Cancel does not undo that operation. This packet makes **no changes to Java source or Android manifest**.

`PROVENANCE.json` stores SHA-256 of the original Java/manifest/Gradle files, so tests can establish unchanged lineage. `app/build.gradle` was updated to versionCode 11/versionName 0.1.11 and explicit Java 8 language compatibility. `.github/workflows/build.yml` was upgraded to include a self-contained no-secrets debug APK build and optional stable-signed release APK. The former README/workflow and all historical candidate notes are preserved in `source-lineage/`.

Local source/manifest/workflow checks do **not** establish Android compilation. Java 21 is present in the packaging environment but no Android SDK, Gradle distribution or network access was available. The GitHub Actions build is therefore **prepared, not executed**. Initial GitHub runner build logs and physical Kyocera behavior remain to be observed.
