# Install Referrer Attribution Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add Google Play Install Referrer based ad-attribution detection to the downloader app and persist ad-attributed users across launches.

**Architecture:** Keep the parsing and decision rules in a small pure-Kotlin component so it can be unit tested first, then wrap Google Play Install Referrer behind a thin Android-facing checker launched from `DLApplication`. Persist the result through `SharedPrefHelper`, with one-way promotion to `true`.

**Tech Stack:** Kotlin, Android app module, Google Play Install Referrer library, JUnit4.

---

## Chunk 1: Pure Attribution Rules

### Task 1: Add failing tests for attribution parsing and persistence rules

**Files:**
- Create: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ads/InstallReferrerAttributionDeciderTest.kt`
- Create: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ads/InstallReferrerAttributionCoordinatorTest.kt`

- [ ] Step 1: Write tests for query parsing and one-way persistence.
- [ ] Step 2: Run the focused tests and confirm they fail for missing production code.
- [ ] Step 3: Add minimal pure-Kotlin implementation.
- [ ] Step 4: Re-run focused tests and confirm they pass.

## Chunk 2: App Integration

### Task 2: Add the Play Install Referrer dependency and Android checker

**Files:**
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/gradle/libs.versions.toml`
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/build.gradle.kts`
- Create: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ads/InstallReferrerAttributionChecker.kt`

- [ ] Step 1: Add a failing source-level test or behavior test covering dependency/integration expectations if needed.
- [ ] Step 2: Add the `com.android.installreferrer:installreferrer` dependency through the version catalog.
- [ ] Step 3: Implement the Android-facing checker with non-blocking fetch, safe cleanup, and no startup crash path.
- [ ] Step 4: Verify focused tests still pass.

### Task 3: Persist and trigger attribution state from app startup

**Files:**
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/util/SharedPrefHelper.kt`
- Modify: `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/DLApplication.kt`

- [ ] Step 1: Add failing tests for the new shared-pref keys and startup trigger expectations where practical.
- [ ] Step 2: Add `SharedPrefHelper` accessors for attribution state and attempt tracking.
- [ ] Step 3: Trigger the checker from `DLApplication.onCreate()` on a background coroutine without blocking existing startup work.
- [ ] Step 4: Run the focused attribution tests and the relevant app unit test suite.

## Chunk 3: Verification

### Task 4: Run regression verification

**Files:**
- Test only

- [ ] Step 1: Run focused attribution tests.
- [ ] Step 2: Run a broader app unit test command to catch regressions in the downloader module.
- [ ] Step 3: Record any warnings or limitations honestly in the handoff.
