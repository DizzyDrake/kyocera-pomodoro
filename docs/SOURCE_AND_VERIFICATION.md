# v0.2.1 correction addendum — November 15, 2026

The full v0.2.0 source ZIP remains preserved in Library. This candidate fixes three findings from an independent source review. Absolute deadlines are no longer recomputed from receiver delivery times; overdue automatic stages catch up in a single persisted transaction. A manual timer in the ready (not paused) state immediately adopts newly saved stage minutes. The half-second UI tick is paused in Activity.onStop and restarted in Activity.onStart; Android AlarmManager continues separately.

**Important:** The specified 25–5–25–5–25–5–25–15 pattern totals **130 minutes**. Android's `setExactAndAllowWhileIdle()` may still be delayed by Doze/idle quotas; code fixes drift of session progress and cannot guarantee alert sound at every exact boundary while asleep. A delayed delivery for several completed stages creates one aggregated alert.

Validation uses pure-Java session/planner tests, source contracts, and Java compilation against synthetic Android API stubs. No actual GitHub or Android SDK build or v0.2.1 device test is claimed.

---

# Source provenance and verification — v0.2.0 candidate

**Parent:** `/FOSS/Kyocera Dura Pomodoro/GitHub Build Packets/DuraPomodoro_v0.1.11_GitHub_Actions_Integrated_20261115.zip` (SHA-256 `20ed92b6f04366834c5e020e9a3fcb43c3e31c149a431561c032e46b645b1b8e`). The underlying original v0.1.11 Worker E source candidate remains `Source Candidates/DuraPomodoro_v0.1.11_settings_validation_WQE144_SOURCE_CANDIDATE.zip` (SHA-256 `0fc9eb78de4072bd2a4ad784f7bef354cb20ad83945a1cd8ea663327a4425386`).

**Owner physical evidence:** installed previous integrated version; countdown, one-minute completion, sound and vibration with phone closed work. Keypad entry, Settings scrolling, and no automatic transition are owner-observed limitations of that earlier installed version. See `OWNER_20261115_KYOCERA_POMODORO_PHONE_TRIAL_AND_SESSION_REQUIREMENTS.md` in the Library for exact observed requirements and decisions.

**New source:** `SessionPlan.java` pure deterministic planner and codec; revised `MainActivity.java` UI/session editing and persisted state; revised `TimerReceiver.java` automatic stage advance and exact-alarm scheduling. Manifest changed to support two build-specific labels and explicitly qualified component class names, ensuring the preview application ID suffix cannot change receiver/activity class resolution. `BootReceiver.java` and original Gradle root remain unchanged. New debug application ID suffix `.preview` intentionally protects the installed original. Optional personal release retains original application ID (matching signing identity needed).

**Local verification:** Python source/workflow contract tests, Java source compiling using JDK 21 with `javac --release 8` (compatible with Java 17 on Actions), pure-Java session behavior assertions, Android source javac against *temporary local API stubs only* (syntax/type sanity, not SDK), YAML parsing, shell syntax checks, ZIP integrity and fresh-extraction hash comparisons. See test logs packaged separately with source, if supplied. No Gradle download, Android SDK compiler, on-device install, GitHub Actions run or GitHub repo inspection was possible in the packaging environment. The GitHub connector returned zero accessible repositories. **Do not label this candidate APK-built or phone-validated.**

**Prior source-lineage** notes and original workflow from v0.1.11 are retained under `docs/source-lineage/`, and historical source ZIP remains in Library. The sound picker still saves selection immediately on return; Settings Cancel does not roll it back.
