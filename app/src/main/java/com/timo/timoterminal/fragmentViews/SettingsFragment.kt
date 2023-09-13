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
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.FragmentSettingsBinding
import com.zkteco.android.core.sdk.sources.IHardwareSource
import org.koin.android.ext.android.inject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentSettingsBinding
    private val hardwareSource: IHardwareSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        setupOnClickListeners()

        return binding.root
    }

    private fun setupOnClickListeners() {
        binding.buttonUserSetting.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                replace(R.id.fragment_container_view, UserSettingsFragment.newInstance("", ""))
            }
        }
        binding.buttonDevOps.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            startActivity(intent)
        }
        binding.buttonEthernet.setOnClickListener {
            hardwareSource.openAndroidEthernetSettings()
        }
        binding.buttonMobileNetwork.setOnClickListener {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(intent)
        }
        binding.buttonTime.setOnClickListener {
            val intent = Intent(Settings.ACTION_DATE_SETTINGS)
            startActivity(intent)
        }
        binding.buttonWifi.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
        binding.buttonReboot.setOnClickListener {
            requireActivity().sendBroadcast(Intent("com.zkteco.android.action.REBOOT"))
        }
        //to get to launcher settings you need to enter TimoTimo1 as password
        binding.buttonLauncher.setOnClickListener {
            val passCodeEditText = EditText(requireContext())
            passCodeEditText.isFocusableInTouchMode = false
            passCodeEditText.isFocusable = false
            val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
            dlgAlert.setMessage("Please enter code to proceed")
            dlgAlert.setTitle("Verification")
            dlgAlert.setView(passCodeEditText)
            dlgAlert.setNegativeButton("Cancel") { dia, _ -> dia.dismiss() }
            dlgAlert.setPositiveButton("OK") { _, _ ->
                val code = passCodeEditText.text.toString()
                if (code == "TimoTimo1") {
                    val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                    startActivity(intent)
                }
            }
            val dialog = dlgAlert.create()
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
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettignsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}