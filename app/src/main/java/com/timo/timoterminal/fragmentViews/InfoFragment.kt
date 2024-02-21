package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import org.koin.android.ext.android.inject

class InfoFragment : Fragment(), TimoRfidListener, FingerprintListener {

    private lateinit var binding: FragmentInfoBinding
    private lateinit var itemBinding: FragmentInfoMessageSheetItemBinding
    private var verifying = true

    private val userRepository: UserRepository by inject()
    private val sharedPrefService: SharedPrefService by inject()
    private val httpService: HttpService by inject()
    private val languageService: LanguageService by inject()
    private val soundSource: SoundSource by inject()

    private var viewModel: InfoFragmentViewModel =
        InfoFragmentViewModel(
            userRepository,
            sharedPrefService,
            httpService,
            languageService,
            soundSource
        )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(inflater, container, false)
        itemBinding = FragmentInfoMessageSheetItemBinding.inflate(inflater, container, false)

        setUpOnClickListeners()
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

    fun register() {
        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
    }

    override fun onPause() {
        unregister()

        super.onPause()
    }

    fun unregister() {
        RfidService.unregister()
        FingerprintService.unregister()
    }

    private fun setUpOnClickListeners() {

        binding.keyboardImage.setSafeOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            showVerificationAlert()
        }

        itemBinding.linearTextContainer.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            viewModel.restartTimer()
        }

        binding.fragmentInfoRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        Log.d("FP", fingerprint)
        // get Key associated to the fingerprint
        FingerprintService.identify(template)?.run {
            soundSource.playSound(SoundSource.successSound)
            Log.d("FP Key", this)
            (activity as MainActivity?)?.showLoadMask()
            viewModel.loadUserInfoById(this.substring(0, this.length - 2), this@InfoFragment)
            return
        }
        soundSource.playSound(SoundSource.authenticationFailed)
    }

    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            soundSource.playSound(SoundSource.successSound)
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            (activity as MainActivity?)?.showLoadMask()
            viewModel.loadUserInfoByCard(oct, this)
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
                viewModel.loadUserInfoByLoginAndPin(login, pin, this)
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

    fun setVerifying(b: Boolean) {
        verifying = b
    }

    fun showCard(card: String) {
        Utils.showMessage(parentFragmentManager, "RFID: $card")
    }
}