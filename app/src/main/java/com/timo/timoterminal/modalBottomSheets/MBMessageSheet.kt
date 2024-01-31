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
import com.timo.timoterminal.databinding.MbMessageSheetBinding
import com.timo.timoterminal.utils.Utils

class MBMessageSheet : BottomSheetDialogFragment() {
    private lateinit var binding: MbMessageSheetBinding

    private var message: String = ""
    private val timer = object : CountDownTimer(5000, 500) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }

    companion object {
        const val TAG = "MBMessageSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbMessageSheetBinding.inflate(inflater, container, false)

        message = arguments?.getString("message") ?: ""
        binding.messageTextViewBookingMessage.text = message
        timer.start()

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)
        val contentView = View.inflate(context, R.layout.mb_message_sheet, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        return dialog
    }

    fun getBinding(): MbMessageSheetBinding {
        return binding
    }
}