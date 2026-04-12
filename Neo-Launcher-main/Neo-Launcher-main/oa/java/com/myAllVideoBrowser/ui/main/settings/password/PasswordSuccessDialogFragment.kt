package com.myAllVideoBrowser.ui.main.settings.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.DialogPasswordSuccessBinding

class PasswordSuccessDialogFragment : androidx.fragment.app.DialogFragment() {

    private lateinit var binding: DialogPasswordSuccessBinding
    private var openPrivateSpaceOnFinish: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        openPrivateSpaceOnFinish = arguments?.getBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH) == true
        setStyle(STYLE_NO_TITLE, 0)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_password_success, container, false)
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
        binding.messageText.text = getString(R.string.password_success_message)
        binding.confirmButton.text = getString(R.string.password_success_confirm)
        binding.confirmButton.setOnClickListener {
            dismissAllowingStateLoss()
            if (openPrivateSpaceOnFinish) {
                PasswordFlowNavigator.finishToPrivateSpace(requireActivity().supportFragmentManager)
            } else {
                PasswordFlowNavigator.finish(requireActivity().supportFragmentManager)
            }
        }
    }

    companion object {
        private const val ARG_OPEN_PRIVATE_SPACE_ON_FINISH = "arg_open_private_space_on_finish"

        fun newInstance(openPrivateSpaceOnFinish: Boolean = false) =
            PasswordSuccessDialogFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH, openPrivateSpaceOnFinish)
                }
            }
    }
}
