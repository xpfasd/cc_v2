package com.myAllVideoBrowser.ui.main.settings.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogSecurityQuestionBinding
class SecurityQuestionDialogFragment : androidx.fragment.app.DialogFragment() {

    private lateinit var binding: DialogSecurityQuestionBinding
    private var selectedQuestion: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedQuestion = arguments?.getString(ARG_SELECTED).orEmpty()
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_security_question, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        binding.cardContainer.setBackgroundResource(R.drawable.password_dialog_card_background)

        val options = resources.getStringArray(R.array.security_question_options)
        binding.optionsContainer.removeAllViews()
        options.forEach { option ->
            val item = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_security_question_option, binding.optionsContainer, false)
            val text = item.findViewById<com.google.android.material.textview.MaterialTextView>(R.id.optionText)
            val checkIcon = item.findViewById<android.widget.ImageView>(R.id.checkIcon)
            text.text = option
            item.isSelected = option == selectedQuestion
            text.isSelected = option == selectedQuestion
            checkIcon.visibility = if (option == selectedQuestion) View.VISIBLE else View.GONE
            item.setBackgroundResource(R.drawable.security_question_row_background)
            item.setOnClickListener {
                parentFragmentManager.setFragmentResult(
                    SECURITY_QUESTION_RESULT_KEY,
                    bundleOf(SECURITY_QUESTION_RESULT_VALUE to option)
                )
                dismissAllowingStateLoss()
            }
            binding.optionsContainer.addView(item)
        }
    }

    companion object {
        private const val ARG_SELECTED = "arg_selected"

        fun newInstance(selectedQuestion: String) = SecurityQuestionDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_SELECTED, selectedQuestion) }
        }
    }
}
