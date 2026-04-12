package com.myAllVideoBrowser.ui.main.home.browser

import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserTabSessionManagerTest {

    @Test
    fun `open appends tab and selects it`() {
        val session = BrowserTabSessionManager.open(
            BrowserTabSessionManager.defaultSession(),
            WebTab(url = "https://www.google.com/", title = "Google", id = "google")
        )

        assertEquals(2, session.tabs.size)
        assertEquals(1, session.currentTabIndex)
        assertEquals("google", session.tabs.last().id)
    }

    @Test
    fun `close keeps home tab and selects previous tab when closing current`() {
        val initial = BrowserTabSession(
            tabs = listOf(
                PersistedBrowserTab.home(),
                PersistedBrowserTab(id = "tab-a", url = "https://a.example"),
                PersistedBrowserTab(id = "tab-b", url = "https://b.example")
            ),
            currentTabIndex = 2
        )

        val session = BrowserTabSessionManager.close(initial, "tab-b")

        assertEquals(listOf("home", "tab-a"), session.tabs.map { it.id })
        assertEquals(1, session.currentTabIndex)
    }

    @Test
    fun `close ignores home tab`() {
        val initial = BrowserTabSessionManager.defaultSession()

        val session = BrowserTabSessionManager.close(initial, "home")

        assertEquals(listOf("home"), session.tabs.map { it.id })
        assertEquals(0, session.currentTabIndex)
    }

    @Test
    fun `clear resets session to single home tab`() {
        val session = BrowserTabSessionManager.clear()

        assertEquals(1, session.tabs.size)
        assertTrue(session.tabs.first().isHome())
        assertEquals(0, session.currentTabIndex)
    }

    @Test
    fun `sanitize keeps provided tabs and clamps index`() {
        val session = BrowserTabSessionManager.sanitize(
            tabs = listOf(PersistedBrowserTab(id = "tab-a", url = "https://a.example")),
            currentTabIndex = 4
        )

        assertEquals(listOf("tab-a"), session.tabs.map { it.id })
        assertEquals(0, session.currentTabIndex)
    }

    @Test
    fun `replace current swaps selected non home tab and keeps index`() {
        val initial = BrowserTabSession(
            tabs = listOf(
                PersistedBrowserTab.home(),
                PersistedBrowserTab(id = "tab-a", url = "https://a.example", title = "A"),
                PersistedBrowserTab(id = "tab-b", url = "https://b.example", title = "B")
            ),
            currentTabIndex = 2
        )

        val session = BrowserTabSessionManager.replaceCurrent(
            initial,
            WebTab(url = "https://target.example", title = "Target")
        )

        assertEquals(2, session.currentTabIndex)
        assertEquals("https://target.example", session.tabs[2].url)
        assertEquals("Target", session.tabs[2].title)
        assertFalse(session.tabs[2].isHome())
    }

    @Test
    fun `replace current allows replacing the home slot with a regular page`() {
        val session = BrowserTabSessionManager.replaceCurrent(
            BrowserTabSessionManager.defaultSession(),
            WebTab(url = "https://target.example", title = "Target")
        )

        assertEquals(0, session.currentTabIndex)
        assertEquals(1, session.tabs.size)
        assertEquals("https://target.example", session.tabs[0].url)
        assertFalse(session.tabs[0].isHome())
    }

    @Test
    fun `from runtime tabs preserves non home metadata`() {
        val session = BrowserTabSessionManager.fromRuntimeTabs(
            tabs = listOf(
                WebTab.HOME_TAB,
                WebTab(
                    url = "https://example.com",
                    title = "Example",
                    headers = mapOf("User-Agent" to "test"),
                    id = "tab-1"
                )
            ),
            currentTabIndex = 1
        )

        assertEquals(2, session.tabs.size)
        assertEquals("https://example.com", session.tabs[1].url)
        assertEquals("Example", session.tabs[1].title)
        assertEquals("test", session.tabs[1].headers["User-Agent"])
    }

    @Test
    fun `from runtime tabs preserves tab preview bytes`() {
        val previewBytes = byteArrayOf(1, 2, 3, 4)

        val session = BrowserTabSessionManager.fromRuntimeTabs(
            tabs = listOf(
                WebTab.HOME_TAB,
                WebTab(
                    url = "https://example.com",
                    title = "Example",
                    previewBytes = previewBytes,
                    id = "tab-1"
                )
            ),
            currentTabIndex = 1
        )

        assertTrue(session.tabs[1].previewBytes!!.contentEquals(previewBytes))
    }

    @Test
    fun `to runtime tabs restores persisted preview bytes`() {
        val previewBytes = byteArrayOf(9, 8, 7)

        val runtimeTabs = BrowserTabSessionManager.toRuntimeTabs(
            BrowserTabSession(
                tabs = listOf(
                    PersistedBrowserTab.home(),
                    PersistedBrowserTab(
                        id = "tab-1",
                        url = "https://example.com",
                        title = "Example",
                        previewBytes = previewBytes
                    )
                ),
                currentTabIndex = 1
            )
        )

        assertTrue(runtimeTabs[1].getPreviewBytes().contentEquals(previewBytes))
    }

    @Test
    fun `launch resets to default session on cold start`() {
        val launchSession = BrowserTabSessionManager.sessionForLaunch(
            restoredSession = BrowserTabSession(
                tabs = listOf(
                    PersistedBrowserTab.home(),
                    PersistedBrowserTab(id = "tab-1", url = "https://example.com", title = "Example")
                ),
                currentTabIndex = 1
            ),
            isColdStart = true
        )

        assertEquals(1, launchSession.tabs.size)
        assertTrue(launchSession.tabs.first().isHome())
        assertEquals(0, launchSession.currentTabIndex)
    }

    @Test
    fun `launch keeps restored tabs but resets selection to home on warm recreation`() {
        val restoredSession = BrowserTabSession(
            tabs = listOf(
                PersistedBrowserTab.home(),
                PersistedBrowserTab(id = "tab-1", url = "https://example.com", title = "Example")
            ),
            currentTabIndex = 1
        )

        val launchSession = BrowserTabSessionManager.sessionForLaunch(
            restoredSession = restoredSession,
            isColdStart = false
        )

        assertEquals(listOf("home", "tab-1"), launchSession.tabs.map { it.id })
        assertEquals(0, launchSession.currentTabIndex)
    }

    @Test
    fun `launch restores a home tab first when previous session replaced it`() {
        val restoredSession = BrowserTabSession(
            tabs = listOf(
                PersistedBrowserTab(id = "tab-1", url = "https://example.com", title = "Example"),
                PersistedBrowserTab(id = "tab-2", url = "https://openai.com", title = "OpenAI")
            ),
            currentTabIndex = 1
        )

        val launchSession = BrowserTabSessionManager.sessionForLaunch(
            restoredSession = restoredSession,
            isColdStart = false
        )

        assertEquals(listOf("home", "tab-1", "tab-2"), launchSession.tabs.map { it.id })
        assertEquals(0, launchSession.currentTabIndex)
    }
}
