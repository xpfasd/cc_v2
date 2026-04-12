# Language Whitelist Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Limit the app language picker to system default plus the requested 19 language codes.

**Architecture:** Move language-option construction from resource auto-discovery to a fixed whitelist builder in `PrefUtils.kt`. Keep locale formatting and preference persistence intact while removing the duplicate English-default entry.

**Tech Stack:** Kotlin, Android resources, JUnit 4

---

## Chunk 1: Test The Whitelist Contract

### Task 1: Add a failing unit test for the language map

**Files:**
- Create: `unitTest/com/neoapps/neolauncher/util/PrefUtilsTest.kt`
- Test: `unitTest/com/neoapps/neolauncher/util/PrefUtilsTest.kt`

- [ ] **Step 1: Write the failing test**
- [ ] **Step 2: Run `gradlew.bat testDebugUnitTest --tests com.neoapps.neolauncher.util.PrefUtilsTest` and confirm it fails for the missing/new contract**
- [ ] **Step 3: Implement the minimal production change**
- [ ] **Step 4: Re-run the same test and confirm it passes**

## Chunk 2: Replace The Dynamic Source

### Task 2: Implement the fixed language whitelist

**Files:**
- Modify: `Omega/src/com/neoapps/neolauncher/preferences/Constants.kt`
- Modify: `Omega/src/com/neoapps/neolauncher/util/PrefUtils.kt`
- Modify: `build.gradle.kts`

- [ ] **Step 1: Add system-language constants and the approved language-code whitelist**
- [ ] **Step 2: Refactor language option building to use the whitelist instead of `BuildConfig.DETECTED_ANDROID_LOCALES`**
- [ ] **Step 3: Preserve display formatting for locale labels**
- [ ] **Step 4: Remove the duplicate English default entry**

## Chunk 3: Verify The Result

### Task 3: Run targeted verification

**Files:**
- Modify: `Omega/src/com/neoapps/neolauncher/preferences/Constants.kt`
- Modify: `Omega/src/com/neoapps/neolauncher/util/PrefUtils.kt`
- Modify: `build.gradle.kts`
- Test: `unitTest/com/neoapps/neolauncher/util/PrefUtilsTest.kt`

- [ ] **Step 1: Run `gradlew.bat testDebugUnitTest --tests com.neoapps.neolauncher.util.PrefUtilsTest`**
- [ ] **Step 2: Inspect the diff to confirm only the whitelist behavior changed**
