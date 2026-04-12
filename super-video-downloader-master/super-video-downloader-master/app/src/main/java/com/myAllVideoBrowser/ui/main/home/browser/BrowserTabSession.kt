package com.myAllVideoBrowser.ui.main.home.browser

import com.myAllVideoBrowser.ui.main.home.browser.webTab.WebTab

data class PersistedBrowserTab(
    val id: String,
    val url: String,
    val title: String? = null,
    val previewBytes: ByteArray? = null,
    val headers: Map<String, String> = emptyMap()
) {
    fun isHome(): Boolean = id == WebTab.HOME_TAB.id

    fun toWebTab(): WebTab {
        return if (isHome()) {
            WebTab.HOME_TAB
        } else {
            WebTab(
                url = url,
                title = title,
                previewBytes = previewBytes,
                headers = headers,
                id = id
            )
        }
    }

    companion object {
        fun home(): PersistedBrowserTab = PersistedBrowserTab(
            id = WebTab.HOME_TAB.id,
            url = WebTab.HOME_TAB.getUrl(),
            title = WebTab.HOME_TAB.getTitle(),
            headers = emptyMap()
        )

        fun fromWebTab(webTab: WebTab): PersistedBrowserTab = PersistedBrowserTab(
            id = webTab.id,
            url = webTab.getUrl(),
            title = webTab.getTitle().ifBlank { null },
            previewBytes = webTab.getPreviewBytes().takeIf { it.isNotEmpty() },
            headers = webTab.getHeaders() ?: emptyMap()
        )
    }
}

data class BrowserTabSession(
    val tabs: List<PersistedBrowserTab>,
    val currentTabIndex: Int
)

data class BrowserTabSessionPayload(
    val tabs: List<PersistedBrowserTab> = emptyList(),
    val currentTabIndex: Int = HOME_TAB_INDEX
)

object BrowserTabSessionManager {
    fun defaultSession(): BrowserTabSession = BrowserTabSession(
        tabs = listOf(PersistedBrowserTab.home()),
        currentTabIndex = HOME_TAB_INDEX
    )

    fun sessionForLaunch(
        restoredSession: BrowserTabSession,
        isColdStart: Boolean
    ): BrowserTabSession {
        return if (isColdStart) {
            defaultSession()
        } else {
            prepareForLaunch(restoredSession)
        }
    }

    private fun prepareForLaunch(session: BrowserTabSession): BrowserTabSession {
        val sanitized = sanitize(session.tabs, session.currentTabIndex)
        val homeIndex = sanitized.tabs.indexOfFirst { it.isHome() }
        val launchTabs = when {
            homeIndex == 0 -> sanitized.tabs
            homeIndex > 0 -> listOf(sanitized.tabs[homeIndex]) + sanitized.tabs.filterIndexed { index, _ ->
                index != homeIndex
            }
            else -> listOf(PersistedBrowserTab.home()) + sanitized.tabs
        }
        return sanitize(launchTabs, HOME_TAB_INDEX)
    }

    fun sanitize(
        tabs: List<PersistedBrowserTab>?,
        currentTabIndex: Int?
    ): BrowserTabSession {
        val normalizedTabs = tabs
            ?.filterNot { it.id.isBlank() }
            ?.toMutableList()
            ?: mutableListOf()

        if (normalizedTabs.isEmpty()) {
            normalizedTabs.add(0, PersistedBrowserTab.home())
        }

        val safeIndex = (currentTabIndex ?: HOME_TAB_INDEX)
            .coerceIn(HOME_TAB_INDEX, normalizedTabs.lastIndex)

        return BrowserTabSession(
            tabs = normalizedTabs.toList(),
            currentTabIndex = safeIndex
        )
    }

    fun open(session: BrowserTabSession, webTab: WebTab): BrowserTabSession {
        val newTabs = session.tabs + PersistedBrowserTab.fromWebTab(webTab)
        return BrowserTabSession(
            tabs = newTabs,
            currentTabIndex = newTabs.lastIndex
        )
    }

    fun close(session: BrowserTabSession, tabId: String): BrowserTabSession {
        val currentTabs = session.tabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.id == tabId }
        if (index < 0 || currentTabs[index].isHome()) {
            return sanitize(currentTabs, session.currentTabIndex)
        }

        currentTabs.removeAt(index)
        val newIndex = when {
            session.currentTabIndex > index -> session.currentTabIndex - 1
            session.currentTabIndex == index -> (index - 1).coerceAtLeast(HOME_TAB_INDEX)
            else -> session.currentTabIndex
        }

        return sanitize(currentTabs, newIndex)
    }

    fun select(session: BrowserTabSession, tabId: String): BrowserTabSession {
        val index = session.tabs.indexOfFirst { it.id == tabId }
        if (index < 0) {
            return sanitize(session.tabs, session.currentTabIndex)
        }
        return sanitize(session.tabs, index)
    }

    fun update(session: BrowserTabSession, webTab: WebTab): BrowserTabSession {
        val currentTabs = session.tabs.toMutableList()
        val index = currentTabs.indexOfFirst { it.id == webTab.id }
        if (index < 0) {
            return sanitize(currentTabs, session.currentTabIndex)
        }
        currentTabs[index] = PersistedBrowserTab.fromWebTab(webTab)
        return sanitize(currentTabs, session.currentTabIndex)
    }

    fun replaceCurrent(session: BrowserTabSession, webTab: WebTab): BrowserTabSession {
        val currentTabs = sanitize(session.tabs, session.currentTabIndex).tabs.toMutableList()
        val safeIndex = session.currentTabIndex.coerceIn(HOME_TAB_INDEX, currentTabs.lastIndex)
        currentTabs[safeIndex] = PersistedBrowserTab.fromWebTab(webTab)
        return sanitize(currentTabs, safeIndex)
    }

    fun clear(): BrowserTabSession = defaultSession()

    fun toRuntimeTabs(session: BrowserTabSession): List<WebTab> {
        return sanitize(session.tabs, session.currentTabIndex).tabs.map { it.toWebTab() }
    }

    fun fromRuntimeTabs(
        tabs: List<WebTab>?,
        currentTabIndex: Int
    ): BrowserTabSession {
        val persistedTabs = tabs?.map(PersistedBrowserTab.Companion::fromWebTab)
        return sanitize(persistedTabs, currentTabIndex)
    }
}
