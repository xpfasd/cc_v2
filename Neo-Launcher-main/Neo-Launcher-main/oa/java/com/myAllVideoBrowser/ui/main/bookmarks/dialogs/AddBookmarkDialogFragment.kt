package com.myAllVideoBrowser.ui.main.bookmarks.dialogs

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogAddBookmarkBinding

class AddBookmarkDialogFragment : BaseBookmarkDialogFragment() {
    private lateinit var binding: DialogAddBookmarkBinding
    private var initialTitle: String? = null
    private var initialUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialTitle = arguments?.getString(ARG_TITLE)
        initialUrl = arguments?.getString(ARG_URL)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_add_bookmark, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icon.setImageResource(R.drawable.bookmark_add_24px)
        binding.title.text = getString(R.string.bookmarks_dialog_add_title)
        binding.message.text = getString(R.string.bookmarks_dialog_add_message)
        binding.titleInputLayout.hint = getString(R.string.bookmarks_dialog_name_hint)
        binding.urlInputLayout.hint = getString(R.string.bookmarks_dialog_url_hint)
        binding.primaryButton.text = getString(R.string.bookmarks_dialog_save)
        binding.secondaryButton.text = getString(R.string.bookmarks_dialog_cancel)

        binding.titleInput.setText(initialTitle.orEmpty())
        binding.urlInput.setText(initialUrl.orEmpty())
        updateSaveButtonState()

        binding.titleInput.addTextChangedListener { updateSaveButtonState() }
        binding.urlInput.addTextChangedListener { updateSaveButtonState() }

        binding.primaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                BookmarksDialogResults.RESULT_KEY,
                bundleOf(
                    BookmarksDialogResults.RESULT_ACTION to BookmarksDialogResults.ACTION_SAVE,
                    BookmarksDialogResults.RESULT_TITLE to binding.titleInput.text?.toString().orEmpty().trim(),
                    BookmarksDialogResults.RESULT_URL to binding.urlInput.text?.toString().orEmpty().trim()
                )
            )
            dismissAllowingStateLoss()
        }

        binding.secondaryButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                BookmarksDialogResults.RESULT_KEY,
                bundleOf(BookmarksDialogResults.RESULT_ACTION to BookmarksDialogResults.ACTION_CANCEL)
            )
            dismissAllowingStateLoss()
        }

        dialog?.setOnShowListener {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.titleInput.requestFocus()
            }, 150)
        }
    }

    private fun updateSaveButtonState() {
        val title = binding.titleInput.text?.toString().orEmpty().trim()
        val url = binding.urlInput.text?.toString().orEmpty().trim()
        binding.primaryButton.isEnabled = title.isNotBlank() && url.isNotBlank()
    }

    companion object {
        const val TAG = "add_bookmark_dialog"
        private const val ARG_TITLE = "arg_title"
        private const val ARG_URL = "arg_url"

        fun newInstance(title: String? = null, url: String? = null) = AddBookmarkDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_URL, url)
            }
        }
    }
}

