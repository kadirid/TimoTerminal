package com.timo.timoterminal.fragmentViews

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.timo.timoterminal.BuildConfig
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.activities.NoInternetNetworkSettingsActivity
import com.timo.timoterminal.databinding.FragmentLoginBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.utils.CodesArrayAdapter
import com.timo.timoterminal.viewModel.LoginFragmentViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.TimeZone


class LoginFragment : Fragment() {
    private val propertyService: PropertyService by inject()
    private val loginService: LoginService by inject()
    private val settingsService: SettingsService by inject()
    private val languageService: LanguageService by inject()
    private val viewModel: LoginFragmentViewModel =
        LoginFragmentViewModel(loginService, settingsService, languageService)
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        initButtonListener()
        initLanguageDropdown()
        initTimezoneDropdown()
        initDebugFields()
        return binding.root
    }

    private fun initDebugFields() {
        if (BuildConfig.DEBUG) {
            val url = propertyService.getProperties().getProperty("customUrl")
            val company = propertyService.getProperties().getProperty("company")
            val username = propertyService.getProperties().getProperty("username")
            val password = propertyService.getProperties().getProperty("password")
            binding.customUrl.setText(url)
            binding.textInputEditTextLoginCompany.setText(if (!company.isNullOrEmpty()) company else "")
            binding.textInputEditTextLoginUser.setText(if (!username.isNullOrEmpty()) username else "")
            binding.textInputEditTextLoginPassword.setText(if (!password.isNullOrEmpty()) password else "")
        }
    }

    private fun openMainView(isNewTerminal: Boolean?) {
        val goToMainActivity = Intent(context, MainActivity::class.java)
        goToMainActivity.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        goToMainActivity.putExtra("isNewTerminal", isNewTerminal)
        startActivity(goToMainActivity)
    }

    private fun initLanguageDropdown() {
        val languages = viewModel.languages
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages)
        binding.dropdownMenuLanguage.setAdapter(adapter)
    }

    private fun initTimezoneDropdown() {
        val ids = TimeZone.getAvailableIDs().toMutableList()
        val adapter =
            CodesArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, ids)
        binding.dropdownMenuTimezone.setAdapter(adapter)
    }

    private fun showLangAndTimezone() {
        // INIT ANIMATION
        val animSet = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_move_up)
        val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.textInputLayoutLoginCompany.visibility = View.GONE
                binding.textInputLayoutLoginPassword.visibility = View.GONE
                binding.textInputLayoutLoginUser.visibility = View.GONE

                binding.dropdownMenuLayoutLanguage.startAnimation(fadeInAnimation)
                binding.dropdownMenuLayoutLanguage.visibility = View.VISIBLE

                binding.dropdownMenuLayoutTimezone.startAnimation(fadeInAnimation)
                binding.dropdownMenuLayoutTimezone.visibility = View.VISIBLE

                binding.buttonSubmit.setText(R.string.save_settings)
                initSaveSettingsButton()
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        activity?.runOnUiThread {
            binding.textInputLayoutLoginCompany.startAnimation(animSet)
            binding.textInputLayoutLoginPassword.startAnimation(animSet)
            binding.textInputLayoutLoginUser.startAnimation(animSet)
        }
    }

    private fun initButtonListener() {
        binding.buttonSubmit.setOnClickListener {
            val company = binding.textInputEditTextLoginCompany.text.toString()
            val user = binding.textInputEditTextLoginUser.text.toString()
            val password = binding.textInputEditTextLoginPassword.text.toString()
            val customUrl = binding.customUrl.text.toString()

            viewModel.loginCompany(
                company,
                user,
                password,
                customUrl,
                requireContext()
            ) { isNewTerminal ->
                viewModel.loadPermissions(requireContext()) { worked ->
                    if (worked) {
                        if (isNewTerminal) showLangAndTimezone() else openMainView(false)
                    }
                }
            }
        }

        binding.goToInetSettingsButton.setOnClickListener {
            val goToInetSettingsActivity = Intent(context, NoInternetNetworkSettingsActivity::class.java)
            startActivity(goToInetSettingsActivity)
        }
    }

    private fun initSaveSettingsButton() {
        binding.buttonSubmit.setOnClickListener {
            val lang = binding.dropdownMenuLanguage.text.toString()
            val tz = binding.dropdownMenuTimezone.text.toString()
            //Set language and timezone locally and send it to backend
            viewModel.saveLangAndTimezone(requireActivity(), lang, tz) {
                openMainView(true)
            }
        }
    }
}
