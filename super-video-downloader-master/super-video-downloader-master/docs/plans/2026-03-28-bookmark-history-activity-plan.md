# Bookmark History Activity Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace bookmark/history fragment overlays with standalone activities that return a selected URL and overwrite the currently selected browser tab, including the home tab slot.

**Architecture:** Add two dedicated activities for bookmark and history selection, route launches through `ActivityResultLauncher`, and introduce a replace-current-tab flow in the browser stack. Relax the browser session model so home is the default starting tab, but not a permanently reserved slot.

**Tech Stack:** Kotlin, Android View system, XML layouts, View Binding/Data Binding, Activity Result API, existing MVVM/ViewModel/Repository setup, JUnit4.

---

## File Map

- Create: `app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksActivity.kt`
- Create: `app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryActivity.kt`
- Create: `app/src/main/res/layout/activity_bookmarks.xml`
- Create: `app/src/main/res/layout/activity_history.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BaseWebTabFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/homeTab/BrowserHomeFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserViewModel.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSession.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryFragment.kt`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSessionManagerTest.kt`
- Test: `app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserSelectionResultContractTest.kt`

## Chunk 1: Browser Replace-Current-Tab Flow

### Task 1: Add failing tests for replacing tabs

**Files:**
- Modify: `app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSessionManagerTest.kt`

- [ ] **Step 1: Write the failing test for replacing a normal selected tab**

```kotlin
@Test
fun `replace current swaps selected non-home tab and keeps index`() {
    val session = BrowserTabSession(
        tabs = listOf(
            PersistedBrowserTab.home(),
            PersistedBrowserTab(id = "a", url = "https://a.com", title = "A"),
            PersistedBrowserTab(id = "b", url = "https://b.com", title = "B")
        ),
        currentTabIndex = 2
    )

    val updated = BrowserTabSessionManager.replaceCurrent(
        session,
        WebTab(url = "https://target.com", title = "Target")
    )

    assertEquals(2, updated.currentTabIndex)
    assertEquals("https://target.com", updated.tabs[2].url)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserTabSessionManagerTest"`
Expected: FAIL because `replaceCurrent` does not exist yet or does not preserve replacement behavior.

- [ ] **Step 3: Write the failing test for replacing the home tab slot**

```kotlin
@Test
fun `replace current allows replacing home slot with regular page`() {
    val session = BrowserTabSessionManager.defaultSession()

    val updated = BrowserTabSessionManager.replaceCurrent(
        session,
        WebTab(url = "https://target.com", title = "Target")
    )

    assertEquals(0, updated.currentTabIndex)
    assertFalse(updated.tabs[0].isHome())
    assertEquals("https://target.com", updated.tabs[0].url)
}
```

- [ ] **Step 4: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserTabSessionManagerTest"`
Expected: FAIL because the session sanitizer still forces home at index 0.

- [ ] **Step 5: Implement the minimal browser session changes**

Update `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSession.kt`:
- Add `replaceCurrent(session: BrowserTabSession, webTab: WebTab): BrowserTabSession`
- Relax `sanitize()` so it no longer force-inserts a home tab after startup
- Keep `defaultSession()` unchanged so cold start still begins with home

- [ ] **Step 6: Run the targeted tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserTabSessionManagerTest"`
Expected: PASS for both new replacement cases.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSession.kt app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserTabSessionManagerTest.kt
git commit -m "feat: support replacing current browser tab"
```

### Task 2: Add browser event flow for overwrite behavior

**Files:**
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserViewModel.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserFragment.kt`

- [ ] **Step 1: Write the failing test for result contract mapping**

Create `app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserSelectionResultContractTest.kt` with a pure-Kotlin helper expectation:

```kotlin
@Test
fun `selection result with url maps to replace current tab request`() {
    val request = BrowserSelectionResultMapper.map(
        url = "https://target.com",
        title = "Target"
    )

    assertEquals("https://target.com", request.url)
    assertEquals("Target", request.title)
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContractTest"`
Expected: FAIL because mapper/helper does not exist yet.

- [ ] **Step 3: Implement the minimal overwrite event path**

Update browser flow:
- Add a new replace-current event in `BrowserViewModel`
- In `BrowserFragment`, observe the new event and call `BrowserTabSessionManager.replaceCurrent(...)`
- Add a small mapper/helper if it keeps result parsing isolated and testable

- [ ] **Step 4: Run the targeted tests to verify they pass**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContractTest"`
Expected: PASS

- [ ] **Step 5: Run browser tab tests again**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserTabSessionManagerTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserViewModel.kt app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserFragment.kt app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserSelectionResultContractTest.kt
git commit -m "feat: wire browser overwrite tab result flow"
```

## Chunk 2: Standalone Bookmark And History Activities

### Task 3: Build the bookmark activity shell and result contract

**Files:**
- Create: `app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksActivity.kt`
- Create: `app/src/main/res/layout/activity_bookmarks.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksFragment.kt`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Write the failing test for bookmark result key constants if extracted**

If using a result-contract object, add a simple constant/assertion test first.

- [ ] **Step 2: Run test to verify it fails**

Run the smallest targeted test for the extracted helper/contract.

- [ ] **Step 3: Implement `BookmarksActivity`**

Requirements:
- Host the bookmark UI as an activity screen
- Apply `DownloaderAppTheme`
- Register back/search/add interactions
- On item click, return `RESULT_OK` with selected URL/title

- [ ] **Step 4: Adapt bookmark screen logic for activity hosting**

Update `BookmarksFragment.kt` so it can run without assuming `MainActivity` back stack semantics, or extract the shared list logic into activity-friendly helpers while preserving add/search/reorder/delete behavior.

- [ ] **Step 5: Implement Figma-aligned bookmark layout**

Match node `37:154`:
- gray background `#F5F5F5`
- top bar with back/title/search
- white rounded 12dp card container
- list row spacing, divider color `#E7E7E8`
- yellow floating add button

- [ ] **Step 6: Run targeted tests**

Run bookmark-related targeted tests plus any new helper tests you introduced.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksActivity.kt app/src/main/res/layout/activity_bookmarks.xml app/src/main/java/com/myAllVideoBrowser/ui/main/bookmarks/BookmarksFragment.kt app/src/main/AndroidManifest.xml app/src/main/res/values/colors.xml app/src/main/res/values/strings.xml
git commit -m "feat: add standalone bookmark activity"
```

### Task 4: Build the history activity shell and result contract

**Files:**
- Create: `app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryActivity.kt`
- Create: `app/src/main/res/layout/activity_history.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryFragment.kt`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/strings.xml`

- [ ] **Step 1: Write the failing test for history result key/constants if extracted**

Add the contract test before implementation if a dedicated helper is used.

- [ ] **Step 2: Run test to verify it fails**

Run the smallest targeted test for the history contract/helper.

- [ ] **Step 3: Implement `HistoryActivity`**

Requirements:
- Host the history UI as an activity screen
- Return selected URL/title through `setResult`
- Preserve delete/search/clear-history actions

- [ ] **Step 4: Adapt history screen logic for activity hosting**

Update `HistoryFragment.kt` so it no longer depends on `parentFragmentManager.popBackStack()` or singleton tab-opening behavior. Route selection through the activity result callback instead.

- [ ] **Step 5: Implement Figma-aligned history layout**

Match node `14:73`:
- top bar with back/title/search
- white rounded list card
- bottom fixed `Clear History` action area
- typography and colors aligned with Figma

- [ ] **Step 6: Run targeted tests**

Run history-related targeted tests plus any helper tests added in this chunk.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryActivity.kt app/src/main/res/layout/activity_history.xml app/src/main/java/com/myAllVideoBrowser/ui/main/history/HistoryFragment.kt app/src/main/AndroidManifest.xml app/src/main/res/values/colors.xml app/src/main/res/values/strings.xml
git commit -m "feat: add standalone history activity"
```

## Chunk 3: Launchers, Result Handling, And Regression Coverage

### Task 5: Replace fragment launches with activity launches

**Files:**
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BaseWebTabFragment.kt`
- Modify: `app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/homeTab/BrowserHomeFragment.kt`

- [ ] **Step 1: Write the failing test for selection result handling helper**

If needed, add/extend a helper test so one URL result leads to the replace-current event instead of open-new-tab.

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContractTest"`
Expected: FAIL before wiring launchers.

- [ ] **Step 3: Implement activity launchers**

Use `registerForActivityResult(StartActivityForResult())` in the relevant browser hosts:
- Web tab menu entry opens activity
- Home shortcut opens activity
- On success, parse the returned URL and trigger replace-current event

- [ ] **Step 4: Remove old fragment transaction navigation for bookmark/history**

Delete or stop using the `navigateToHistory()` / `navigateToBookMarks()` fragment-overlay path once activity flow is fully wired.

- [ ] **Step 5: Run targeted tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContractTest"`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/BaseWebTabFragment.kt app/src/main/java/com/myAllVideoBrowser/ui/main/home/browser/homeTab/BrowserHomeFragment.kt app/src/test/java/com/myAllVideoBrowser/ui/main/home/browser/BrowserSelectionResultContractTest.kt
git commit -m "feat: launch bookmark and history activities from browser"
```

### Task 6: Final verification

**Files:**
- No new files unless verification reveals gaps

- [ ] **Step 1: Run focused browser tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserTabSessionManagerTest"`
Expected: PASS

- [ ] **Step 2: Run focused result-contract tests**

Run: `.\gradlew.bat :app:testDebugUnitTest --tests "com.myAllVideoBrowser.ui.main.home.browser.BrowserSelectionResultContractTest"`
Expected: PASS

- [ ] **Step 3: Run any bookmark/history targeted tests added during implementation**

Run the exact commands for the new targeted tests you introduced.

- [ ] **Step 4: Run the broader unit test suite if the build is stable**

Run: `.\gradlew.bat :app:testDebugUnitTest`
Expected: PASS, or document the exact pre-existing failures if blocked by unrelated build issues.

- [ ] **Step 5: Manual QA**

Verify on device/emulator:
- Open bookmark activity from home and select an item: current tab slot becomes the target page
- Open history activity from home and select an item: current tab slot becomes the target page
- Open bookmark/history from a normal web tab and select an item: same tab is overwritten
- Bookmark add button works
- History clear button works
- Search and empty states match Figma

- [ ] **Step 6: Commit final fixes**

```bash
git add app/src/main
git add app/src/test
git commit -m "feat: finish bookmark and history activity flow"
```
