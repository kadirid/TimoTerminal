package com.timo.timoterminal.modalBottomSheets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.timo.timoterminal.databinding.LoginWelcomeModalBinding

class MBLoginWelcomeSheet() : DialogFragment() {

    lateinit var binding: LoginWelcomeModalBinding
    companion object {
        const val TAG = "MBLoginWelcomeSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoginWelcomeModalBinding.inflate(inflater, container, false)
        binding.iconButton.setOnClickListener {
            dialog?.dismiss()
        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

}