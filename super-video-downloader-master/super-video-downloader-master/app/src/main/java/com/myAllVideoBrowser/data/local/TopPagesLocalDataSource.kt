package com.myAllVideoBrowser.data.local

import com.myAllVideoBrowser.data.local.room.dao.PageDao
import com.myAllVideoBrowser.data.local.room.entity.PageInfo
import com.myAllVideoBrowser.data.repository.TopPagesRepository
import com.myAllVideoBrowser.util.CopyrightRestrictedSitePolicy
import com.myAllVideoBrowser.util.PopularSiteIconRegistry
import com.myAllVideoBrowser.util.SharedPrefHelper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TopPagesLocalDataSource @Inject constructor(
    private val pageDao: PageDao,
    private val sharedPrefHelper: SharedPrefHelper
) : TopPagesRepository {

    private val figmaPopularSites = listOf(
        PageInfo(
            name = "Facebook",
            link = "https://www.facebook.com",
            icon = PopularSiteIconRegistry.FACEBOOK
        ),
        PageInfo(
            name = "Instagram",
            link = "https://www.instagram.com",
            icon = PopularSiteIconRegistry.INSTAGRAM
        ),
        PageInfo(
            name = "TikTok",
            link = "https://www.tiktok.com",
            icon = PopularSiteIconRegistry.TIKTOK
        ),
        PageInfo(
            name = "X(Twitter)",
            link = "https://x.com",
            icon = PopularSiteIconRegistry.X
        ),
        PageInfo(
            name = "Vimeo",
            link = "https://vimeo.com",
            icon = PopularSiteIconRegistry.VIMEO
        ),
        PageInfo(
            name = "Pinterest",
            link = "https://www.pinterest.com",
            icon = PopularSiteIconRegistry.PINTEREST
        ),
        PageInfo(
            name = "Google",
            link = "https://www.google.com",
            icon = PopularSiteIconRegistry.GOOGLE
        ),
    )

    private val legacyPopularLinks = setOf(
        "https://www.imdb.com",
        "https://github.com/DNSCrypt/dnscrypt-resolvers/blob/master/v3/public-resolvers.md",
        "https://www.dailymotion.com",
        "https://www.instagram.com",
        "https://www.twitter.com",
        "https://www.pinterest.com/videos",
        "https://www.twitch.tv",
    )

    override suspend fun getTopPages(): List<PageInfo> {
        val localBookmarks = pageDao.getPageInfos().blockingFirst(emptyList())
        if (localBookmarks.isEmpty()) {
            val isFirstStart = sharedPrefHelper.getIsFirstStart()
            if (isFirstStart) {
                val defaultList = getDefaultBookmarks()
                pageDao.insertAllProgressInfo(defaultList)

                return defaultList
            }
        }

        val migratedBookmarks = migratePopularSitesIfNeeded(localBookmarks)
        if (migratedBookmarks != null) {
            pageDao.deleteAll()
            pageDao.insertAllProgressInfo(migratedBookmarks)
            return migratedBookmarks
        }

        return localBookmarks
    }

    override fun saveTopPage(pageInfo: PageInfo) {
        pageDao.insertProgressInfo(pageInfo)
    }

    override fun replaceBookmarksWith(pageInfos: List<PageInfo>) {
        pageDao.deleteAll()
        pageDao.insertAllProgressInfo(pageInfos)
    }

    override fun deletePageInfo(pageInfo: PageInfo) {
        pageDao.deleteProgressInfo(pageInfo)
    }

    override suspend fun updateLocalStorageFavicons(): Flow<PageInfo> {
        throw NotImplementedError("NO NEED, HANDLED BY REPO")
    }

    private fun getDefaultBookmarks(): List<PageInfo> {
        return figmaPopularSites.mapIndexed { index, page ->
            page.copy(order = index)
        }
    }

    private fun migratePopularSitesIfNeeded(localBookmarks: List<PageInfo>): List<PageInfo>? {
        if (localBookmarks.isEmpty()) {
            return null
        }

        val sanitizedBookmarks = localBookmarks.filterNot { bookmark ->
            bookmark.isSystem && CopyrightRestrictedSitePolicy.isDownloadRestrictedUrl(bookmark.link)
        }
        if (sanitizedBookmarks.size != localBookmarks.size) {
            return sanitizedBookmarks.mapIndexed { index, page ->
                page.copy(order = index)
            }
        }

        val existingLinks = sanitizedBookmarks.map { it.link }.toSet()
        val newLinks = figmaPopularSites.map { it.link }.toSet()

        val shouldReplaceLegacyDefaults = existingLinks == legacyPopularLinks
        val shouldFillMissingFigmaDefaults =
            existingLinks.isNotEmpty() &&
                existingLinks.subtract(newLinks).isEmpty() &&
                sanitizedBookmarks.size < figmaPopularSites.size

        if (!shouldReplaceLegacyDefaults && !shouldFillMissingFigmaDefaults) {
            return null
        }

        return figmaPopularSites.mapIndexed { index, defaultPage ->
            val existingPage = sanitizedBookmarks.firstOrNull { it.link == defaultPage.link }
            val page = existingPage?.copy() ?: defaultPage.copy()
            page.name = defaultPage.name
            page.link = defaultPage.link
            page.icon = defaultPage.icon
            page.order = index
            page
        }
    }
}
