package com.timo.timoterminal.fragmentViews

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.BuildConfig
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.DialogSingleTextInputBinding
import com.timo.timoterminal.databinding.FragmentInfoBinding
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.modalBottomSheets.MBFragmentInfoSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class InfoFragment : Fragment() {

    private lateinit var binding: FragmentInfoBinding
    private lateinit var itemBinding: FragmentInfoMessageSheetItemBinding
    private var verifying = true

    private val languageService: LanguageService by inject()

    private val viewModel by sharedViewModel<InfoFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        itemBinding = FragmentInfoMessageSheetItemBinding.inflate(inflater, container, false)

        setUpListeners()
        setText()

        return binding.root
    }

    private fun setText() {
        binding.infoIdentificationText.text = languageService.getText("#WaitIdentification")
    }

    override fun onResume() {
        super.onResume()

        if (verifying) {
            register()
        }
    }

    private fun register() {
        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(viewModel)
        RfidService.register()
        FingerprintService.setListener(viewModel)
        FingerprintService.register()
    }

    override fun onPause() {
        unregister()

        super.onPause()
    }

    private fun unregister() {
        RfidService.unregister()
        FingerprintService.unregister()
    }

    private fun setUpListeners() {
        viewModel.viewModelScope.launch {

            binding.keyboardImage.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                showVerificationAlert()
            }

            itemBinding.linearTextContainer.setOnClickListener {
                viewModel.restartTimer()
            }

            if (BuildConfig.DEBUG) {
                //Chill, das ist nur zum allgemeinen Testen der DL APK funktion und verschwindet wieder :)
                binding.installNewAppButton.setOnClickListener {
                    val launchIntent: Intent? =
                        requireActivity().packageManager.getLaunchIntentForPackage("com.timo.timoupdate")
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launchIntent) //null pointer check in case package name was not found
                    }
                }
            }


            binding.fragmentInfoRootLayout.setOnClickListener {
                (activity as MainActivity?)?.restartTimer()
            }

            viewModel.liveRfidNumber.value = ""
            viewModel.liveRfidNumber.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    (activity as MainActivity?)?.hideLoadMask()
                    showCard(it)
                    viewModel.liveRfidNumber.value = ""
                }
            }
            viewModel.liveHideMask.value = false
            viewModel.liveHideMask.observe(viewLifecycleOwner) {
                if (it == true) {
                    (activity as MainActivity?)?.hideLoadMask()
                    viewModel.liveHideMask.value = false
                }
            }
            viewModel.liveShowMask.value = false
            viewModel.liveShowMask.observe(viewLifecycleOwner) {
                if (it == true) {
                    (activity as MainActivity?)?.showLoadMask()
                    viewModel.liveShowMask.value = false
                }
            }
            viewModel.liveRestartTimer.value = false
            viewModel.liveRestartTimer.observe(viewLifecycleOwner) {
                if (it == true) {
                    (activity as MainActivity?)?.restartTimer()
                    viewModel.liveRestartTimer.value = false
                }
            }
            viewModel.liveDismissInfoSheet.value = false
            viewModel.liveDismissInfoSheet.observe(viewLifecycleOwner) {
                if (it == true) {
                    (activity as MainActivity?)?.restartTimer()
                    register()
                    verifying = true
                    viewModel.liveDismissInfoSheet.value = false
                }
            }
            viewModel.liveMessage.value = ""
            viewModel.liveMessage.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    Utils.showMessage(
                        parentFragmentManager,
                        it
                    )
                    viewModel.liveMessage.value = ""
                }
            }
            viewModel.liveUser.value = null
            viewModel.liveUser.observe(viewLifecycleOwner) {
                if (it != null) {
                    unregister()
                    verifying = false
                    viewModel.loadUserInformation(it, null)
                    viewModel.liveUser.value = null
                }
            }
            viewModel.liveShowInfoSheet.value = false
            viewModel.liveShowInfoSheet.observe(viewLifecycleOwner) {
                if (it == true) {
                    val sheet = MBFragmentInfoSheet()
                    sheet.show(
                        parentFragmentManager,
                        MBFragmentInfoSheet.TAG
                    )
                    viewModel.liveShowInfoSheet.value = false
                }
            }
            viewModel.liveErrorMessage.value = ""
            viewModel.liveErrorMessage.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    Utils.showErrorMessage(requireContext(), it)
                    viewModel.liveErrorMessage.value = ""
                }
            }
        }
    }

    private fun showVerificationAlert() {
        val dialogBinding = DialogSingleTextInputBinding.inflate(layoutInflater)
        unregister()

        val dlgAlert: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.MyDialog)
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
            (activity as MainActivity?)?.restartTimer()
            val pin = dialogBinding.dialogTextInputEditValue.text.toString()
            if (pin.isNotEmpty()) {
                (activity as MainActivity?)?.showLoadMask()
                viewModel.loadUserInfoByPin(pin)
            }
        }

        val dialog = dlgAlert.create()
        Utils.hideNavInDialog(dialog)
        val alertTimer = object : CountDownTimer(10000, 5000) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                dialog.dismiss()
            }
        }

        dialogBinding.dialogTextViewMessage.text =
            languageService.getText("#EnterCredentials")
        dialogBinding.dialogTextInputEditValue.doOnTextChanged { _, _, _, _ ->
            alertTimer.cancel()
            alertTimer.start()
            (activity as MainActivity?)?.restartTimer()
        }
        dialogBinding.dialogTextInputLayoutValue.hint =
            requireContext().getText(R.string.user_passcode)
        dialogBinding.dialogTextInputEditValue.isFocusable = true
        dialogBinding.dialogTextInputEditValue.isFocusableInTouchMode = true
        dialogBinding.dialogTextInputEditValue.inputType =
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        dialog.setOnDismissListener {
            alertTimer.cancel()
            register()
        }
        dialog.show()
    }

    private fun showCard(card: String) {
        Utils.showMessage(parentFragmentManager, "RFID: $card")
    }
}