package com.timo.timoterminal.fragmentViews

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.BuildConfig
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.activities.NoInternetNetworkSettingsActivity
import com.timo.timoterminal.databinding.FragmentLoginBinding
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.LoginService
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.service.SettingsService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.CodesArrayAdapter
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.LoginFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class LoginFragment : Fragment() {
    private val propertyService: PropertyService by inject()
    private val loginService: LoginService by inject()
    private val settingsService: SettingsService by inject()
    private val sharedPrefService: SharedPrefService by inject()
    private val languageService: LanguageService by inject()
    private val soundSource: SoundSource by inject()
    private val viewModel: LoginFragmentViewModel =
        LoginFragmentViewModel(loginService, settingsService, languageService, sharedPrefService)
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        initButtonListener()
        initLanguageDropdown()
        initTimezoneDropdown()
        initDebugFields()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Utils.hideStatusAndNavbar(requireActivity())
        viewModel.onResume { isOnline ->
            if (isOnline) {
                showLogin()
            } else {
                val goToInetSettingsActivity =
                    Intent(context, NoInternetNetworkSettingsActivity::class.java)
                startActivity(goToInetSettingsActivity)
            }
        }
    }

    private fun initDebugFields() {
        viewModel.viewModelScope.launch {
            if (BuildConfig.DEBUG) {
                val url = propertyService.getProperties().getProperty("customUrl")
                val company = propertyService.getProperties().getProperty("company")
                val username = propertyService.getProperties().getProperty("username")
                val password = propertyService.getProperties().getProperty("password")
                binding.customUrl.setText(url)
                binding.customUrl.visibility = View.VISIBLE
                binding.textInputEditTextLoginCompany.setText(if (!company.isNullOrEmpty()) company else "")
                binding.textInputEditTextLoginUser.setText(if (!username.isNullOrEmpty()) username else "")
                binding.textInputEditTextLoginPassword.setText(if (!password.isNullOrEmpty()) password else "")
           }
        }
    }

    private fun openMainView(isNewTerminal: Boolean?) {
        val goToMainActivity = Intent(context, MainActivity::class.java)
        goToMainActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        goToMainActivity.putExtra("isNewTerminal", isNewTerminal)
        startActivity(goToMainActivity)
    }

    private fun initLanguageDropdown() {
        viewModel.viewModelScope.launch {
            val languages = viewModel.languages
            val adapter =
                ArrayAdapter(requireContext(), R.layout.dropdown, languages)
            binding.dropdownMenuLanguage.setAdapter(adapter)
            binding.dropdownMenuLanguage.setText(languages[0], false)
        }
    }

    private fun initTimezoneDropdown() {
        viewModel.viewModelScope.launch {
            val ids = TimeZone.getAvailableIDs()
            val timeZones = mutableListOf<CodesArrayAdapter.TimeZoneListEntry>()
            for (id in ids) {
                val timeZone = TimeZone.getTimeZone(id)
                val offset = Utils.getOffset(timeZone.getOffset(Date().time))
                timeZones.add(CodesArrayAdapter.TimeZoneListEntry(id, timeZone.displayName, offset))
            }
            val adapter = CodesArrayAdapter(requireContext(), R.layout.double_dropdown, timeZones)
            binding.dropdownMenuTimezone.setAdapter(adapter)
            binding.dropdownMenuTimezone.setText("Europe/Berlin", false)
        }
    }

    private fun showLogin() {
        // INIT ANIMATION
        val animSet = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_move_up)
        val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.dropdownMenuLayoutLanguage.visibility = View.GONE
                binding.dropdownMenuLayoutTimezone.visibility = View.GONE

                binding.textInputLayoutLoginCompany.startAnimation(fadeInAnimation)
                binding.textInputLayoutLoginCompany.visibility = View.VISIBLE

                binding.textInputLayoutLoginPassword.startAnimation(fadeInAnimation)
                binding.textInputLayoutLoginPassword.visibility = View.VISIBLE

                binding.textInputLayoutLoginUser.startAnimation(fadeInAnimation)
                binding.textInputLayoutLoginUser.visibility = View.VISIBLE

                val locale =
                    Locale(sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "de")!!)
                viewModel.viewModelScope.launch {
                    soundSource.loadForLogin(locale.language)
                }
                val config = Configuration()
                config.setLocale(locale)
                val context = activity?.baseContext?.createConfigurationContext(config)
                binding.buttonSubmit.text = context?.getText(R.string.login)
                binding.linearTitleContainerText.text =
                    context?.getText(R.string.please_enter_timo_login_data)
                binding.textInputLayoutLoginCompany.hint = context?.getText(R.string.company)
                binding.textInputLayoutLoginPassword.hint = context?.getText(R.string.password)
                binding.textInputLayoutLoginUser.hint = context?.getText(R.string.loginname)
                initLoginButton()
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        activity?.runOnUiThread {
            binding.dropdownMenuLayoutLanguage.startAnimation(animSet)
            binding.dropdownMenuLayoutTimezone.startAnimation(animSet)
        }
    }

    private fun initButtonListener() {
        viewModel.viewModelScope.launch {
            binding.buttonSubmit.setSafeOnClickListener {
                val lang = binding.dropdownMenuLanguage.text.toString()
                val tz = binding.dropdownMenuTimezone.text.toString()
                //Set language and timezone locally and send it to backend
                viewModel.saveLangAndTimezone(requireActivity(), lang, tz) { isOnline ->
                    if (isOnline) {
                        showLogin()
                    } else {
                        val goToInetSettingsActivity =
                            Intent(context, NoInternetNetworkSettingsActivity::class.java)
                        startActivity(goToInetSettingsActivity)
                    }
                }
            }

            binding.goToInetSettingsButton.setSafeOnClickListener {
                val goToInetSettingsActivity =
                    Intent(context, NoInternetNetworkSettingsActivity::class.java)
                startActivity(goToInetSettingsActivity)
            }
        }
    }

    private fun initLoginButton() {
        binding.buttonSubmit.setSafeOnClickListener {
            val company = binding.textInputEditTextLoginCompany.text.toString()
            val user = binding.textInputEditTextLoginUser.text.toString()
            val password = binding.textInputEditTextLoginPassword.text.toString()
            val customUrl = binding.customUrl.text.toString()

            if (company.isNotEmpty() && user.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginCompany(
                    company,
                    user,
                    password,
                    customUrl,
                    requireContext()
                ) { isNewTerminal ->
                    viewModel.loadPermissions(requireContext()) { worked ->
                        if (worked) {
                            openMainView(isNewTerminal)
                        }
                    }
                }
            } else {
                val locale =
                    Locale(sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "de")!!)
                val config = Configuration()
                config.setLocale(locale)
                val context = activity?.baseContext?.createConfigurationContext(config)
                if (context != null) {
                    Utils.showErrorMessage(
                        requireContext(),
                        context.getText(R.string.fill_out_fields).toString()
                    )
                }else{
                    Utils.showErrorMessage(
                        requireContext(),
                        requireContext().getText(R.string.fill_out_fields).toString()
                    )
                }
            }
        }
    }
}
