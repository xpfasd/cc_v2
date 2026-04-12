package com.myAllVideoBrowser.ui.main.settings.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.FragmentSecurityQuestionBinding
import com.myAllVideoBrowser.ui.main.base.BaseFragment
import com.myAllVideoBrowser.util.SharedPrefHelper
import javax.inject.Inject

class SecurityQuestionFragment : BaseFragment() {

    @Inject
    lateinit var sharedPrefHelper: SharedPrefHelper

    private lateinit var binding: FragmentSecurityQuestionBinding
    private var pin: String = ""
    private var selectedQuestion: String = ""
    private var openPrivateSpaceOnFinish: Boolean = false
    private var editOnlySecurityQuestion: Boolean = false
    private var verifyForResetPin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pin = arguments?.getString(ARG_PIN).orEmpty()
        openPrivateSpaceOnFinish = arguments?.getBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH) == true
        editOnlySecurityQuestion = arguments?.getBoolean(ARG_EDIT_ONLY_SECURITY_QUESTION) == true
        verifyForResetPin = arguments?.getBoolean(ARG_VERIFY_FOR_RESET_PIN) == true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_security_question, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedQuestion = savedInstanceState?.getString(STATE_QUESTION).orEmpty().ifBlank {
            if (verifyForResetPin) {
                sharedPrefHelper.getSecurityQuestion()
            } else {
                getQuestionOptions().firstOrNull().orEmpty()
            }
        }

        setupHeader()
        setupQuestionPicker()
        setupAnswerField()
        setupDialogResultListener()
        updateSelectedQuestion()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_QUESTION, selectedQuestion)
        super.onSaveInstanceState(outState)
    }

    private fun setupHeader() {
        binding.toolbar.title = ""
        binding.toolbar.setNavigationIcon(R.drawable.homeback)
        binding.toolbar.setNavigationOnClickListener { parentFragmentManager.popBackStack() }
        binding.titleText.text = getString(
            when {
                verifyForResetPin -> R.string.private_space_security_verify_title
                editOnlySecurityQuestion -> R.string.private_space_action_change_security
                else -> R.string.password_security_question_title
            }
        )
        binding.confirmButton.text = getString(
            if (verifyForResetPin) R.string.private_space_security_verify_confirm
            else R.string.password_security_question_confirm
        )
    }

    private fun setupQuestionPicker() {
        if (verifyForResetPin) {
            binding.questionPicker.isClickable = false
            binding.questionPicker.isFocusable = false
        } else {
            binding.questionPicker.setOnClickListener {
                SecurityQuestionDialogFragment.newInstance(selectedQuestion)
                    .show(childFragmentManager, SECURITY_QUESTION_DIALOG_TAG)
            }
        }

        binding.confirmButton.setOnClickListener {
            val answer = binding.answerInput.text?.toString().orEmpty().trim()
            if (answer.isBlank()) {
                binding.answerError.isVisible = true
                binding.answerError.text = getString(R.string.password_answer_required)
                return@setOnClickListener
            }

            binding.answerError.isVisible = false
            if (verifyForResetPin) {
                if (sharedPrefHelper.verifySecurityAnswer(answer)) {
                    PasswordFlowNavigator.startResetPin(
                        requireActivity().supportFragmentManager,
                        true
                    )
                    return@setOnClickListener
                }
                binding.answerError.isVisible = true
                binding.answerError.text = getString(R.string.private_space_security_answer_invalid)
                return@setOnClickListener
            }
            sharedPrefHelper.saveSecurityQuestion(selectedQuestion)
            sharedPrefHelper.saveSecurityAnswer(answer)
            if (editOnlySecurityQuestion) {
                android.widget.Toast.makeText(
                    requireContext(),
                    getString(R.string.private_space_security_updated),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
            } else {
                sharedPrefHelper.savePasswordPin(pin)
                PasswordSuccessDialogFragment.newInstance(openPrivateSpaceOnFinish)
                    .show(requireActivity().supportFragmentManager, PASSWORD_SUCCESS_DIALOG_TAG)
            }
        }
    }

    private fun setupAnswerField() {
        binding.answerInputLayout.hint = getString(
            if (verifyForResetPin) R.string.private_space_security_answer_hint
            else R.string.password_answer_hint
        )
    }

    private fun setupDialogResultListener() {
        childFragmentManager.setFragmentResultListener(
            SECURITY_QUESTION_RESULT_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            selectedQuestion = bundle.getString(SECURITY_QUESTION_RESULT_VALUE).orEmpty()
            updateSelectedQuestion()
        }
    }

    private fun updateSelectedQuestion() {
        binding.questionPicker.text = selectedQuestion
        binding.questionEmptyHint.isVisible = selectedQuestion.isBlank() && !verifyForResetPin
    }

    private fun getQuestionOptions(): List<String> {
        return resources.getStringArray(R.array.security_question_options).toList()
    }

    companion object {
        private const val ARG_PIN = "arg_pin"
        private const val ARG_OPEN_PRIVATE_SPACE_ON_FINISH = "arg_open_private_space_on_finish"
        private const val ARG_EDIT_ONLY_SECURITY_QUESTION = "arg_edit_only_security_question"
        private const val ARG_VERIFY_FOR_RESET_PIN = "arg_verify_for_reset_pin"
        private const val STATE_QUESTION = "state_question"

        fun newInstance(pin: String, openPrivateSpaceOnFinish: Boolean = false) =
            SecurityQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PIN, pin)
                    putBoolean(ARG_OPEN_PRIVATE_SPACE_ON_FINISH, openPrivateSpaceOnFinish)
                }
            }

        fun newEditOnlyInstance() = SecurityQuestionFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_EDIT_ONLY_SECURITY_QUESTION, true)
            }
        }

        fun newVerifyForResetInstance() = SecurityQuestionFragment().apply {
            arguments = Bundle().apply {
                putBoolean(ARG_VERIFY_FOR_RESET_PIN, true)
            }
        }
    }
}
