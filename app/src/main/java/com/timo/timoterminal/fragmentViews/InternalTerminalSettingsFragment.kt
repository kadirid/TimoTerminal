package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentInternalTerminalSettingsBinding
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.InternalTerminalSettingsFragmentViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class InternalTerminalSettingsFragment : Fragment() {
    private val languageService: LanguageService by inject()
    private val sharedPrefService: SharedPrefService by inject()
    private val viewModel: InternalTerminalSettingsFragmentViewModel by viewModel()

    private var timerLength = sharedPrefService.getLong(SharedPreferenceKeys.BOOKING_MESSAGE_TIMEOUT_IN_SEC, 5)

    private lateinit var binding: FragmentInternalTerminalSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentInternalTerminalSettingsBinding.inflate(inflater, container, false)

        setUp()

        return binding.root
    }

    private fun setUp() {
        binding.terminalSettingTimeoutInputEditText.setText(timerLength.toString())

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.buttonSubmit.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            viewModel.setTimeOut(binding.terminalSettingTimeoutInputEditText.text.toString())
            Utils.showMessage(parentFragmentManager, languageService.getText("#Data was saved successfully"))
        }
    }

    companion object {
        const val TAG = "InternalTerminalSettingsFragment"
    }

}