package com.timo.timoterminal.modalBottomSheets

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.LoginWelcomeModalBinding
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import org.koin.android.ext.android.inject
import java.util.Locale


class MBLoginWelcomeSheet : DialogFragment() {
    private val sharedPrefService: SharedPrefService by inject()

    lateinit var binding: LoginWelcomeModalBinding
    companion object {
        const val TAG = "MBLoginWelcomeSheet"
    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog?.window?.setLayout(width, height)
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            Utils.hideNavInDialog(this.dialog)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LoginWelcomeModalBinding.inflate(inflater, container, false)
        dialog?.window?.attributes?.windowAnimations = R.style.DialogAnimation

        binding.iconButton.setOnClickListener {
            dismiss()
        }

        val locale =
            Locale(sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "de")!!)
        val config = Configuration()
        config.setLocale(locale)
        val context = activity?.baseContext?.createConfigurationContext(config)
        binding.welcomeText.text = context?.getText(R.string.wilkommen_bei_timo)

        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

}