package com.myAllVideoBrowser.ui.main.history

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.FragmentHistoryBinding
import com.myAllVideoBrowser.ui.component.adapter.HistoryAdapter
import com.myAllVideoBrowser.ui.component.adapter.HistoryListener
import com.myAllVideoBrowser.ui.component.adapter.HistorySearchAdapter
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.progress.WrapContentLinearLayoutManager
import com.myAllVideoBrowser.util.AppLogger
import javax.inject.Inject

class HistoryFragment : BaseFragment() {

    interface HistorySelectionHost {
        fun onHistorySelected(url: String, title: String?)
        fun onHistoryClosed()
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }

    private lateinit var historyModel: HistoryViewModel
    private lateinit var dataBinding: FragmentHistoryBinding
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var searchHistoryAdapter: HistorySearchAdapter

    private var isSearchUiVisible = false

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        historyModel = ViewModelProvider(this, viewModelFactory)[HistoryViewModel::class.java]

        historyAdapter = HistoryAdapter(emptyList(), historyListener)
        searchHistoryAdapter = HistorySearchAdapter(emptyList(), searchHistoryListener)

        dataBinding = FragmentHistoryBinding.inflate(inflater, container, false).apply {
            viewModel = historyModel

            val historyManagerLayout =
                WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            historyList.layoutManager = historyManagerLayout
            historyList.adapter = historyAdapter

            val searchHistoryManagerLayout =
                WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            historySearchList.layoutManager = searchHistoryManagerLayout
            historySearchList.adapter = searchHistoryAdapter

            searchInput.setText(historyModel.searchQuery.get().orEmpty())
            searchInput.addTextChangedListener(searchTextChangeListener)

            backButton.setOnClickListener {
                selectionHost()?.onHistoryClosed() ?: requireActivity().finish()
            }
            searchToggleButton.setOnClickListener {
                toggleSearchUi(!isSearchUiVisible)
            }
            clearHistoryButton.setOnClickListener {
                searchInput.text?.clear()
                historyModel.clearHistory()
            }
            searchClearButton.setOnClickListener {
                searchInput.text?.clear()
            }
            searchEmptyClearButton.setOnClickListener {
                searchInput.text?.clear()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            selectionHost()?.onHistoryClosed() ?: requireActivity().finish()
        }

        return dataBinding.root
    }

    private val historyListener = object : HistoryListener {
        override fun onHistoryOpenClicked(view: View, id: String) {
            openHistoryItem(id)
        }

        override fun onHistoryDeleteClicked(view: View, id: String) = Unit

        override fun onMenuClicked(view: View, id: String) {
            showPopupMenu(view, id)
        }

        override fun onAllHistoryDeleteClicked() = Unit
    }

    private val searchHistoryListener = object : HistoryListener {
        override fun onHistoryOpenClicked(view: View, id: String) {
            openHistoryItem(id)
        }

        override fun onHistoryDeleteClicked(view: View, id: String) = Unit

        override fun onMenuClicked(view: View, id: String) {
            showPopupMenu(view, id)
        }

        override fun onAllHistoryDeleteClicked() = Unit
    }

    private fun selectionHost(): HistorySelectionHost? {
        return activity as? HistorySelectionHost
    }

    private fun openHistoryItem(id: String) {
        AppLogger.d("openHistoryItem: $id")
        val item = historyModel.historyItems.get()?.find { it.id == id }
            ?: historyModel.searchHistoryItems.get()?.find { it.id == id }
            ?: return
        selectionHost()?.onHistorySelected(item.url, item.title)
            ?: requireActivity().finish()
    }

    private val searchTextChangeListener = object : TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            historyModel.queryHistory(s?.toString().orEmpty())
            if (!s.isNullOrEmpty()) {
                isSearchUiVisible = true
            }
            renderSearchUi()
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyModel.start()
        renderSearchUi()
    }

    override fun onDestroyView() {
        dataBinding.searchInput.removeTextChangedListener(searchTextChangeListener)
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        historyModel.stop()
    }

    private fun toggleSearchUi(visible: Boolean) {
        isSearchUiVisible = visible
        if (!visible) {
            dataBinding.searchInput.text?.clear()
        } else {
            dataBinding.searchInput.requestFocus()
        }
        renderSearchUi()
    }

    private fun renderSearchUi() {
        val shouldShowSearch = isSearchUiVisible || historyModel.isSearchActive.get()
        dataBinding.searchBox.visibility = if (shouldShowSearch) View.VISIBLE else View.GONE
    }

    private fun showPopupMenu(view: View, historyId: String) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.menuInflater.inflate(R.menu.menu_history, popupMenu.menu)
        popupMenu.setForceShowIcon(true)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.item_remove -> {
                    historyModel.historyItems.get()?.find { it.id == historyId }
                        ?.let { historyModel.deleteHistory(it) }
                    true
                }

                else -> false
            }
        }
    }
}
