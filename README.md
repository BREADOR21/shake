<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# Run and deploy your AI Studio app

This contains everything you need to run your app locally.

View your app in AI Studio: https://ai.studio/apps/3c2bfe79-852f-4877-b90b-b7b57c6fc124

## Run Locally

**Prerequisites:**  [Android Studio](https://developer.android.com/studio)


1. Open Android Studio
2. Select **Open** and choose the directory containing this project
3. Allow Android Studio to fix any incompatibilities as it imports the project.
4. Create a file named `.env` in the project directory and set `GEMINI_API_KEY` in that file to your Gemini API key (see `.env.example` for an example)
5. Remove this line from the app's `build.gradle.kts` file: `signingConfig = signingConfigs.getByName("debugConfig")`
6. Run the app on an emulator or physical device

## Build APK via GitHub Actions

This repo includes `.github/workflows/build-apk.yml`, which builds a debug APK on every push to `main`/`master` (or manually via the "Run workflow" button). It does not require a checked-in Gradle wrapper — it installs Gradle directly and reads `gradle/libs.versions.toml` automatically. The resulting APK is uploaded as a downloadable workflow artifact under the Actions tab.

