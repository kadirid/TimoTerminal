package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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

    private var timerLength =
        sharedPrefService.getLong(SharedPreferenceKeys.BOOKING_MESSAGE_TIMEOUT_IN_SEC, 5)

    private lateinit var binding: FragmentInternalTerminalSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInternalTerminalSettingsBinding.inflate(inflater, container, false)

        setUp()

        return binding.root
    }

    private fun setUp() {
        binding.terminalSettingTimeoutInputEditText.setText(timerLength.toString())
        binding.terminalSettingTimeoutInputEditText.filters = arrayOf(object : InputFilter {
            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                try {
                    val newVal = "${dest.subSequence(0, dstart)}${
                        source.subSequence(
                            start,
                            end
                        )
                    }${dest.subSequence(dend, dest.length)}"
                    val input = Integer.parseInt(newVal)
                    if (input in 1..60) {
                        return null
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace()
                }
                return ""
            }
        })

        binding.buttonBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.buttonSubmit.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
            val text = binding.terminalSettingTimeoutInputEditText.text.toString()
            if (text.isNotEmpty()) {
                val number = text.toLong()
                viewModel.setTimeOut(number)
                Utils.showMessage(
                    parentFragmentManager,
                    languageService.getText("#Data was saved successfully")
                )
            } else {
                Utils.showErrorMessage(
                    requireContext(),
                    languageService.getText("ALLGEMEIN#LeererWertIstNichtErlaubt"),
                    false
                )
            }
        }
    }

    companion object {
        const val TAG = "InternalTerminalSettingsFragment"
    }

}