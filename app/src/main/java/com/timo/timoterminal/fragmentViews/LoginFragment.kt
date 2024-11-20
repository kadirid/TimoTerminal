package com.timo.timoterminal.fragmentViews

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.BuildConfig
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.activities.NoInternetNetworkSettingsActivity
import com.timo.timoterminal.databinding.DialogSingleTextInputBinding
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
    private lateinit var audioManager: AudioManager
    private var first = true
    private var firstRes = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        initButtonListener()
        initLanguageDropdown()
        initTimezoneDropdown()
        initDebugFields()
        setUpSeekBar()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        Utils.hideStatusAndNavbar(requireActivity())
        if(!firstRes)
            MainApplication.lcdk.hideSystemUI()
        firstRes = false
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
//                binding.customUrl.visibility = View.VISIBLE
                val fillLoginFields =
                    propertyService.getProperties().getProperty("fillLoginFields").equals("true")
                if (fillLoginFields) {
                    if (!url.equals("")) binding.customUrl.setText(url)
                    binding.textInputEditTextLoginCompany.setText(if (!company.isNullOrEmpty()) company else "")
                    binding.textInputEditTextLoginUser.setText(if (!username.isNullOrEmpty()) username else "")
                    binding.textInputEditTextLoginPassword.setText(if (!password.isNullOrEmpty()) password else "")
                }
            }
        }
    }

    private fun setUpSeekBar() {
        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        binding.volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        binding.volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
        if (first) {
            val animSet = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_move_up)
            val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

            animSet.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(p0: Animation?) {
                }

                override fun onAnimationEnd(p0: Animation?) {
                    binding.dropdownMenuLayoutLanguage.visibility = View.GONE
                    binding.dropdownMenuLayoutTimezone.visibility = View.GONE
                    binding.volumeTextView.visibility = View.GONE
                    binding.volumeSeekBar.visibility = View.GONE
                    binding.buttonPlaySound.visibility = View.GONE
                    binding.screenSaverContainerText.visibility = View.GONE
                    binding.screenSaverBtn.visibility = View.GONE

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
            first = false
        } else {
            binding.dropdownMenuLayoutLanguage.visibility = View.GONE
            binding.dropdownMenuLayoutTimezone.visibility = View.GONE
            binding.volumeTextView.visibility = View.GONE
            binding.volumeSeekBar.visibility = View.GONE
            binding.buttonPlaySound.visibility = View.GONE
            binding.screenSaverContainerText.visibility = View.GONE
            binding.screenSaverBtn.visibility = View.GONE
            binding.textInputLayoutLoginCompany.visibility = View.VISIBLE
            binding.textInputLayoutLoginPassword.visibility = View.VISIBLE
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
    }

    private fun initButtonListener() {
        viewModel.viewModelScope.launch {
            binding.buttonSubmit.setSafeOnClickListener {
                viewModel.viewModelScope.launch {
                    val lang = binding.dropdownMenuLanguage.text.toString()
                    if (!viewModel.languages.contains(lang)) {
                        binding.dropdownMenuLayoutLanguage.error = context?.getText(R.string.invalid_value)
                        Utils.showErrorMessage(
                            requireContext(),
                            context?.getText(R.string.invalid_value).toString(),
                            false
                        )
                        return@launch
                    }
                    binding.dropdownMenuLayoutLanguage.error = null
                    val tz = binding.dropdownMenuTimezone.text.toString()
                    if (!TimeZone.getAvailableIDs().contains(tz)) {
                        binding.dropdownMenuLayoutTimezone.error = context?.getText(R.string.invalid_value)
                        Utils.showErrorMessage(
                            requireContext(),
                            context?.getText(R.string.invalid_value).toString(),
                            false
                        )
                        return@launch
                    }
                    binding.dropdownMenuLayoutTimezone.error = null
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
            }

            binding.goToInetSettingsButton.setSafeOnClickListener {
                val goToInetSettingsActivity =
                    Intent(context, NoInternetNetworkSettingsActivity::class.java)
                startActivity(goToInetSettingsActivity)
            }

            binding.screenSaverBtn.setSafeOnClickListener {
                MainApplication.lcdk.showSystemUI()
                val goToScreenSaver = Intent(android.provider.Settings.ACTION_DREAM_SETTINGS)
                startActivity(goToScreenSaver);
            }

            binding.buttonPlaySound.setOnClickListener {
                soundSource.playSound(SoundSource.successSound)
            }

            binding.imageViewLogoBig.setOnLongClickListener {
                val title = "URL"
                val message: String
                val positive: String
                val negative: String

                val locale =
                    Locale(sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "de")!!)
                val config = Configuration()
                config.setLocale(locale)
                val context = activity?.baseContext?.createConfigurationContext(config)
                if (context != null) {
                    message = context.getText(R.string.enter_url_for_connection).toString()
                    positive = context.getText(R.string.apply).toString()
                    negative = context.getText(R.string.cancel).toString()
                } else {
                    message = requireContext().getText(R.string.enter_url_for_connection).toString()
                    negative = requireContext().getText(R.string.cancel).toString()
                    positive = requireContext().getText(R.string.apply).toString()
                }

                val dialogBinding = DialogSingleTextInputBinding.inflate(layoutInflater)

                val dlgAlert: AlertDialog.Builder =
                    AlertDialog.Builder(requireContext(), R.style.MyDialog)
                dlgAlert.setView(dialogBinding.root)
                dlgAlert.setNegativeButton(negative) { dia, _ ->
                    dialogBinding.dialogTextInputEditValue.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    val imm =
                        requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
                    dia.dismiss()
                }
                dlgAlert.setPositiveButton(positive) { _, _ ->
                    val code = dialogBinding.dialogTextInputEditValue.text
                    binding.customUrl.text = code
                    dialogBinding.dialogTextInputEditValue.onEditorAction(EditorInfo.IME_ACTION_DONE)
                    val imm =
                        requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view?.rootView?.windowToken, 0)
                }
                dialogBinding.dialogTextViewMessage.text = message
                dialogBinding.dialogTextInputLayoutValue.hint = title
                val dialog = dlgAlert.create()
                Utils.hideNavInDialog(dialog)
                dialog.setOnShowListener {
                    dialogBinding.dialogTextInputEditValue.isFocusableInTouchMode = true
                    dialogBinding.dialogTextInputEditValue.isFocusable = true
                    dialogBinding.dialogTextInputEditValue.inputType =
                        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
                    dialogBinding.dialogTextInputEditValue.text = binding.customUrl.text
                }
                dialog.show()
                true
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
                        context.getText(R.string.fill_out_fields).toString(),
                        false
                    )
                } else {
                    Utils.showErrorMessage(
                        requireContext(),
                        requireContext().getText(R.string.fill_out_fields).toString(),
                        false
                    )
                }
            }
        }
    }
}
