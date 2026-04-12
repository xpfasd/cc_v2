# Language Settings Activity Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move language selection into a dedicated `Activity`, keep selection staged until the user taps `Done`, and refresh the screen to feel like a polished standalone settings page.

**Architecture:** The new screen will be an `Activity` that owns its own layout, back handling, and apply action. Selection state will be isolated into a tiny helper so the UI can stage changes locally and only persist/apply them when the user confirms. Existing language storage in `SharedPrefHelper` and locale application through `AppCompatDelegate` will remain the source of truth.

**Tech Stack:** Kotlin, Android View system with XML, RecyclerView, Material components, Dagger Android, JUnit4/Mockito.

---

### Task 1: Add a dedicated language settings Activity

**Files:**
- Create: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsActivity.kt`
- Create: `app/src/main/res/layout/activity_language_settings.xml`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/SettingsFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/di/module/ActivityBindingModule.kt`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Write the failing test**

Add a small JVM test that describes the Activity-level navigation contract, or a helper-based test that will be used by the Activity, before implementation.

- [ ] **Step 2: Run the test to verify it fails**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.settings.language.*"`

Expected: fail because the new Activity/helper does not exist yet.

- [ ] **Step 3: Write the minimal implementation**

Create `LanguageSettingsActivity` with a `createIntent(context, initialLanguageTag)` helper, inflate the new layout, host the toolbar/title/list/footer directly, and wire the `Done` and back actions.

Update `SettingsFragment` so the language row launches the Activity instead of replacing the fragment container.

Register the new Activity in the manifest and Dagger activity binding module.

- [ ] **Step 4: Run the targeted test/build check**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.settings.language.*"`

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsActivity.kt app/src/main/res/layout/activity_language_settings.xml app/src/main/java/com/myAllVideoBrowser/ui/main/settings/SettingsFragment.kt app/src/main/java/com/myAllVideoBrowser/di/module/ActivityBindingModule.kt app/src/main/AndroidManifest.xml
git commit -m "feat: add language settings activity"
```

### Task 2: Extract staged selection state and make confirm-only apply possible

**Files:**
- Create: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSelectionState.kt`
- Create: `app/src/test/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSelectionStateTest.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsActivity.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsAdapter.kt`

- [ ] **Step 1: Write the failing test**

Add tests for:
- initial selection equals the provided language tag
- changing selection updates only the staged value
- `Done` should use the staged value, not the original value
- blank/unknown input falls back safely

- [ ] **Step 2: Run the test to verify it fails**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.settings.language.LanguageSelectionStateTest"`

Expected: fail because the helper is not implemented yet.

- [ ] **Step 3: Write the minimal implementation**

Add the tiny state holder and make the Activity read/write it. Keep adapter updates focused on selection visuals only.

- [ ] **Step 4: Run the targeted tests again**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.settings.language.*"`

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSelectionState.kt app/src/test/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSelectionStateTest.kt app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsActivity.kt app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsAdapter.kt
git commit -m "feat: stage language selection"
```

### Task 3: Refresh the language screen UI

**Files:**
- Modify: `app/src/main/res/layout/activity_language_settings.xml`
- Modify: `app/src/main/res/layout/item_language_row.xml`
- Modify: `app/src/main/res/values/language_settings_strings.xml`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/dimens.xml`
- Add if needed: `app/src/main/res/drawable/bg_language_*`
- Add if needed: `app/src/main/res/drawable/ic_language_*`

- [ ] **Step 1: Write the failing test**

Add only if a new helper or formatter is introduced for the UI; otherwise keep this task verified by manual build and screen review.

- [ ] **Step 2: Implement the UI refresh**

Build a more intentional standalone settings page:
- top app bar with back affordance and centered title
- short explanatory subtitle
- card-based language list with a stronger selected state
- fixed bottom `Done` action
- cleaner spacing, rounded corners, and stronger contrast

Keep the current warm white / gray app language so the new page feels native to the rest of the app.

- [ ] **Step 3: Run a build check**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:assembleDebug`

Expected: build succeeds.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/res/layout/activity_language_settings.xml app/src/main/res/layout/item_language_row.xml app/src/main/res/values/language_settings_strings.xml app/src/main/res/values/colors.xml app/src/main/res/values/dimens.xml app/src/main/res/drawable
git commit -m "feat: refresh language settings ui"
```

### Task 4: Remove obsolete fragment-only wiring and verify end-to-end

**Files:**
- Modify or delete: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsFragment.kt`
- Modify or delete: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsListener.kt`
- Modify or delete: `app/src/main/java/com/myAllVideoBrowser/ui/main/settings/language/LanguageSettingsNavigator.kt`
- Modify: any remaining callers found by search

- [ ] **Step 1: Search for stale references**

Verify there are no remaining callers that still expect the fragment-based flow.

- [ ] **Step 2: Remove or refactor stale code**

Delete dead fragment navigation pieces if they are no longer needed, or keep only the minimum compatibility shims if another screen still references them.

- [ ] **Step 3: Run the full unit test suite**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:testDebugUnitTest`

Expected: pass.

- [ ] **Step 4: Run a final debug build**

Run: `java -cp .\gradle\wrapper\gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain :downloaderlib:assembleDebug`

Expected: pass.

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "feat: move language settings to activity"
```
