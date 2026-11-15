# Update the EXISTING GitHub repository, not a new one

The previous entire source folder was uploaded to GitHub and used for the installed v0.1.11 app (owner report). v0.2.1 is an **in-place source-tree update**, with relative paths matched to that previous root.

**Preferred:** Extract the **changed-files overlay ZIP** and upload/copy each relative-path file over the existing file in your repository, including the hidden `.github/workflows/build.yml`. New paths must also be created. Don't place a second `DuraPomodoro_v0.2.1/` folder inside the GitHub repository, and don't upload just the ZIP file; the workflow expects `app/build.gradle`, `settings.gradle`, and `.github/workflows/build.yml` at their same prior locations.

**Alternative:** Extract the **full source ZIP** and copy/merge its contents over the existing repo root. It is intentionally self-contained, includes all historical source-lineage notes, and should not delete unrelated files you may have added to the repo.

Once changes are committed to `main`/`master`, GitHub Actions will run. The debug artifact is **`DuraPomodoro-preview-debug-apk`**, containing the independently installable v0.2.1 preview. The workflow name remains **Build Dura Pomodoro**.

If the repository uses a different branch name, start the workflow manually from the branch where you committed the files. The current YAML has push triggers for `main` and `master` only, carried over from the previous packet.

**No repository mutation happened through the connector in this session.** The GitHub tool's repository listing was empty; actual files currently in the owner's repo, branch names, and CI statuses could not be inspected. This update was built against the exact prior ZIP, not a guessed fresh project.
