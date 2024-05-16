package com.timo.timoterminal.fragmentViews

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentSettingsBinding
import com.timo.timoterminal.service.HeartbeatService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.SettingsFragmentViewModel
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val ARG_USERID = "userId"

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val hardwareSource: IHardwareSource by inject()
    private val languageService: LanguageService by inject()
    private val heartbeatService: HeartbeatService by inject()
    private val viewModel: SettingsFragmentViewModel by viewModel()
    private var userId: Long = -1
    private var active = true
    private var first = true

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

    override fun onResume() {
        super.onResume()

        if(!first) {
            (requireActivity() as MainActivity).getViewModel().hideSystemUI()
        }
        first = false
    }

    private fun setText() {
        viewModel.viewModelScope.launch {
            binding.buttonUserSetting.text = languageService.getText("TERMINAL#UserSettings")
            binding.buttonActualizeTerminal.text =
                languageService.getText("#Mark terminal for update")
            binding.buttonResetTerminal.text = languageService.getText("#ResetTerminal", "Reset")
            binding.buttonEthernet.text = languageService.getText("#EthernetSettings")
            binding.buttonMobileNetwork.text = languageService.getText("#MobileNetworkSettings")
            binding.buttonWifi.text = languageService.getText("#WifiSettings")
            binding.buttonLauncher.text = languageService.getText("#Launcher")
            binding.buttonReboot.text = languageService.getText("#RebootTerminal")
            binding.buttonTerminalBooking.text = languageService.getText("TERMINAL#Booking list")
            binding.buttonLogout.text = languageService.getText("#RenewLogin", "Logout")
            binding.buttonBack.text = languageService.getText("hints#zurueck", "Back")
            active = viewModel.getSoundActive()
            binding.buttonSound.text =
                if (active)
                    languageService.getText("#DeactivateSound")
                else
                    languageService.getText("#ActivateSound")
        }
    }

    private fun setupOnClickListeners() {
        viewModel.viewModelScope.launch {
            binding.fragmentSettingRootLayout.setOnClickListener {
                (activity as MainActivity?)?.restartTimer()
            }
            binding.buttonUserSetting.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                parentFragmentManager.commit {
                    addToBackStack(null)
                    replace(
                        R.id.fragment_container_view,
                        UserSettingsFragment.newInstance(userId),
                        UserSettingsFragment.TAG
                    )
                }
                viewModel.loadSound()
            }
            binding.buttonReboot.setSafeOnClickListener {
                requireActivity().sendBroadcast(Intent("com.zkteco.android.action.REBOOT"))
            }
            binding.buttonSound.visibility = if (userId < 0) View.VISIBLE else View.GONE
            binding.buttonSound.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                viewModel.viewModelScope.launch {
                    active = !active
                    viewModel.setSoundActive(active)
                    binding.buttonSound.text =
                        if (active)
                            languageService.getText("#DeactivateSound")
                        else
                            languageService.getText("#ActivateSound")
                    Utils.showMessage(
                        parentFragmentManager,
                        if (active)
                            languageService.getText("#SoundWasActivated")
                        else
                            languageService.getText("#SoundWasDeactivated")
                    )
                }
            }
            binding.buttonLogout.visibility = if (userId < 0) View.VISIBLE else View.GONE
            binding.buttonLogout.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                val dlgAlert: AlertDialog.Builder =
                    AlertDialog.Builder(requireContext(), R.style.MyDialog)
                dlgAlert.setMessage(languageService.getText("#LogoutOfTimOSystem"))
                dlgAlert.setTitle(languageService.getText("#Attention"))
                dlgAlert.setIcon(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.baseline_warning_24
                    )
                )
                dlgAlert.setNegativeButton(
                    languageService.getText(
                        "BUTTON#Gen_Cancel",
                        "Cancel"
                    )
                ) { dia, _ -> dia.dismiss() }
                dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok", "OK")) { _, _ ->
                    viewModel.logout(requireContext())
                    heartbeatService.stopHeartBeat()
                }
                val dialog = dlgAlert.create()
                Utils.hideNavInDialog(dialog)
                dialog.setOnShowListener {
                    val textView = dialog.findViewById<TextView>(android.R.id.message)
                    textView?.textSize = 30f
                    val imageView = dialog.findViewById<ImageView>(android.R.id.icon)
                    val params = imageView?.layoutParams
                    params?.height = 48
                    params?.width = 48
                    imageView?.layoutParams = params
                }
                dialog.show()
                dialog.window?.setLayout(680, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            binding.buttonTerminalBooking.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                parentFragmentManager.commit {
                    addToBackStack(null)
                    replace(R.id.fragment_container_view, BookingListFragment())
                }
            }
            binding.buttonWifi.visibility = if (userId < 0) View.VISIBLE else View.GONE
            binding.buttonWifi.setSafeOnClickListener {
                (requireActivity() as MainActivity).getViewModel().showSystemUI()
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            binding.buttonMobileNetwork.visibility = if (userId < 0) View.VISIBLE else View.GONE
            binding.buttonMobileNetwork.setSafeOnClickListener {
                (requireActivity() as MainActivity).getViewModel().showSystemUI()
                val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
                startActivity(intent)
            }
            binding.buttonEthernet.visibility = if (userId < 0) View.VISIBLE else View.GONE
            binding.buttonEthernet.setSafeOnClickListener {
                (requireActivity() as MainActivity).getViewModel().showSystemUI()
                hardwareSource.openAndroidEthernetSettings()
            }
            binding.buttonActualizeTerminal.setSafeOnClickListener {
                viewModel.actualizeTerminal(requireContext())
                Utils.showMessage(
                    parentFragmentManager,
                    languageService.getText("#TerminalUpdating")
                )
            }
            binding.buttonResetTerminal.visibility = if (userId < 0) View.VISIBLE else View.GONE
            binding.buttonResetTerminal.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                val dlgAlert: AlertDialog.Builder =
                    AlertDialog.Builder(requireContext(), R.style.MyDialog)
                dlgAlert.setMessage(languageService.getText("#DeleteDataResetTerminal"))
                dlgAlert.setTitle(languageService.getText("#Attention"))
                dlgAlert.setIcon(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.baseline_warning_24
                    )
                )
                dlgAlert.setNegativeButton(
                    languageService.getText(
                        "BUTTON#Gen_Cancel",
                        "Cancel"
                    )
                ) { dia, _ -> dia.dismiss() }
                dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok", "OK")) { _, _ ->
                    confirmAgain()
                }
                val dialog = dlgAlert.create()
                Utils.hideNavInDialog(dialog)
                dialog.setOnShowListener {
                    val textView = dialog.findViewById<TextView>(android.R.id.message)
                    textView?.textSize = 30f
                    val imageView = dialog.findViewById<ImageView>(android.R.id.icon)
                    val params = imageView?.layoutParams
                    params?.height = 48
                    params?.width = 48
                    imageView?.layoutParams = params
                }
                dialog.show()
                dialog.window?.setLayout(680, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            binding.buttonBack.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
            //to get to launcher settings you need to enter TimoTimo1 as password
            binding.buttonLauncher.visibility = if (userId == -2L) View.VISIBLE else View.GONE
            binding.buttonLauncher.setSafeOnClickListener {
                (activity as MainActivity?)?.restartTimer()
                val passCodeEditText = EditText(requireContext())
                passCodeEditText.isFocusableInTouchMode = false
                passCodeEditText.isFocusable = false
                passCodeEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_FILTER
                passCodeEditText.privateImeOptions = "nm"

                val dlgAlert: AlertDialog.Builder =
                    AlertDialog.Builder(requireContext(), R.style.MySmallDialog)
                dlgAlert.setMessage(languageService.getText("#PleaseEnterCode"))
                dlgAlert.setTitle(languageService.getText("#Verification"))
                dlgAlert.setIcon(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.baseline_warning_24
                    )
                )
                dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
                dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                    val code = passCodeEditText.text.toString()
                    if (code == "TimoTimo1") {
                        (requireActivity() as MainActivity).getViewModel().showSystemUI()
                        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                        startActivity(intent)
                    }
                }
                val dialog = dlgAlert.create()
                Utils.hideNavInDialog(dialog)
                passCodeEditText.doOnTextChanged { _, _, _, _ ->
                    (activity as MainActivity?)?.restartTimer()
                }
                dialog.setView(passCodeEditText, 20, 0, 20, 0)
                dialog.setOnShowListener {
                    passCodeEditText.isFocusableInTouchMode = true
                    passCodeEditText.isFocusable = true
                    passCodeEditText.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

                    val imageView = dialog.findViewById<ImageView>(android.R.id.icon)
                    val params = imageView?.layoutParams
                    params?.height = 48
                    params?.width = 48
                    imageView?.layoutParams = params
                }
                dialog.show()
                dialog.window?.setLayout(680, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }
    }

    private fun confirmAgain() {
        (activity as MainActivity?)?.restartTimer()
        val dlgAlert: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.MyDialog)
        dlgAlert.setMessage(languageService.getText("#DeleteDataResetTerminal"))
        dlgAlert.setTitle(languageService.getText("#Attention"))
        dlgAlert.setIcon(
            AppCompatResources.getDrawable(
                requireContext(), R.drawable.baseline_warning_24
            )
        )
        dlgAlert.setNegativeButton(
            languageService.getText(
                "BUTTON#Gen_Cancel",
                "Cancel"
            )
        ) { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok", "OK")) { _, _ ->
            viewModel.resetTerminal(requireContext())
        }
        val dialog = dlgAlert.create()
        Utils.hideNavInDialog(dialog)
        dialog.setOnShowListener {
            val textView = dialog.findViewById<TextView>(android.R.id.message)
            textView?.textSize = 30f

            val imageView = dialog.findViewById<ImageView>(android.R.id.icon)
            val params = imageView?.layoutParams
            params?.height = 48
            params?.width = 48
            imageView?.layoutParams = params
        }
        dialog.show()
        dialog.window?.setLayout(680, ViewGroup.LayoutParams.WRAP_CONTENT)
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