# Dura Pomodoro — Tomato launcher icon

This overlay is for the **v0.2.1** GitHub source layout.
The artwork is the owner-approved red oval tomato with a dark green plus-sign leaf, on a light green background.

## Apply to your existing repository

1. **Download and unzip this overlay** on your PC.
2. Open your existing Dura Pomodoro repository's root (where `app/` and `.github/` live).
3. Copy the contents of the unzipped overlay into the **repository root**, allowing the new `app/src/main/res/` files and the matching `app/src/main/AndroidManifest.xml` to be added/replaced. Do not upload the ZIP itself as the only file.
4. Before replacing the manifest, compare it to the existing `app/src/main/AndroidManifest.xml`: the only intended change is adding `android:icon="@mipmap/ic_launcher"` and `android:roundIcon="@mipmap/ic_launcher_round"` to `<application>`. Preserve any independent changes you made to your copy.
5. Commit/push, then use the existing GitHub Actions **Build Dura Pomodoro** workflow. Install or update only after confirming the correct package ID and a matching signing certificate; debug preview and release package IDs may differ.

## Files installed

- Standard density icons: `res/mipmap-mdpi` through `res/mipmap-xxxhdpi`.
- Android 8+ adaptive foreground with transparent margins: `res/drawable-nodpi/ic_launcher_foreground.png`.
- Adaptive launcher definitions: `res/mipmap-anydpi-v26/ic_launcher.xml` and `ic_launcher_round.xml`.
- Light-green background color: `res/values/dura_pomodoro_icon_colors.xml`.
- Image for repository documentation: `repo-assets/dura-pomodoro-tomato-icon.png`.
- The version-matched Android manifest references the icon resources.

**This is a source overlay, not an APK.** It changes only the launcher icon and a manifest reference. It does not alter the timer, sessions, alarms, version or signing. A new GitHub build is needed for the changed icon to appear in the installed app.

Created for Aurora and GPT-6 on November 22, 2026.
