package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.databinding.FragmentInfoBinding
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class InfoFragment : Fragment(), RfidListener, FingerprintListener {

    private lateinit var binding: FragmentInfoBinding
    private var verifying = true
    private val userRepository: UserRepository by inject()
    private val sharedPrefService: SharedPrefService by inject()
    private val httpService: HttpService by inject()
    private var viewModel: InfoFragmentViewModel =
        InfoFragmentViewModel(userRepository, sharedPrefService, httpService)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoBinding.inflate(inflater, container, false)

        setUpOnClickListeners()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        if (verifying) {
            RfidService.setListener(this)
            RfidService.register()
            FingerprintService.setListener(this)
            FingerprintService.register()
        }
    }

    override fun onPause() {
        RfidService.unregister()
        FingerprintService.unregister()

        super.onPause()
    }

    private fun setUpOnClickListeners() {
        //for test cases, can be removed later
        binding.cardImage.setOnClickListener {
            viewModel.loadUserInfoByCard("505650110", this)
        }

        binding.keyboardImage.setOnClickListener {
            showVerificationAlert()
        }

        binding.linearTextContainer.setOnClickListener {
            viewModel.restartTimer()
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            Log.d("FP", fingerprint)
//            viewModel.loadUserInformation(user, this@InfoFragment)
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            viewModel.loadUserInfoByCard(oct, this)
        }
    }

    private fun showVerificationAlert() {
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)

        val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        dlgAlert.setMessage("Please enter your credentials")
        dlgAlert.setTitle("INFO")
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton("Cancel") { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton("OK") { _, _ ->
            val code = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (code.isNotEmpty()) {
                viewModel.loadUserInfoByIdAndPin(code, pin, this)
            }
        }

        val dialog = dlgAlert.create()
        dialog.setOnShowListener {
            dialogBinding.textInputEditTextVerificationId.isFocusable = true
            dialogBinding.textInputEditTextVerificationId.isFocusableInTouchMode = true
            dialogBinding.textInputEditTextVerificationId.transformationMethod = null
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        dialog.show()
    }

    fun setVerifying(b: Boolean) {
        verifying = b
    }

    fun getBinding(): FragmentInfoBinding {
        return binding
    }
}