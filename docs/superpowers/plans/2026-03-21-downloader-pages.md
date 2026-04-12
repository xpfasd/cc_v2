# Downloader Pages Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Recreate the downloader Processing, Downloaded, and password flow screens from Figma while preserving existing download data and core actions.

**Architecture:** Keep the existing bottom-tab architecture for Browser, Processing, Downloaded, and Settings. Rebuild Processing and Downloaded inside their current fragment/viewmodel/adapter structure, and add a new password flow reachable from Settings through a dedicated navigation path. Reuse current repositories and download actions; limit changes to presentation, navigation, and a small amount of local state persistence for the password flow.

**Tech Stack:** Android XML layouts, Kotlin fragments/viewmodels, RecyclerView adapters, Material Components, existing databinding setup.

---

## Chunk 1: Processing Tab Refresh

### Task 1: Rebuild Processing screen and item card layout

**Files:**
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\fragment_progress.xml`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\item_progress.xml`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\component\adapter\ProgressAdapter.kt`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\progress\ProgressFragment.kt`
- Create/Modify shared drawables/colors/dimens only if needed for Processing visuals

- [ ] Match the Figma Processing header, yellow top background, paste input, action button, and card spacing without breaking databinding.
- [ ] Keep existing download control actions wired to `pause`, `resume`, `cancel`, and live-stop behavior.
- [ ] Ensure the processing list still renders real `ProgressInfo` data with thumbnail/icon fallbacks.
- [ ] Add only the minimal shared resources required for card backgrounds, progress styling, and icon tints.

## Chunk 2: Downloaded Tab and Dialogs

### Task 2: Rebuild Downloaded screen, item list, and top filters

**Files:**
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\fragment_video.xml`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\item_video.xml`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\component\adapter\VideoAdapter.kt`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\video\VideoFragment.kt`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\video\VideoViewModel.kt`

- [ ] Match the Figma Downloaded header, shield/search actions, filter chips, list container, item separators, and thumbnails.
- [ ] Preserve existing video open/share/delete/rename/open-with behaviors.
- [ ] Add lightweight state for current media filter and selection mode only if the screen needs it.

### Task 3: Implement Downloaded popups and multi-select UI

**Files:**
- Modify/Create popup/dialog layouts under `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\component\dialog\RenameVideoDialog.kt`
- Modify/Create dialog classes under `...app\src\main\java\com\myAllVideoBrowser\ui\component\dialog\`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\video\VideoFragment.kt`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\video\VideoViewModel.kt`

- [ ] Recreate the “more actions”, rename, sort, and multi-select action surfaces from Figma.
- [ ] Reuse existing rename/delete/share logic where possible instead of duplicating behaviors.
- [ ] Keep new selection/sort UI scoped to the Downloaded screen.

## Chunk 3: Settings Password Flow

### Task 4: Add Settings entry point for password flow

**Files:**
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\fragment_settings.xml`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\settings\SettingsFragment.kt`
- Modify: `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\settings\SettingsViewModel.kt`

- [ ] Add a clear settings entry that opens the new password flow rather than embedding it into the existing settings form.
- [ ] Keep the rest of Settings behavior untouched.

### Task 5: Build independent password flow screens

**Files:**
- Create new Kotlin/UI files under `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\java\com\myAllVideoBrowser\ui\main\settings\password\`
- Create new layouts under `D:\Users\xpfasd\Documents\GitHub\downloader-laucher\downloader-laucher\super-video-downloader-master\super-video-downloader-master\app\src\main\res\layout\`
- Modify/create navigation hookup from settings to new screens
- Modify: shared prefs/helper files only if needed for password/security-question persistence

- [ ] Implement `Set Password`, `Confirm Password`, `Security Question`, `Security Question Dialog`, and `Success` as separate screens/dialogs matching Figma.
- [ ] Use a dedicated state holder for entered PIN and selected security question so the flow survives configuration changes where practical.
- [ ] Keep persistence minimal and local: only store what the flow genuinely needs.

## Chunk 4: Integration and Verification

### Task 6: Integrate, build-check, and fix resource collisions

**Files:**
- Modify only integration points touched by earlier tasks

- [ ] Wire new shared drawables/colors/dimens carefully to avoid clobbering the recent homepage work.
- [ ] Run targeted resource compilation for the downloader module.
- [ ] Run targeted Kotlin/source compilation if feasible, and document any pre-existing blockers that remain outside this feature scope.
