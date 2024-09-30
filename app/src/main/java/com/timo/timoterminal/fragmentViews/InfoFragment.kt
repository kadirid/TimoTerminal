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
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.DialogSingleTextInputBinding
import com.timo.timoterminal.databinding.FragmentInfoBinding
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.modalBottomSheets.MBFragmentInfoSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
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
        binding.infoVersionText.text = viewModel.getVersionName()
    }

    override fun onResume() {
        super.onResume()

        if (verifying) {
            register()
        }
    }

    private fun register() {
        RfidService.unregister()
        RfidService.setListener(viewModel)
        RfidService.register()
        MainApplication.lcdk.setFingerprintListener(null)
        MainApplication.lcdk.setFingerprintListener(viewModel)
    }

    override fun onPause() {
        unregister()

        super.onPause()
    }

    private fun unregister() {
        RfidService.unregister()
        MainApplication.lcdk.setFingerprintListener(null)
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
                        launchIntent.putExtra(
                            "src",
                            "http://192.168.0.45/timo_prd/services/rest/zktecoTerminal/downloadAPK/?firma=prdjbtestzkteco&token=teHJdiMxvwV3steWsyXfaXp99U1721644328176&terminalSN=CKWN214760894&terminalId=6"
                        )
                        launchIntent.putExtra("version", "1.2.00000000000000000000000000000000000000000000001")
                        startActivity(launchIntent) //null pointer check in case package name was not found
                    }
                }
            }
            binding.installNewAppButton.visibility = View.GONE

            binding.fragmentInfoRootLayout.setOnClickListener {
                (activity as MainActivity?)?.restartTimer()
            }

            binding.buttonBack.setOnClickListener {
                parentFragmentManager.popBackStack()
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
            viewModel.liveUser.value = Pair(false, null)
            viewModel.liveUser.observe(viewLifecycleOwner) {
                if (it.first) {
                    if (it.second != null) {
                        unregister()
                        verifying = false
                        viewModel.loadUserInformation(it.second as UserEntity, null)
                        viewModel.liveUser.value = Pair(false, null)
                    } else {
                        Utils.showErrorMessage(
                            requireContext(),
                            languageService.getText("#NoUserFound")
                        )
                        viewModel.liveHideMask.postValue(true)
                    }
                }
            }
            viewModel.liveInfoSuccess.value = Bundle()
            viewModel.liveInfoSuccess.observe(viewLifecycleOwner) {
                if (!it.getString("card").isNullOrBlank()) {
                    val sheet = MBFragmentInfoSheet()
                    sheet.arguments = it
                    sheet.show(
                        parentFragmentManager,
                        MBFragmentInfoSheet.TAG
                    )
                    viewModel.liveInfoSuccess.value = Bundle()
                }
            }
            viewModel.liveErrorMessage.value = ""
            viewModel.liveErrorMessage.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    Utils.showErrorMessage(requireContext(), it)
                    viewModel.liveErrorMessage.value = ""
                }
            }
            viewModel.liveShowMessageSheet.value = ""
            viewModel.liveShowMessageSheet.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    Utils.showMessage(parentFragmentManager, it)
                    viewModel.liveShowMessageSheet.value = ""
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