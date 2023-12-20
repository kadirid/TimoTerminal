package com.timo.timoterminal.fragmentViews

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentSettingsBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.viewModel.SettingsFragmentViewModel
import com.zkteco.android.core.sdk.sources.IHardwareSource
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ARG_USERID = "userId"

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val hardwareSource: IHardwareSource by inject()
    private val languageService: LanguageService by inject()
    private val viewModel: SettingsFragmentViewModel by viewModel()
    private var userId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupOnClickListeners()
        setText()

        return binding.root
    }

    private fun setText() {
        binding.buttonUserSetting.text = languageService.getText("ALLGEMEIN#UserSettings")
        binding.buttonDevOps.text = languageService.getText("#DevOps")
        binding.buttonEthernet.text = languageService.getText("#EthernetSettings")
        binding.buttonMobileNetwork.text = languageService.getText("#MobileNetworkSettings")
        binding.buttonWifi.text = languageService.getText("#WifiSettings")
        binding.buttonLauncher.text = languageService.getText("#Launcher")
        binding.buttonReboot.text = languageService.getText("#RebootTerminal")
        binding.buttonTerminalBooking.text = languageService.getText("#Booking list")
    }

    private fun setupOnClickListeners() {
        binding.fragmentSettingRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.buttonUserSetting.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, UserSettingsFragment.newInstance(userId))
            }
        }
        binding.buttonReboot.setOnClickListener {
            requireActivity().sendBroadcast(Intent("com.zkteco.android.action.REBOOT"))
        }
        binding.buttonLogout.setOnClickListener {
            viewModel.logout(requireContext())
        }
        binding.buttonTerminalBooking.visibility = if(userId<0) View.VISIBLE else View.GONE
        binding.buttonTerminalBooking.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            parentFragmentManager.commit {
                replace(R.id.fragment_container_view, BookingListFragment())
            }
        }
        binding.buttonWifi.visibility = if(userId<0) View.VISIBLE else View.GONE
        binding.buttonWifi.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
        binding.buttonMobileNetwork.visibility = if(userId<0) View.VISIBLE else View.GONE
        binding.buttonMobileNetwork.setOnClickListener {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(intent)
        }
        binding.buttonEthernet.visibility = if(userId<0) View.VISIBLE else View.GONE
        binding.buttonEthernet.setOnClickListener {
            hardwareSource.openAndroidEthernetSettings()
        }
        binding.buttonDevOps.visibility = if(userId==-2L) View.VISIBLE else View.GONE
        binding.buttonDevOps.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            startActivity(intent)
        }
        //to get to launcher settings you need to enter TimoTimo1 as password
        binding.buttonLauncher.visibility = if(userId==-2L) View.VISIBLE else View.GONE
        binding.buttonLauncher.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            val passCodeEditText = EditText(requireContext())
            passCodeEditText.isFocusableInTouchMode = false
            passCodeEditText.isFocusable = false
            val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            dlgAlert.setMessage(languageService.getText("#PleaseEnterCode"))
            dlgAlert.setTitle(languageService.getText("#Verification"))
            dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
            dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                val code = passCodeEditText.text.toString()
                if (code == "TimoTimo1") {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    startActivity(intent)
                }
            }
            val dialog = dlgAlert.create()
            passCodeEditText.doOnTextChanged {  _, _, _, _ ->
                (activity as MainActivity?)?.restartTimer()
            }
            dialog.setView(passCodeEditText, 20, 0, 20, 0)
            dialog.setOnShowListener {
                passCodeEditText.isFocusableInTouchMode = true
                passCodeEditText.isFocusable = true
                passCodeEditText.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            dialog.show()
        }
    }

    companion object {
        fun newInstance(userId: Long) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                }
            }
    }
}