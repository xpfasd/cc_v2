# TopOn Ad Scenes Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement all requested TopOn ad placements and scene triggers across downloader and launcher.

**Architecture:** Extend the shared `:topon-ads` module with typed placement IDs and a reusable scene manager for preloading/showing splash, interstitial, and native ads. App modules only call scene-level APIs so each page does not know raw TopOn SDK details.

**Tech Stack:** Kotlin, Android Views, TopOn SDK, Gradle Kotlin DSL, JUnit4 source/logic tests.

---

## Chunk 1: Shared Ad Scene Layer

### Task 1: Placement IDs and Scene Manager

**Files:**
- Modify: `super-video-downloader-master/super-video-downloader-master/topon-ads/src/main/java/com/cc/ads/topon/TopOnAdConfig.kt`
- Create: `super-video-downloader-master/super-video-downloader-master/topon-ads/src/main/java/com/cc/ads/topon/TopOnAdScenes.kt`
- Create: `super-video-downloader-master/super-video-downloader-master/topon-ads/src/main/java/com/cc/ads/topon/TopOnAdSceneManager.kt`

- [ ] Add typed placement constants for first splash, active splash, first interstitial, general interstitial, launcher app interstitial, guide native, language native, and home native.
- [ ] Add manager methods for preload/show splash, preload/show interstitial with automatic reload after close, and creating native/banner views.

## Chunk 2: Downloader Flow

### Task 2: Startup and Guide Flow

**Files:**
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/splash/SplashActivity.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/guide/GuideActivity.kt`

- [ ] Start a three-second splash delay while loading the correct splash placement.
- [ ] Request launcher setup twice when cancelled, then continue to guide.
- [ ] Preload first interstitial and guide native before guide.
- [ ] Show loaded splash when entering guide, show guide native after page two, and show first interstitial at guide completion.

### Task 3: Native Ad Containers

**Files:**
- Modify: language page layout/activity.
- Modify: browser home layout/fragment.

- [ ] Add native ad placeholders for language page and home top.
- [ ] Request native ads by scene and render when available.

### Task 4: General Interstitial Triggers

**Files:**
- Modify video open, download, delete, and browser back handlers.

- [ ] Show general interstitial before video playback, downloads, video deletion, and webpage exit.
- [ ] Continue original action if ad is unavailable.

## Chunk 3: Launcher Flow

### Task 5: Launcher App Open Interstitial

**Files:**
- Modify launcher activity launch path.

- [ ] Intercept non-system/mainstream app launches.
- [ ] Show launcher app interstitial, then continue app launch, and preload next interstitial.

## Chunk 4: Verification

- [ ] Add focused unit/source tests for scene IDs and launch decision logic.
- [ ] Run downloader focused tests.
- [ ] Run `clean :app:assembleDebug`.
- [ ] Run launcher focused tests and `assembleAospOmegaDebug`.
