package com.myAllVideoBrowser.ui.component.dialog

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.util.AppUtil

fun showRenameVideoDialog(
    context: Context,
    appUtil: AppUtil,
    currentName: String,
    onClickListener: View.OnClickListener
) {
    val content = LayoutInflater.from(context)
        .inflate(R.layout.dialog_downloaded_rename_video, null, false)

    val etName = content.findViewById<TextInputEditText>(R.id.et_name)
    val ivClose = content.findViewById<ImageView>(R.id.iv_close)
    val btnSave = content.findViewById<MaterialButton>(R.id.btn_save)

    etName.setText(currentName)
    etName.text?.let { etName.setSelection(it.length) }
    etName.imeOptions = EditorInfo.IME_ACTION_DONE

    val dialog = AlertDialog.Builder(context)
        .setView(content)
        .create()

    dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

    ivClose.setOnClickListener {
        appUtil.hideSoftKeyboard(etName)
        dialog.dismiss()
    }

    btnSave.setOnClickListener {
        appUtil.hideSoftKeyboard(etName)
        onClickListener.onClick(etName)
        dialog.dismiss()
    }

    dialog.setOnShowListener {
        Handler(Looper.getMainLooper()).postDelayed({
            appUtil.showSoftKeyboard(etName)
        }, 250)
    }

    dialog.show()
}
