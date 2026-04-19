# External Interstitial Attribution Gating Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent external interstitial preload/show for non-ad-attributed users, and only allow it for ad-attributed users after launcher activation and a three-hour wait.

**Architecture:** Add durable timestamps for ad attribution match and launcher activation, centralize the eligibility decision in `topon-ads`, and update launcher activation flow to record the activation moment. Keep the decision logic pure where possible so it can be unit tested before wiring Android entry points.

**Tech Stack:** Kotlin, Java, Android shared preferences, existing TopOn scene manager, JUnit4.

---

## Chunk 1: Eligibility Rules

### Task 1: Add failing tests for external interstitial gating rules

**Files:**
- Create: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ads/ExternalInterstitialEligibilityTest.kt`

- [ ] Step 1: Write tests for non-attributed users, attributed-but-not-activated users, attributed-and-activated users before 3 hours, and after 3 hours.
- [ ] Step 2: Run focused tests and confirm they fail.
- [ ] Step 3: Add minimal pure decision logic.
- [ ] Step 4: Re-run focused tests and confirm they pass.

## Chunk 2: Shared State

### Task 2: Persist attribution and launcher-activation timestamps

**Files:**
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/util/SharedPrefHelper.kt`
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ads/InstallReferrerAttributionChecker.kt`

- [ ] Step 1: Add failing source or unit tests for new persisted timestamps.
- [ ] Step 2: Store ad attribution matched timestamp when referrer first matches.
- [ ] Step 3: Add shared-pref accessors for launcher activation timestamp.
- [ ] Step 4: Re-run focused tests.

## Chunk 3: Gate preload and show

### Task 3: Apply eligibility checks to external interstitial preload and show

**Files:**
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/topon-ads/src/main/java/com/cc/ads/topon/TopOnAdSceneManager.kt`
- Create or modify helper files under `topon-ads` if needed for accessing persisted gating state

- [ ] Step 1: Add failing tests or source tests for launcher interstitial gating hooks.
- [ ] Step 2: Block `preloadLauncherAppInterstitial` for ineligible users.
- [ ] Step 3: Block `showLauncherAppInterstitial` for ineligible users.
- [ ] Step 4: Verify focused tests pass.

## Chunk 4: Record launcher activation

### Task 4: Capture default launcher activation success

**Files:**
- Modify: `D:/code/cc_v2/cc_v2/Neo-Launcher-main/Neo-Launcher-main/src/com/android/launcher3/Launcher.java`
- Modify or add related tests in `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ads/`

- [ ] Step 1: Add a failing source test for activation timestamp recording.
- [ ] Step 2: Record launcher activation timestamp at the confirmed success point.
- [ ] Step 3: Verify source tests pass.

## Chunk 5: Verification

### Task 5: Run regression verification

**Files:**
- Test only

- [ ] Step 1: Run focused attribution and launcher-gating tests.
- [ ] Step 2: Run at least the relevant unit test subsets in downloader app.
- [ ] Step 3: Report any pre-existing unrelated failures separately.
