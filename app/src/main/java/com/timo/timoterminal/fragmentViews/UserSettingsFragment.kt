package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentUserSettingsBinding
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor.OnItemClickListener
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.modalBottomSheets.MBUserWaitSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


private const val ARG_USERID = "userId"

class UserSettingsFragment : Fragment(), TimoRfidListener, FingerprintListener {

    private val userSettingsFragmentViewModel: UserSettingsFragmentViewModel by viewModel()
    private lateinit var binding: FragmentUserSettingsBinding
    private lateinit var adapter: UserEntityAdaptor
    private val languageService: LanguageService by inject()
    private val paramMap = HashMap<String, String>()
    private var userId: Long = -1
    private var assignedToTerminal = false
    private val soundSource: SoundSource by inject()

    private val assignUser = fun(callback: () -> Unit?) {
        (activity as MainActivity?)?.restartTimer()
        requireActivity().runOnUiThread {
            val dlgAlert: AlertDialog.Builder =
                AlertDialog.Builder(requireContext(), R.style.MyDialog)
            dlgAlert.setMessage(languageService.getText("#AssignUserToTerminal"))
            dlgAlert.setTitle(languageService.getText("#Attention"))
            dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
            dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                (activity as MainActivity?)?.showLoadMask()
                userSettingsFragmentViewModel.assignUser(paramMap["id"]!!, userId, callback)
            }
            val dialog = dlgAlert.create()
            Utils.hideNavInDialog(dialog)
            dialog.setOnShowListener {
                val textView = dialog.findViewById<TextView>(android.R.id.message)
                textView?.textSize = 40f
            }
            dialog.show()
            dialog.window?.setLayout(680, 324)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getLong(ARG_USERID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserSettingsBinding.inflate(inflater, container, false)
        setUpOnClickListeners()
        setAdapter()
        initSearchFilter()
        setText()
        soundSource.loadForFP()
        return binding.root
    }

    private fun setText() {
        binding.textviewPersonalIdLabel.text = languageService.getText("#JourneyId")
        binding.textviewFirstNameLabel.text = languageService.getText("MITARBEITER#Vorname")
        binding.textviewLastNameLabel.text = languageService.getText("MITARBEITER#Nachname")
        binding.textviewHireDateLabel.text =
            languageService.getText("MITARBEITER#Einstellungsdatum")
    }

    private fun initSearchFilter() {
        binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // This method is called before the text changes.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                (activity as MainActivity?)?.restartTimer()
                userSettingsFragmentViewModel.viewModelScope.launch {
                    val queriedList = userSettingsFragmentViewModel.getAllAsList().filter {
                        it.lastName.toLowerCase(Locale.current).contains(s) ||
                                it.firstName.toLowerCase(Locale.current).contains(s) ||
                                s.toString() == it.card
                    }
                    binding.viewRecyclerUserFilter.adapter = UserEntityAdaptor(queriedList,
                        object : OnItemClickListener {
                            override fun onItemClick(user: UserEntity) {
                                loadFormData(user)
                            }
                        })
                }
            }

            override fun afterTextChanged(s: Editable) {
                // This method is called after the text changes.
            }
        })
    }

    override fun onResume() {
        super.onResume()

        RfidService.unregister()
        RfidService.setListener(this)
        RfidService.register()
    }

    override fun onPause() {
        RfidService.unregister()

        super.onPause()
    }

    private fun setAdapter() {
        userSettingsFragmentViewModel.viewModelScope.launch {
            userSettingsFragmentViewModel.getAll().collect {
                adapter = UserEntityAdaptor(it,
                    object : OnItemClickListener {
                        override fun onItemClick(user: UserEntity) {
                            loadFormData(user)
                        }
                    })
                binding.viewRecyclerUserAll.adapter = adapter
            }
        }
    }

    private fun loadFormData(user: UserEntity) {
        paramMap["id"] = user.id.toString()
        assignedToTerminal = user.assignedToTerminal
        paramMap["editor"] = userId.toString()
        binding.personalIdText.text = user.id.toString()
        binding.firstNameText.text = user.firstName
        binding.lastNameText.text = user.lastName
        binding.hireDateText.text = Utils.getDateFromTimestamp(user.hireDate)
        binding.buttonRfid.isEnabled = true
        binding.buttonPin.isEnabled = true
        binding.buttonFingerprint.isEnabled = true
        binding.buttonDeleteFingerprint.isEnabled = true
        binding.buttonDeleteFingerprint.setIconTintResource(R.color.red)
    }

    private fun setUpOnClickListeners() {
        binding.buttonUserLoad.setOnClickListener {
            userSettingsFragmentViewModel.loadUserFromServer()
            (activity as MainActivity?)?.restartTimer()
        }

        binding.buttonRfid.setSafeOnClickListener {
            val call = fun() {
                (activity as MainActivity?)?.hideLoadMask()
                (activity as MainActivity?)?.cancelTimer()
                requireActivity().runOnUiThread {
                    val sheet =
                        MBUserWaitSheet.newInstance(paramMap["id"], paramMap["editor"], false)
                    sheet.show(parentFragmentManager, MBUserWaitSheet.TAG)
                }
            }
            if (Utils.isOnline(requireContext())) {
                if (paramMap["id"] != null) {
                    if (assignedToTerminal) {
                        call()
                    } else {
                        assignUser(call)
                    }
                }
            } else {
                showMsg(languageService.getText("#OfflineNoUserEdit"))//Only online
            }
        }
        binding.buttonFingerprint.setSafeOnClickListener {
            val call = fun() {
                (activity as MainActivity?)?.hideLoadMask()
                (activity as MainActivity?)?.cancelTimer()
                requireActivity().runOnUiThread {
                    val sheet =
                        MBUserWaitSheet.newInstance(paramMap["id"], paramMap["editor"], true)
                    sheet.show(parentFragmentManager, MBUserWaitSheet.TAG)
                }
            }
            if (Utils.isOnline(requireContext())) {
                if (paramMap["id"] != null) {
                    if (assignedToTerminal) {
                        call()
                    } else {
                        assignUser(call)
                    }
                }
            } else {
                showMsg(languageService.getText("#OfflineNoUserEdit"))//Only online
            }
        }
        binding.buttonDeleteFingerprint.setSafeOnClickListener {
            val call = fun() {
                (activity as MainActivity?)?.hideLoadMask()
                (activity as MainActivity?)?.cancelTimer()
                requireActivity().runOnUiThread {
                    val sheet =
                        MBUserWaitSheet.newInstance(
                            paramMap["id"], paramMap["editor"],
                            isFP = true,
                            isDelete = true
                        )
                    sheet.show(parentFragmentManager, MBUserWaitSheet.TAG)
                }
            }
            if (Utils.isOnline(requireContext())) {
                if (paramMap["id"] != null) {
                    if (assignedToTerminal) {
                        call()
                    } else {
                        assignUser(call)
                    }
                }
            } else {
                showMsg(languageService.getText("#OfflineNoUserEdit"))//Only online
            }
        }
        binding.buttonPin.setOnClickListener {
            val call = fun() {
                (activity as MainActivity?)?.hideLoadMask()
                (activity as MainActivity?)?.restartTimer()
                requireActivity().runOnUiThread {

                    val textLayout = TextInputLayout(requireContext())
                    textLayout.hint = "Pin Code"
                    textLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE

                    val passCodeEditText = TextInputEditText(textLayout.context)
                    passCodeEditText.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    passCodeEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40F)
                    val layoutParams = LinearLayout.LayoutParams(600, 100)
                    passCodeEditText.layoutParams = layoutParams

                    textLayout.addView(passCodeEditText)

                    val dlgAlert: AlertDialog.Builder =
                        AlertDialog.Builder(requireContext(), R.style.MyDialog)
                    dlgAlert.setMessage(languageService.getText("#NewPIN"))
                    dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
                    dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                        val code = passCodeEditText.text
                        paramMap["pin"] = code.toString()
                        (activity as MainActivity?)?.showLoadMask()
                        userSettingsFragmentViewModel.updatePin(paramMap) { obj ->
                            if (obj != null) {
                                if (obj.optString("error", "").isEmpty()) {
                                    activity?.runOnUiThread {
                                        showMsg(
                                            obj.optString("message", "")
                                        )
                                    }
                                } else {
                                    Utils.showErrorMessage(requireContext(), obj.getString("error"))
                                }
                            }
                            (activity as MainActivity?)?.hideLoadMask()
                        }
                    }
                    val dialog = dlgAlert.create()
                    Utils.hideNavInDialog(dialog)
                    passCodeEditText.doOnTextChanged { _, _, _, _ ->
                        (activity as MainActivity?)?.restartTimer()
                    }
                    dialog.setView(textLayout, 20, 0, 20, 0)
                    dialog.setOnShowListener {
                        val textView = dialog.findViewById<TextView>(android.R.id.message)
                        textView?.textSize = 40f
                    }
                    dialog.show()
                }
            }
            if (Utils.isOnline(requireContext())) {
                if (paramMap["id"] != null) {
                    if (assignedToTerminal) {
                        call()
                    } else {
                        assignUser(call)
                    }
                }
            } else {
                showMsg(languageService.getText("#OfflineNoUserEdit"))//Only online
            }
        }

        binding.fragmentUserSettingsRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.fragmentUserSettingsNestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            (activity as MainActivity?)?.restartTimer()
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        Log.d("FP", fingerprint)
        // get Key associated to the fingerprint
        FingerprintService.identify(template)?.run {
            Log.d("FP Key", this)
            // TODO("maybe use this (Key of Fingerprint) to get User to show")
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            (activity as MainActivity?)?.restartTimer()
            binding.searchView.show()
            binding.searchView.setText(oct)
        }
    }

    companion object {
        fun newInstance(userId: Long) =
            UserSettingsFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_USERID, userId)
                }
            }
    }

    private fun showMsg(text: String) {
        Utils.showMessage(parentFragmentManager, text)
    }
}