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
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.DialogSingleTextInputBinding
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MBSheetVerifyUserViewModel
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MBSheetVerifyUser (
    private val callback: (user: UserEntity) -> Unit
) : BottomSheetDialogFragment() {
    private val languageService: LanguageService by inject()

    private lateinit var binding: MbSheetFingerprintCardReaderBinding
    private val viewModel: MBSheetVerifyUserViewModel by sharedViewModel()

    private var ranCancel: Boolean = false

    private val timer = object : CountDownTimer(10000, 5000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }

    companion object {
        const val TAG = "MBSheetVerifyUser"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbSheetFingerprintCardReaderBinding.inflate(inflater, container, false)

        setValues()
        setUpListeners()

        return binding.root
    }

    private fun setValues() {
        binding.cardImage.contentDescription = languageService.getText("#RFID")
        binding.keyboardImage.contentDescription = languageService.getText("#RFID")
        binding.identificationText.text = languageService.getText("#WaitIdentification")
    }

    private fun setUpListeners() {
        viewModel.viewModelScope.launch {
            binding.keyboardImage.setSafeOnClickListener {
                showVerificationAlert()
            }
            binding.buttonClose.setOnClickListener {
                this@MBSheetVerifyUser.dismiss()
            }

            viewModel.liveUser.value = null
            viewModel.liveUser.observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    callback(user)
                    dismiss()
                }
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        ranCancel = true
        RfidService.unregister()
        MainApplication.lcdk.setFingerprintListener(null)
        timer.cancel()
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
        RfidService.setListener(viewModel)
        RfidService.register()
        MainApplication.lcdk.setFingerprintListener(null)
        MainApplication.lcdk.setFingerprintListener(viewModel)
        timer.start()
    }

    // remove listener on pause
    override fun onPause() {
        if (!ranCancel) {
            RfidService.unregister()
            MainApplication.lcdk.setFingerprintListener(null)
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
                viewModel.getUserByPIN(pin)
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