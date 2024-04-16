package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
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
        return binding.root
    }

    private fun setText() {
        userSettingsFragmentViewModel.viewModelScope.launch {
            binding.textviewPersonalIdLabel.text = languageService.getText("#JourneyId")
            binding.textviewFirstNameLabel.text = languageService.getText("MITARBEITER#Vorname")
            binding.textviewLastNameLabel.text = languageService.getText("MITARBEITER#Nachname")
            binding.textviewHireDateLabel.text =
                languageService.getText("MITARBEITER#Einstellungsdatum")
            binding.searchBar.hint = languageService.getText("#SearchEmployee")
            binding.searchView.hint = languageService.getText("#SearchEmployee")
        }
    }

    private fun initSearchFilter() {
        userSettingsFragmentViewModel.viewModelScope.launch {
            binding.searchView.editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // This method is called before the text changes.
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    (activity as MainActivity?)?.restartTimer()
                    userSettingsFragmentViewModel.viewModelScope.launch {
                        val queriedList = userSettingsFragmentViewModel.getAllAsList().filter {
                            it.lastName.toLowerCase(Locale.current).contains(s) ||
                                    it.firstName.toLowerCase(Locale.current).contains(s) ||
                                    s.toString() == it.card || s.toString() == it.id.toString()
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
        val emptyAdapter = UserEntityAdaptor(emptyList(),
            object : OnItemClickListener {
                override fun onItemClick(user: UserEntity) {}
            })
        binding.viewRecyclerUserFilter.adapter = emptyAdapter
        binding.viewRecyclerUserAll.adapter = emptyAdapter
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
        binding.buttonFingerprint.isEnabled = true
        binding.buttonDeleteFingerprint.isEnabled = true
        binding.buttonDeleteFingerprint.setIconTintResource(R.color.red)
    }

    private fun setUpOnClickListeners() {
        userSettingsFragmentViewModel.viewModelScope.launch {
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
                                paramMap["id"], paramMap["editor"], isFP = true, isDelete = true
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

            binding.fragmentUserSettingsRootLayout.setOnClickListener {
                (activity as MainActivity?)?.restartTimer()
            }
            binding.fragmentUserSettingsNestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
                (activity as MainActivity?)?.restartTimer()
            }
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        // get Key associated to the fingerprint
        FingerprintService.identify(template)?.run {
            Log.d("FP Key", this)
            val id = this.substring(0, this.length - 2)
            (activity as MainActivity?)?.restartTimer()
            binding.searchView.show()
            binding.searchView.setText(id)
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
        const val TAG = "UserSettingsFragmentTag"

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