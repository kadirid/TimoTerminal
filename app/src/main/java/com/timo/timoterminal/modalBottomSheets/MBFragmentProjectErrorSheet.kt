package com.timo.timoterminal.modalBottomSheets

import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentProjectErrorMessageSheetBinding
import com.timo.timoterminal.utils.Utils

class MBFragmentProjectErrorSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentProjectErrorMessageSheetBinding
    private var message: String = ""

    private var isTimerRunning = true
    private val timer = object : CountDownTimer(28999, 950) {
        override fun onTick(millisUntilFinished: Long) {
            isTimerRunning = true
            binding.textViewSecondClose.text = (millisUntilFinished / 950).toString()
        }

        override fun onFinish() {
            this@MBFragmentProjectErrorSheet.dismiss()
        }
    }

    companion object {
        const val TAG = "MBFragmentProjectErrorSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProjectErrorMessageSheetBinding.inflate(inflater, container, false)

        message = arguments?.getString("message") ?: "An unknown error occurred."

        binding.buttonBack.setOnClickListener {
            this@MBFragmentProjectErrorSheet.dismiss()
        }
        binding.textViewErrorMessage.text = message
        timer.start()

        return binding.root
    }

    override fun onResume() {
        binding.projectErrorMessageSheet.setOnClickListener {
            timer.cancel()
            timer.start()
            (activity as MainActivity?)?.restartTimer()
        }

        super.onResume()
    }

    override fun onPause() {
        super.onPause()

        if (isTimerRunning) {
            timer.cancel()
            isTimerRunning = false
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)
        val contentView = View.inflate(context, R.layout.fragment_project_error_message_sheet, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        behavior.maxWidth = 900
        return dialog
    }
}