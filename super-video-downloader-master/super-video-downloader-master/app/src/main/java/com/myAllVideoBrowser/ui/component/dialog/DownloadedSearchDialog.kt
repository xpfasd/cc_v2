package com.myAllVideoBrowser.ui.component.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.util.AppUtil

fun showDownloadedSearchDialog(
    context: Context,
    appUtil: AppUtil,
    initialQuery: String,
    onSearchConfirmed: (String) -> Unit
) {
    val content = LayoutInflater.from(context)
        .inflate(R.layout.dialog_downloaded_search, null, false)

    val etSearch = content.findViewById<TextInputEditText>(R.id.et_search)
    val ivClose = content.findViewById<ImageView>(R.id.iv_close)
    val ivClear = content.findViewById<ImageView>(R.id.iv_clear)
    val btnCancel = content.findViewById<MaterialButton>(R.id.btn_cancel)
    val btnSearch = content.findViewById<MaterialButton>(R.id.btn_search)

    fun currentQuery(): String = etSearch.text?.toString().orEmpty()

    fun updateClearVisibility() {
        ivClear.isVisible = currentQuery().isNotEmpty()
    }

    val dialog = AlertDialog.Builder(context)
        .setView(content)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    etSearch.setText(initialQuery)
    etSearch.text?.let { etSearch.setSelection(it.length) }
    etSearch.imeOptions = EditorInfo.IME_ACTION_SEARCH
    updateClearVisibility()

    etSearch.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateClearVisibility()
        }

        override fun afterTextChanged(s: android.text.Editable?) = Unit
    })

    fun dismissDialog() {
        appUtil.hideSoftKeyboard(etSearch)
        dialog.dismiss()
    }

    fun submitSearch() {
        appUtil.hideSoftKeyboard(etSearch)
        onSearchConfirmed(currentQuery())
        dialog.dismiss()
    }

    ivClose.setOnClickListener { dismissDialog() }
    btnCancel.setOnClickListener { dismissDialog() }
    ivClear.setOnClickListener {
        etSearch.text?.clear()
        etSearch.requestFocus()
    }
    btnSearch.setOnClickListener { submitSearch() }
    etSearch.setOnEditorActionListener { _, actionId, event ->
        val isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
        val isEnterKey =
            event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
        if (isSearchAction || isEnterKey) {
            submitSearch()
            true
        } else {
            false
        }
    }

    dialog.setOnShowListener {
        Handler(Looper.getMainLooper()).postDelayed({
            etSearch.requestFocus()
            appUtil.showSoftKeyboard(etSearch)
        }, 250)
    }

    dialog.show()
}
