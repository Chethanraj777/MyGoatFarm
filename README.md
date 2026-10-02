# 🐐 My Goat Farm — Android App

Offline Android goat-farm management app.

## Included starting records

- 26/07/2026 — 4 goats purchased — ₹50,000
- 04/08/2026 — 3 goats purchased — ₹26,000
- 07/08/2026 — Nati medicine — ₹600
- 31/08/2026 — Shiva, male kid
- 04/09/2026 — female kid
- 28/09/2026 — groundnut cake 10 kg — ₹580
- 28/09/2026 — maize 10 kg — ₹340
- 28/09/2026 — goat farm shed — ₹1,10,000

## Android Studio

1. Extract this project.
2. Open the folder in Android Studio.
3. Let Gradle sync.
4. Connect your Android phone or use an emulator.
5. Run the `app` configuration.
6. To create an APK: **Build → Build APK(s)**.
7. The debug APK will be at:
   `app/build/outputs/apk/debug/app-debug.apk`

## GitHub Actions

Push the project to a GitHub repository. The workflow at
`.github/workflows/build-apk.yml` automatically builds the debug APK.

After the workflow finishes:
**GitHub → Actions → Build APK → Artifacts → MyGoatFarm-debug-apk**

## Data

Records are stored locally on the phone using SharedPreferences. The app works offline.
The Dashboard has a simple Backup / Export option.
