# Optional persistent signing for upgradeable APKs

A GitHub debug build runs without configuration, but GitHub-hosted runners may use a different debug certificate on later runs. Installing a new package with a different signer over the existing app fails on Android. **Choose one long-lived private signing key before your first personal release install** and reuse it for every future update. You may use an existing personally controlled Android APK signing keystore if its password and alias are known, or generate a distinct new key with `keytool`.

Create four GitHub repository **Settings → Secrets and variables → Actions → Repository secrets** (spelling must match):

| Secret | Content |
| --- | --- |
| `DURAPOMODORO_KEYSTORE_B64` | Base64 of the raw JKS or PKCS12 keystore file |
| `DURAPOMODORO_KEYSTORE_PASSWORD` | Password opening that keystore |
| `DURAPOMODORO_KEY_ALIAS` | Exact key alias inside the keystore |
| `DURAPOMODORO_KEY_PASSWORD` | Private-key password (often same as store password) |

For base64 of an existing keystore using Windows PowerShell, run (adjust path):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes('C:\path\to\my-private-signing.jks')) | Set-Clipboard
```

Paste the clipboard content as the **base64 secret**, not into a source file. Keep an offline backup of the original keystore and passwords. A key lost or replaced cannot sign a normal in-place Android update to an app signed with the old key. Do not commit keys, passwords, or a `local.properties` file.

With all four secrets configured, Actions also produces `DuraPomodoro-personal-signed-apk`, containing `DuraPomodoro-personal-release.apk`. The workflow performs alignment, APK signing, then `apksigner verify`; the keystore is removed from runner temporary storage after signing.

### Installing

Copy the signed APK to the Kyocera and install through its normal package installer, or use `adb install -r DuraPomodoro-personal-release.apk` if ADB is connected. Updating works with the **same application ID, same signer, and an acceptable version code**. This package sets versionCode 11; bump this number when preparing a future release. If you already installed an earlier APK with a *different* signing identity, Android cannot overwrite it. Preserve any needed timer settings before considering uninstalling; uninstall clears app data. The next practical checks are in `DEVICE_TESTS.md`.
