package com.myAllVideoBrowser.ui.main.bookmarks

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.Observable
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.myAllVideoBrowser.data.local.room.entity.PageInfo
import com.myAllVideoBrowser.databinding.FragmentBookmarksBinding
import com.myAllVideoBrowser.ui.component.adapter.BookmarksAdapter
import com.myAllVideoBrowser.ui.component.adapter.BookmarksListener
import com.myAllVideoBrowser.ui.component.adapter.ReorderableItemTouchHelperCallback
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.ui.main.bookmarks.dialogs.BookmarksDialogLauncher
import com.myAllVideoBrowser.ui.main.bookmarks.dialogs.BookmarksDialogResults
import com.myAllVideoBrowser.ui.main.progress.WrapContentLinearLayoutManager
import javax.inject.Inject

class BookmarksFragment : BaseFragment() {

    interface BookmarkSelectionHost {
        fun onBookmarkSelected(url: String, title: String?)
        fun onBookmarkClosed()
    }

    private lateinit var dataBinding: FragmentBookmarksBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var bookmarksViewModel: BookmarksViewModel
    private lateinit var bookmarksAdapter: BookmarksAdapter
    private lateinit var searchBookmarksAdapter: BookmarksAdapter

    private var bookmarksCached = mutableListOf<PageInfo>()
    private var currentBookmarks = mutableListOf<PageInfo>()
    private var currentQuery = ""
    private var hasChanged = false
    private var isSearchUiVisible = false

    private val bookmarksObserver = object : Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            dataBinding.root.post {
                currentBookmarks = bookmarksViewModel.bookmarksList.get()?.toMutableList()
                    ?: mutableListOf()
                if (!hasChanged) {
                    bookmarksCached = currentBookmarks.toMutableList()
                }
                renderBookmarks()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = BookmarksFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bookmarksViewModel =
            ViewModelProvider(this, viewModelFactory)[BookmarksViewModel::class.java]

        bookmarksAdapter = BookmarksAdapter(mutableListOf(), listener)
        searchBookmarksAdapter = BookmarksAdapter(mutableListOf(), listener)
        val touchHelperCallback = ReorderableItemTouchHelperCallback(bookmarksAdapter)
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)

        dataBinding = FragmentBookmarksBinding.inflate(inflater, container, false).apply {
            bookmarksList.layoutManager =
                WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            bookmarksList.adapter = bookmarksAdapter
            searchBookmarksList.layoutManager =
                WrapContentLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            searchBookmarksList.adapter = searchBookmarksAdapter
            itemTouchHelper.attachToRecyclerView(bookmarksList)

            backButton.setOnClickListener {
                selectionHost()?.onBookmarkClosed() ?: requireActivity().finish()
            }
            bookmarksSearchToggleButton.setOnClickListener {
                toggleSearchUi(!isSearchUiVisible)
            }
            addBookmarkFab.setOnClickListener {
                BookmarksDialogLauncher.showAddBookmarkDialog(childFragmentManager)
            }
            bookmarksSearchInput.addTextChangedListener(searchTextWatcher)
            bookmarksSearchClearButton.setOnClickListener {
                bookmarksSearchInput.text?.clear()
            }
            bookmarksSearchEmptyClearButton.setOnClickListener {
                bookmarksSearchInput.text?.clear()
            }
        }

        return dataBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager.setFragmentResultListener(
            BookmarksDialogResults.RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            if (result.getString(BookmarksDialogResults.RESULT_ACTION) != BookmarksDialogResults.ACTION_SAVE) {
                return@setFragmentResultListener
            }

            val title = result.getString(BookmarksDialogResults.RESULT_TITLE).orEmpty()
            val url = result.getString(BookmarksDialogResults.RESULT_URL).orEmpty()
            if (title.isNotBlank() && url.isNotBlank()) {
                bookmarksViewModel.bookmark(url, title, null)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            selectionHost()?.onBookmarkClosed() ?: requireActivity().finish()
        }

        bookmarksViewModel.start()
        bookmarksViewModel.bookmarksList.addOnPropertyChangedCallback(bookmarksObserver)
        currentBookmarks = bookmarksViewModel.bookmarksList.get()?.toMutableList() ?: mutableListOf()
        bookmarksCached = currentBookmarks.toMutableList()
        renderBookmarks()
    }

    override fun onDestroyView() {
        bookmarksViewModel.bookmarksList.removeOnPropertyChangedCallback(bookmarksObserver)
        dataBinding.bookmarksSearchInput.removeTextChangedListener(searchTextWatcher)
        super.onDestroyView()
    }

    override fun onDestroy() {
        if (hasChanged) {
            bookmarksViewModel.updateBookmarks(bookmarksCached)
        }
        bookmarksViewModel.stop()
        super.onDestroy()
    }

    private fun selectionHost(): BookmarkSelectionHost? {
        return activity as? BookmarkSelectionHost
    }

    private val listener = object : BookmarksListener {
        override fun onBookmarkOpenClicked(view: View, bookmarkItem: PageInfo) {
            selectionHost()?.onBookmarkSelected(bookmarkItem.link, bookmarkItem.name)
                ?: requireActivity().finish()
        }

        override fun onBookmarkMove(bookmarks: MutableList<PageInfo>) {
            bookmarksCached = bookmarks.toMutableList()
            currentBookmarks = bookmarks.toMutableList()
            hasChanged = true
            renderBookmarks()
        }

        override fun onBookmarkDelete(bookmarks: MutableList<PageInfo>, position: Int) {
            bookmarks.removeAt(position)
            bookmarksCached = bookmarks.toMutableList()
            currentBookmarks = bookmarks.toMutableList()
            hasChanged = true
            renderBookmarks()
        }
    }

    private val searchTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            currentQuery = s?.toString().orEmpty()
            if (currentQuery.isNotEmpty()) {
                isSearchUiVisible = true
            }
            renderBookmarks()
        }

        override fun afterTextChanged(s: Editable?) = Unit
    }

    private fun toggleSearchUi(visible: Boolean) {
        isSearchUiVisible = visible
        if (!visible) {
            dataBinding.bookmarksSearchInput.text?.clear()
            currentQuery = ""
        } else {
            dataBinding.bookmarksSearchInput.requestFocus()
        }
        renderBookmarks()
    }

    private fun renderBookmarks() {
        val query = currentQuery.trim()
        val filteredBookmarks = if (query.isEmpty()) {
            currentBookmarks
        } else {
            currentBookmarks.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.link.contains(query, ignoreCase = true)
            }.toMutableList()
        }

        val shouldShowSearch = isSearchUiVisible || query.isNotEmpty()
        dataBinding.bookmarksSearchBox.visibility = if (shouldShowSearch) View.VISIBLE else View.GONE
        dataBinding.bookmarksSearchClearButton.visibility =
            if (query.isEmpty()) View.GONE else View.VISIBLE
        dataBinding.bookmarksSectionLabel.visibility = View.GONE
        dataBinding.bookmarksList.visibility =
            if (query.isEmpty() && currentBookmarks.isNotEmpty()) View.VISIBLE else View.GONE
        dataBinding.bookmarksEmptyState.visibility =
            if (query.isEmpty() && currentBookmarks.isEmpty()) View.VISIBLE else View.GONE
        dataBinding.bookmarksSearchResultsContainer.visibility =
            if (query.isNotEmpty() && filteredBookmarks.isNotEmpty()) View.VISIBLE else View.GONE
        dataBinding.bookmarksSearchEmptyState.visibility =
            if (query.isNotEmpty() && filteredBookmarks.isEmpty()) View.VISIBLE else View.GONE

        bookmarksAdapter.setData(currentBookmarks.toMutableList())
        searchBookmarksAdapter.setData(filteredBookmarks)
    }
}
