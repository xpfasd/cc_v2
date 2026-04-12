# Downloader Home Redesign Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rebuild the downloader home screen and bottom navigation to match the approved Figma design while preserving existing browser, history, bookmark, download, and settings behavior.

**Architecture:** Keep `MainActivity` as the host for the root `ViewPager2` and wire it to four tabs. Keep `BrowserFragment` and `BrowserHomeFragment` as the browser/business entry points, but replace their XML and resource layer with Figma-matched components and connect quick actions to existing flows.

**Tech Stack:** Kotlin, Android View system, XML layouts, Data Binding, Material Components

---

## Chunk 1: Root Navigation

### Task 1: Wire four root tabs

**Files:**
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/component/adapter/MainAdapter.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/MainActivity.kt`
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/res/menu/menu_bottom_bar.xml`

- [ ] Add the settings tab to the adapter and activity selection logic.
- [ ] Replace the bottom bar styling with the approved four-tab configuration.
- [ ] Remove outdated menu labels/icons and align names to `Browser / Processing / Downloaded / Settings`.
- [ ] Build the `app` debug target to catch menu, binding, and resource errors.

## Chunk 2: Browser Home UI

### Task 2: Rebuild the browser home layout

**Files:**
- Modify: `app/src/main/res/layout/fragment_browser_home.xml`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/homeTab/BrowserHomeFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/component/adapter/TopPageAdapter.kt`
- Modify: `app/src/main/res/layout/item_top_page.xml`

- [ ] Replace the old toolbar/search/button/grid layout with the Figma home composition.
- [ ] Keep the existing search submission, suggestions, and top-page opening behavior.
- [ ] Connect `Bookmark` and `History` quick actions to existing bookmark drawer/history flows.
- [ ] Restyle the top-page item cards to match the popular websites card.

## Chunk 3: Resources

### Task 3: Add colors, drawables, and icon states

**Files:**
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/dimens.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Create/Modify: `app/src/main/res/drawable/*.xml`
- Create/Modify: `app/src/main/res/color/*.xml`

- [ ] Add the Figma-matched home colors and spacing tokens.
- [ ] Add pill/card/gradient backgrounds for the redesigned home screen.
- [ ] Add four tab icon selectors with selected and unselected states.
- [ ] Add text/icon color selectors for the bottom bar.

## Chunk 4: Verification

### Task 4: Validate the redesign

**Files:**
- Verify only

- [ ] Run a focused Gradle build for `:app`.
- [ ] Review diffs for unintended behavior changes in navigation and browser flows.
- [ ] Report any validation gaps if emulator/UI screenshot verification is not available locally.
