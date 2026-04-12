package com.myAllVideoBrowser.ui.main.history

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.viewModelScope
import com.myAllVideoBrowser.data.local.room.entity.HistoryItem
import com.myAllVideoBrowser.data.repository.HistoryRepository
import com.myAllVideoBrowser.ui.main.base.BaseViewModel
import com.myAllVideoBrowser.util.scheduler.BaseSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import javax.inject.Inject

class HistoryViewModel @Inject constructor(
    private val historyRepository: HistoryRepository,
) :
    BaseViewModel() {

    var historyItems = ObservableField<List<HistoryItem>>(emptyList())

    var searchHistoryItems = ObservableField<List<HistoryItem>>(emptyList())

    val searchQuery = ObservableField("")

    val isLoadingHistory = ObservableBoolean(true)
    val isSearchActive = ObservableBoolean(false)
    val isHistoryEmpty = ObservableBoolean(true)
    val isSearchResultsEmpty = ObservableBoolean(false)

    val executorSingleHistory = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val historyExecutor = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    private val additionalExecutor = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

    override fun start() {
        fetchAllHistory()
    }

    override fun stop() {
    }

    private fun fetchAllHistory() {
        isLoadingHistory.set(true)

        viewModelScope.launch(additionalExecutor) {
            val history = historyRepository.getAllHistory().blockingFirst()
            historyItems.set(history)
            isHistoryEmpty.set(history.isEmpty())
            queryHistory(searchQuery.get().orEmpty())
            isLoadingHistory.set(false)
        }
    }

    fun saveHistory(historyItem: HistoryItem) {
        viewModelScope.launch(historyExecutor) {
            try {
                historyRepository.saveHistory(historyItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun deleteHistory(historyItem: HistoryItem) {
        viewModelScope.launch(historyExecutor) {
            try {
                val newItems = historyItems.get()?.filter { it.id != historyItem.id }
                historyItems.set(newItems)
                isHistoryEmpty.set(newItems.isNullOrEmpty())
                queryHistory(searchQuery.get().orEmpty())
                historyRepository.deleteHistory(historyItem)
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun queryHistory(query: String) {
        searchQuery.set(query)
        val normalizedQuery = query.trim()
        isSearchActive.set(normalizedQuery.isNotEmpty())

        if (normalizedQuery.isEmpty()) {
            searchHistoryItems.set(emptyList())
            isSearchResultsEmpty.set(false)
        }
        if (normalizedQuery.isNotEmpty()) {
            val filtered = historyItems.get()
                ?.filter {
                    it.url.contains(normalizedQuery, ignoreCase = true) ||
                        it.title?.contains(normalizedQuery, ignoreCase = true) == true
                }
            searchHistoryItems.set(filtered ?: emptyList())
            isSearchResultsEmpty.set(filtered.isNullOrEmpty())
        }
    }

    fun clearHistory() {
        viewModelScope.launch(historyExecutor) {
            isLoadingHistory.set(true)
            historyRepository.deleteAllHistory()
            historyItems.set(emptyList())
            searchHistoryItems.set(emptyList())
            searchQuery.set("")
            isSearchActive.set(false)
            isHistoryEmpty.set(true)
            isSearchResultsEmpty.set(false)
            isLoadingHistory.set(false)
        }
    }
}
