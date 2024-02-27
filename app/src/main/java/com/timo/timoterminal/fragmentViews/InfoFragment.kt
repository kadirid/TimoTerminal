package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.databinding.FragmentInfoBinding
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.modalBottomSheets.MBFragmentInfoSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
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
        binding.cardImage.contentDescription = languageService.getText("#RFID")
        binding.keyboardImage.contentDescription = languageService.getText("#RFID")
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

        binding.keyboardImage.setSafeOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            showVerificationAlert()
        }

        itemBinding.linearTextContainer.setOnClickListener {
            viewModel.restartTimer()
        }

        binding.fragmentInfoRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }

        viewModel.liveRfidNumber.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                (activity as MainActivity?)?.hideLoadMask()
                showCard(it)
                viewModel.liveRfidNumber.value = ""
            }
        }
        viewModel.liveHideMask.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.hideLoadMask()
                viewModel.liveHideMask.value = false
            }
        }
        viewModel.liveShowMask.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.showLoadMask()
                viewModel.liveShowMask.value = false
            }
        }
        viewModel.liveRestartTimer.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.restartTimer()
                viewModel.liveRestartTimer.value = false
            }
        }
        viewModel.liveDismissInfoSheet.observe(viewLifecycleOwner) {
            if (it == true) {
                (activity as MainActivity?)?.restartTimer()
                register()
                verifying = true
                viewModel.liveDismissInfoSheet.value = false
            }
        }
        viewModel.liveMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                Utils.showMessage(
                    parentFragmentManager,
                    it
                )
                viewModel.liveMessage.value = ""
            }
        }
        viewModel.liveUser.observe(viewLifecycleOwner) {
            if (it != null) {
                unregister()
                verifying = false
                viewModel.loadUserInformation(it)
                viewModel.liveUser.value = null
            }
        }
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
        viewModel.liveErrorMessage.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                Utils.showErrorMessage(requireContext(), it)
                viewModel.liveErrorMessage.value = ""
            }
        }
    }

    private fun showVerificationAlert() {
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)

        val dlgAlert: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.MySmallDialog)
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
            (activity as MainActivity?)?.restartTimer()
            val login = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (login.isNotEmpty() && pin.isNotEmpty()) {
                (activity as MainActivity?)?.showLoadMask()
                viewModel.loadUserInfoByLoginAndPin(login, pin)
            }
        }

        val dialog = dlgAlert.create()
        Utils.hideNavInDialog(dialog)
        val alertTimer = object : CountDownTimer(10000, 500) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                dialog.dismiss()
            }
        }

        dialogBinding.textViewDialogVerificationMessage.text =
            languageService.getText("#EnterCredentials")
        dialogBinding.textInputEditTextVerificationId.doOnTextChanged { _, _, _, _ ->
            alertTimer.cancel()
            alertTimer.start()
            (activity as MainActivity?)?.restartTimer()
        }
        dialogBinding.textInputEditTextVerificationPin.doOnTextChanged { _, _, _, _ ->
            alertTimer.cancel()
            alertTimer.start()
            (activity as MainActivity?)?.restartTimer()
        }
        dialog.setOnShowListener {
            dialogBinding.textInputEditTextVerificationId.isFocusable = true
            dialogBinding.textInputEditTextVerificationId.isFocusableInTouchMode = true
            dialogBinding.textInputEditTextVerificationId.transformationMethod = null
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        dialog.setOnDismissListener {
            alertTimer.cancel()
        }
        dialog.show()
    }

    private fun showCard(card: String) {
        Utils.showMessage(parentFragmentManager, "RFID: $card")
    }
}