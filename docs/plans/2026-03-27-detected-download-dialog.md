# Download Dialog Refresh Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Replace only the detected-download bottom dialog with the new Figma design, add image sniff/download support, and keep download progress synchronized through the existing `processing` and `downloaded` pipelines.

**Architecture:** Keep the existing download task pipeline centered on `MainViewModel.downloadVideoEvent`, `ProgressViewModel`, `ProgressInfo`, `CustomRegularDownloader`, and `VideoViewModel`. Rebuild the detected-media bottom sheet UI around lightweight row-state projections derived from `ProgressInfo` plus downloaded-file existence, while extending sniffing to emit direct image candidates as regular downloads.

**Tech Stack:** Android Views + XML layouts, Data Binding, RecyclerView, Room, WorkManager, Glide, Kotlin/JUnit4.

---

### Task 1: Add a dialog row-state model for the new sheet

**Files:**
- Create: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/DetectedDownloadRowState.kt`
- Test: `super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/DetectedDownloadRowStateTest.kt`

**Step 1: Write the failing test**

Add tests for:
- image candidate without progress and without local file => `READY`
- progress item in `DOWNLOADING` => `DOWNLOADING`
- progress item in `PAUSE` => `PAUSED`
- progress item in `SUCCESS` or downloaded file exists => `DOWNLOADED`
- progress item in `ERROR`/`ENOSPC` => `FAILED`

**Step 2: Run test to verify it fails**

Run: `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DetectedDownloadRowStateTest"`

Expected: FAIL because the row-state model does not exist yet.

**Step 3: Write minimal implementation**

Create a pure Kotlin mapper that accepts:
- `VideoInfo`
- optional `ProgressInfo`
- `isDownloaded: Boolean`

Return a UI state enum and supporting fields for icon/action selection.

**Step 4: Run test to verify it passes**

Run the same test command and confirm PASS.

### Task 2: Teach sniffing to detect direct image resources

**Files:**
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/CustomWebViewClient.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/util/VideoUtils.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/VideoDetectionTabViewModel.kt`
- Test: `super-video-downloader-master/super-video-downloader-master/app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/DetectedImageSupportTest.kt`

**Step 1: Write the failing test**

Add focused unit tests around pure helpers for:
- `image/jpeg`, `image/png`, `image/webp`, `image/gif` classifying as image content
- image URL extensions no longer being rejected by the regular-download filter
- generated regular download info using the correct image extension instead of forcing `mp4`

**Step 2: Run test to verify it fails**

Run: `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.DetectedImageSupportTest"`

Expected: FAIL because image support helpers do not exist yet.

**Step 3: Write minimal implementation**

Implement:
- a new content type for images in `CustomWebViewClient`/`VideoUtils`
- image handling in `propagateCheckJob()`
- extension inference for regular direct downloads so image URLs become `jpg/png/webp/gif/...`
- keep using `isRegularDownload = true` so progress/downloaded reuse the current pipeline

**Step 4: Run test to verify it passes**

Run the same test command and confirm PASS.

### Task 3: Rebuild the detected download bottom dialog to match the Figma sheet

**Files:**
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/res/layout/fragment_detected_videos_tab.xml`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/res/layout/item_video_info.xml`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/component/adapter/VideoInfoAdapter.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/DetectedVideosTabFragment.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/VideoDetectionTabViewModel.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/res/values/colors.xml`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/res/values/dimens.xml`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/res/values/strings.xml`
- Create/Modify drawable resources as needed for row icons/backgrounds

**Step 1: Write the failing test**

Prefer a small pure-data test over UI snapshotting:
- verify row action selection from row-state maps to the expected icon/action semantic (`DOWNLOAD`, `PAUSE`, `RESUME`, `DONE`, `FAIL`)
- verify images use the same row rendering contract as videos, except preview overlay and labels

**Step 2: Run test to verify it fails**

Run: `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.*"`

Expected: FAIL because the row-action contract is not implemented yet.

**Step 3: Write minimal implementation**

Implement the new sheet:
- sheet title centered as “Downloadable”
- flat row list with divider lines, not card blocks
- thumbnail left, title + metadata middle, state action icon right
- row metadata line showing size/date or progress text
- image rows use image thumbnail without play overlay; video rows keep play overlay
- action taps route to shared behaviors:
  - `DOWNLOAD` => existing `downloadVideoEvent`
  - `PAUSE` => shared `ProgressViewModel.pauseDownload()`
  - `RESUME/START` => shared `ProgressViewModel.resumeDownload()` or download start if not yet queued
  - `DONE` => open local item or no-op icon state
  - `FAIL` => restart download

**Step 4: Run targeted UI-adjacent verification**

Run unit tests first, then build:
`.\gradlew :super-video-downloader-master:super-video-downloader-master:app:assembleDebug`

Expected: PASS.

### Task 4: Wire shared progress/downloaded state into the dialog

**Files:**
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/progress/ProgressViewModel.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/video/VideoViewModel.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/detectedVideos/DetectedVideosTabFragment.kt`
- Modify: `super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/ui/main/progress/ProgressFragment.kt`

**Step 1: Write the failing test**

Add tests for small helper methods:
- resolving progress info by candidate URL or candidate id
- preferring downloaded state over stale failed state when the file already exists
- image candidate queued through dialog appears in progress state resolution

**Step 2: Run test to verify it fails**

Run the focused unit test command for the new helper test class.

Expected: FAIL because shared-state resolution helpers are missing.

**Step 3: Write minimal implementation**

Expose lightweight lookup helpers or observable data needed by the dialog:
- current progress list lookup by `VideoInfo`
- downloaded-file existence check by title/ext or final file name
- keep all download lifecycle operations delegated to the existing progress/downloaded stack

**Step 4: Run tests and build**

Run:
- `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:testDebugUnitTest`
- `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:assembleDebug`

Expected: PASS on both.

### Task 5: Final verification

**Files:**
- Review modified files only

**Step 1: Run focused tests**

Run:
- `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.detectedVideos.*"`

**Step 2: Run full unit test suite**

Run:
- `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:testDebugUnitTest`

**Step 3: Run debug build**

Run:
- `.\gradlew :super-video-downloader-master:super-video-downloader-master:app:assembleDebug`

**Step 4: Manual sanity checklist**

Verify:
- direct image sniff result appears in the new dialog
- tapping image download creates a shared `ProgressInfo`
- pause/resume/fail/done icons in the dialog match current task state
- completed image/video appears in the existing downloaded page
- `processing` page still reflects the same tasks
