package com.myAllVideoBrowser.ui.main.bookmarks

import android.graphics.Bitmap
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.myAllVideoBrowser.data.local.room.entity.PageInfo
import com.myAllVideoBrowser.data.repository.TopPagesRepository
import com.myAllVideoBrowser.ui.main.base.BaseViewModel
import com.myAllVideoBrowser.util.FaviconUtils
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

class BookmarksViewModel @Inject constructor(
    private val topPagesRepository: TopPagesRepository,
) : BaseViewModel() {

    val bookmarksList: ObservableField<MutableList<PageInfo>> = ObservableField(mutableListOf())

    private val executorSingle = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val executorMoverSingle = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    override fun start() {
        updateTopPages()
    }

    override fun stop() {
    }

    fun bookmark(url: String, name: String, favicon: Bitmap?) {
        viewModelScope.launch(executorMoverSingle) {
            var bookmarks = topPagesRepository.getTopPages().toMutableList()
            val faviconBytes = FaviconUtils.bitmapToBytes(favicon)
            val newBookmark = PageInfo(
                link = url,
                order = bookmarks.size,
                name = name,
                favicon = faviconBytes
            )
            bookmarks.add(newBookmark)
            bookmarks = bookmarks.mapIndexed { index, pageInfo ->
                pageInfo.order = index
                pageInfo
            }.toMutableList()
            bookmarksList.set(bookmarks)
            topPagesRepository.replaceBookmarksWith(bookmarks)
        }
    }

    fun updateBookmarks(bookmarks: List<PageInfo>) {
        viewModelScope.launch(executorMoverSingle) {
            val updatedBookmarks = bookmarks.mapIndexed { index, value ->
                value.order = index
                value
            }
            topPagesRepository.replaceBookmarksWith(updatedBookmarks)
            bookmarksList.set(bookmarks.toMutableList())
        }
    }

    private fun updateTopPages() {
        viewModelScope.launch(executorSingle) {
            val pages = try {
                topPagesRepository.getTopPages()
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }

            if (!pages.isNullOrEmpty()) {
                bookmarksList.set(pages.toMutableList())
            }

            try {
                topPagesRepository.updateLocalStorageFavicons().collect { pageInfo ->
                    val currentBookmarks = bookmarksList.get()?.toMutableList() ?: mutableListOf()
                    val index = currentBookmarks.indexOfFirst { it.link == pageInfo.link }
                    if (index >= 0) {
                        currentBookmarks[index] = pageInfo
                    } else {
                        currentBookmarks.add(pageInfo)
                    }
                    bookmarksList.set(currentBookmarks)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}
