package com.timo.timoterminal.modalBottomSheets

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.DialogSingleTextInputBinding
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MBSheetFingerprintCardReaderViewModel
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MBSheetFingerprintCardReader(
    private val callback: () -> Unit?
) : BottomSheetDialogFragment() {
    private val languageService: LanguageService by inject()

    private lateinit var binding: MbSheetFingerprintCardReaderBinding
    private val viewModel: MBSheetFingerprintCardReaderViewModel by sharedViewModel()

    private var status: Int = -1
    private var ranCancel: Boolean = false
    private var success: Boolean = true
    private val timer = object : CountDownTimer(10000, 5000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }

    companion object {
        const val TAG = "MBSheetFingerprintCardReader"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbSheetFingerprintCardReaderBinding.inflate(inflater, container, false)

        success = true
        setValues()
        setUpListeners()

        return binding.root
    }

    private fun setValues() {
        viewModel.viewModelScope.launch {
            //before starting the animation, populate the fields with the correct data! Set the color
            // as well!
            status = arguments?.getInt("status") ?: -1
            viewModel.status = status
            binding.cardImage.contentDescription = languageService.getText("#RFID")
            binding.keyboardImage.contentDescription = languageService.getText("#RFID")
            binding.identificationText.text = languageService.getText("#WaitIdentification")
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        viewModel.status = 0
        ranCancel = true
        RfidService.unregister()
        FingerprintService.unregister()
        timer.cancel()
        callback()
    }

    private fun setUpListeners() {
        viewModel.viewModelScope.launch {
            binding.keyboardImage.setSafeOnClickListener {
                showVerificationAlert()
            }
            binding.buttonClose.setOnClickListener {
                this@MBSheetFingerprintCardReader.dismiss()
            }

            viewModel.liveHideMask.value = false
            viewModel.liveHideMask.observe(viewLifecycleOwner) {
                if (it == true) {
                    this@MBSheetFingerprintCardReader.dialog?.setCanceledOnTouchOutside(true)
                    binding.sheetLayoutLoadMaks.visibility = View.GONE
                    viewModel.liveHideMask.value = false
                }
            }
            viewModel.liveShowMask.value = false
            viewModel.liveShowMask.observe(viewLifecycleOwner) {
                if (it == true) {
                    this@MBSheetFingerprintCardReader.dialog?.setCanceledOnTouchOutside(false)
                    timer.cancel()
                    binding.sheetLayoutLoadMaks.visibility = View.VISIBLE
                    viewModel.liveShowMask.value = false
                }
            }
            viewModel.liveShowMessageSheet.value = Bundle()
            viewModel.liveShowMessageSheet.observe(viewLifecycleOwner) {
                if (it.containsKey("message")) {
                    val sheet = MBBookingResponseSheet()
                    sheet.arguments = it
                    sheet.show(parentFragmentManager, MBBookingResponseSheet.TAG)
                    this@MBSheetFingerprintCardReader.dismiss()
                    viewModel.liveShowMessageSheet.value = Bundle()
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)

        val contentView = View.inflate(context, R.layout.mb_sheet_fingerprint_card_reader, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        return dialog
    }

    override fun onResume() {
        super.onResume()
        binding.buttonClose.visibility = View.VISIBLE

        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(viewModel)
        RfidService.register()
        FingerprintService.setListener(viewModel)
        FingerprintService.register()
        timer.start()
    }

    // remove listener on pause
    override fun onPause() {
        if (!ranCancel) {
            RfidService.unregister()
            FingerprintService.unregister()
            ranCancel = false
        }
        timer.cancel()

        super.onPause()
    }

    private fun showVerificationAlert() {
        val dialogBinding = DialogSingleTextInputBinding.inflate(layoutInflater)
        timer.cancel()

        val dlgAlert: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.MyDialog)
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
            val pin = dialogBinding.dialogTextInputEditValue.text.toString()
            if (pin.isNotEmpty()) {
                viewModel.sendBookingByPIN(pin)
            }
        }
        dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }

        val dialog = dlgAlert.create()
        Utils.hideNavInDialog(dialog)
        val alertTimer = object : CountDownTimer(10000, 500) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                dialog.dismiss()
            }
        }

        dialogBinding.dialogTextInputEditValue.doOnTextChanged { _, _, _, _ ->
            alertTimer.cancel()
            alertTimer.start()
        }
        dialogBinding.dialogTextInputLayoutValue.hint =
            requireContext().getText(R.string.user_passcode)
        dialogBinding.dialogTextViewMessage.text =
            languageService.getText("#EnterCredentials")
        dialogBinding.dialogTextInputEditValue.isFocusable = true
        dialogBinding.dialogTextInputEditValue.isFocusableInTouchMode = true
        dialogBinding.dialogTextInputEditValue.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        dialog.setOnDismissListener {
            Utils.hideNavInDialog(this.dialog)
            alertTimer.cancel()
            timer.start()
        }
        dialog.show()
    }
}
