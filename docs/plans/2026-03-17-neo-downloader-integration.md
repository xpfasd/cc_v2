# Neo Downloader Integration Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a single Android app on top of the Neo Launcher project structure that opens into the downloader UI by default, while allowing the user to switch the app into the system Home launcher from downloader settings.

**Architecture:** Keep `Neo-Launcher-main` as the final application module and convert `super-video-downloader` into a reusable Android library module with its own namespace and resources. Merge downloader initialization into `NeoApp`, expose downloader UI as the app's launcher activity, keep `NeoLauncher` as the HOME activity only, and add a settings action inside the downloader that triggers the system launcher picker / home change flow for Neo.

**Tech Stack:** Android Gradle Plugin 9, Kotlin, Data Binding, Dagger Android, Koin, WorkManager, Media3, Retrofit, RxJava3.

---

### Task 1: Register Downloader As A Library Module

**Files:**
- Modify: `F:\my\downloader\Neo-Launcher-main\Neo-Launcher-main\settings.gradle.kts`
- Modify: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\build.gradle.kts`

**Step 1: Add the downloader module to Neo settings**

Include the external downloader project directory as `:downloaderlib`.

**Step 2: Expose downloader dependency versions**

Load the downloader version catalog TOML from the Neo root settings file so the library module can keep its dependency aliases.

**Step 3: Convert downloader app module into a library**

Change plugin setup to `com.android.library`, remove `applicationId`, signing, ABI split, output naming, and other application-only configuration, while keeping source, dependency, KSP, data binding, and Go build tasks intact.

**Step 4: Verify Gradle model is consistent**

Run: `.\gradlew :downloaderlib:tasks --all`
Expected: module is recognized as a library project; no plugin/configuration errors.

### Task 2: Merge Application Initialization

**Files:**
- Modify: `F:\my\downloader\Neo-Launcher-main\Neo-Launcher-main\Omega\src\com\neoapps\neolauncher\NeoApp.kt`

**Step 1: Make Neo application inherit downloader setup**

Change `NeoApp` to extend `com.myAllVideoBrowser.DLApplication`.

**Step 2: Preserve Neo startup behavior**

Keep Koin initialization, launcher-specific lifecycle handling, theme updates, and restart helpers working after the inheritance change.

**Step 3: Ensure downloader initialization still runs**

Call downloader superclass lifecycle methods in the right order so WorkManager, YoutubeDL, FFmpeg, proxy service helpers, and Dagger injection stay active.

### Task 3: Merge Manifests And Entry Points

**Files:**
- Modify: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\src\main\AndroidManifest.xml`
- Modify: `F:\my\downloader\Neo-Launcher-main\Neo-Launcher-main\Omega\AndroidManifest.xml`
- Modify: `F:\my\downloader\Neo-Launcher-main\Neo-Launcher-main\build.gradle.kts`

**Step 1: Make downloader manifest library-safe**

Remove application-only attributes that must belong to the final Neo app, but keep downloader activities, services, providers, permissions, and deep links.

**Step 2: Keep downloader as the visible app icon entry**

Ensure `com.myAllVideoBrowser.ui.main.home.MainActivity` is the `MAIN` + `LAUNCHER` activity in the final merged manifest.

**Step 3: Keep Neo as the HOME launcher only**

Remove `android.intent.category.LAUNCHER` from `com.neoapps.neolauncher.NeoLauncher` so the app does not expose a second icon while still remaining a valid Home app.

**Step 4: Wire the app dependency**

Add `implementation(project(":downloaderlib"))` to the Neo app module so the merged APK includes the downloader code and resources.

### Task 4: Add “Set As Launcher” To Downloader Settings

**Files:**
- Modify: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\settings\SettingsFragment.kt`
- Modify: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\settings\SettingsViewModel.kt`
- Modify: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\fragment_settings.xml`
- Modify: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\src\main\res\values\strings.xml`

**Step 1: Add a launcher settings action row**

Insert a dedicated settings row near the general settings section for switching the app into launcher mode.

**Step 2: Implement the trigger**

In downloader settings, add logic that enables Neo's fake launcher component by class name and launches the HOME chooser, with a fallback to system Home settings if the chooser cannot be shown.

**Step 3: Keep the library independent**

Avoid direct compile-time references from downloader code to Neo classes; use `ComponentName` and package/class-name strings or plain Android system intents.

### Task 5: Add Regression Tests For The New Settings Flow

**Files:**
- Create: `F:\my\downloader\super-video-downloader-master\super-video-downloader-master\app\src\test\java\com\myAllVideoBrowser\ui\main\settings\LauncherSettingsActionTest.kt`

**Step 1: Write the failing test**

Cover the launcher-setting action builder / helper so it verifies the fake launcher component name and fallback behavior.

**Step 2: Run the test to confirm red**

Run: `.\gradlew :downloaderlib:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.settings.LauncherSettingsActionTest"`
Expected: FAIL because the helper / behavior does not yet exist.

**Step 3: Implement the minimal production code**

Add the smallest helper/API needed for the settings screen to pass the test.

**Step 4: Re-run the test to confirm green**

Run the same command again.
Expected: PASS.

### Task 6: Verify The Integrated Build

**Files:**
- No source changes required

**Step 1: Run targeted unit tests**

Run: `.\gradlew :downloaderlib:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.settings.LauncherSettingsActionTest"`
Expected: PASS.

**Step 2: Run manifest / compile verification**

Run: `.\gradlew :downloaderlib:assembleDebug :app:assembleOmegaDebug`
Expected: Gradle reaches a successful assemble, or any environment blocker is captured precisely.

**Step 3: Document blockers if environment is incomplete**

If Go, NDK, or Android SDK pieces are missing, record the exact missing dependency instead of claiming success.
