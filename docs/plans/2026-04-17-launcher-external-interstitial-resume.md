# Launcher External Interstitial Resume Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep external-app launches working after the launcher interstitial closes.

**Architecture:** Keep the ad decision inside `Launcher.startActivitySafely`, but add a one-shot bypass so the post-ad continuation re-enters the same method without showing the ad twice. This preserves Launcher resume gating, deferred launch handling, and the normal external app launch path.

**Tech Stack:** Android Launcher3 Java, TopOn interstitial wrapper, JUnit4 source tests.

---

### Task 1: Lock the regression with a source test

**Files:**
- Create: `super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ads/LauncherExternalInterstitialSourceTest.kt`
- Test: `super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ads/LauncherExternalInterstitialSourceTest.kt`

- [ ] **Step 1: Write the failing test**

Assert that `Launcher.java`:
- defines a one-shot flag for skipping the next launch interstitial
- re-enters `startActivitySafely(v, intent, item)` from the ad callback
- resets the skip flag before falling through to `super.startActivitySafely(...)`

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ads.LauncherExternalInterstitialSourceTest"`

Expected: FAIL because the launcher still calls `super.startActivitySafely(...)` directly from the ad callback and has no one-shot skip flag.

### Task 2: Keep the launch flow inside Launcher

**Files:**
- Modify: `Neo-Launcher-main/Neo-Launcher-main/src/com/android/launcher3/Launcher.java`

- [ ] **Step 1: Add one-shot skip state**

Add a boolean field that skips ad display for exactly one relaunch after the interstitial closes.

- [ ] **Step 2: Re-enter Launcher launch flow from the ad callback**

Replace the direct `super.startActivitySafely(...)` callback launch with `startActivitySafely(v, intent, item)` after setting the one-shot skip flag.

- [ ] **Step 3: Consume the one-shot skip state**

Before the ad decision branch, reset the skip flag and fall through to the existing `super.startActivitySafely(...)` path.

- [ ] **Step 4: Run focused verification**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ads.LauncherExternalInterstitialSourceTest"`

Expected: PASS.
