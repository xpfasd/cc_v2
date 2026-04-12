package com.myAllVideoBrowser.ui.main.settings.password

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import com.myAllVideoBrowser.R
import com.myAllVideoBrowser.databinding.FragmentPasswordPinBinding
import com.myAllVideoBrowser.ui.main.base.BaseFragment

abstract class PasswordPinFragmentBase : BaseFragment() {

    protected lateinit var binding: FragmentPasswordPinBinding

    private var enteredPin = ""

    protected abstract val screenTitle: String
    protected abstract val screenPrompt: String
    protected open val screenSubtitle: String = ""
    protected open val secondaryActionText: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_password_pin, container, false)
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        enteredPin = savedInstanceState?.getString(STATE_PIN).orEmpty()
        setupHeader()
        setupKeypad()
        updatePinIndicators()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_PIN, enteredPin)
        super.onSaveInstanceState(outState)
    }

    protected open fun onBackPressed() {
        if (parentFragmentManager.backStackEntryCount == 0) {
            requireActivity().finish()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    protected fun appendDigit(digit: String) {
        if (enteredPin.length >= PIN_LENGTH) {
            return
        }
        enteredPin += digit
        updatePinIndicators()
        onPinChanged()
        if (enteredPin.length == PIN_LENGTH) {
            onPinCompleted(enteredPin)
        }
    }

    protected fun clearPin() {
        enteredPin = ""
        updatePinIndicators()
        onPinChanged()
    }

    protected abstract fun onPinCompleted(pin: String)

    protected open fun onPinChanged() {}

    protected open fun onSecondaryActionClicked() {}

    private fun setupHeader() {
        binding.toolbar.title = ""
        binding.toolbar.setNavigationIcon(R.drawable.homeback)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        binding.titleText.text = screenTitle
        binding.promptText.text = screenPrompt
        binding.subtitleText.visibility = if (screenSubtitle.isBlank()) View.GONE else View.VISIBLE
        binding.subtitleText.text = screenSubtitle
        binding.secondaryActionText.visibility =
            if (secondaryActionText.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.secondaryActionText.text = secondaryActionText.orEmpty()
        binding.secondaryActionText.setOnClickListener {
            if (!secondaryActionText.isNullOrBlank()) {
                onSecondaryActionClicked()
            }
        }

        listOf(
            binding.key1 to "1",
            binding.key2 to "2",
            binding.key3 to "3",
            binding.key4 to "4",
            binding.key5 to "5",
            binding.key6 to "6",
            binding.key7 to "7",
            binding.key8 to "8",
            binding.key9 to "9",
            binding.key0 to "0"
        ).forEach { (view, digit) ->
            view.setOnClickListener { appendDigit(digit) }
        }
        binding.keyBackspace.setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin = enteredPin.dropLast(1)
                updatePinIndicators()
                onPinChanged()
            }
        }
    }

    private fun setupKeypad() {
//        binding.keyBackspace.text = ""
//        binding.keyBackspace.setCompoundDrawablesRelativeWithIntrinsicBounds(
//            0,
//            0,
//            R.drawable.password_backspace_24px,
//            0
//        )
    }

    private fun updatePinIndicators() {
        listOf(binding.pin1, binding.pin2, binding.pin3, binding.pin4).forEachIndexed { index, view ->
            view.setBackgroundResource(
                if (index < enteredPin.length) {
                    R.drawable.password_pin_dot_filled
                } else {
                    R.drawable.password_pin_dot_empty
                }
            )
        }
    }

    companion object {
        private const val PIN_LENGTH = 4
        private const val STATE_PIN = "state_pin"
    }
}
