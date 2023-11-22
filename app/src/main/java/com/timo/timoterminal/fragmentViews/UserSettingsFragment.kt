package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentUserSettingsBinding
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor.OnItemClickListener
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject

private const val ARG_USERID = "userId"

class UserSettingsFragment : Fragment(), RfidListener, FingerprintListener {

    private val userSettingsFragmentViewModel: UserSettingsFragmentViewModel by viewModel()
    private lateinit var binding: FragmentUserSettingsBinding
    private lateinit var adapter: UserEntityAdaptor
    private val languageService: LanguageService by inject()
    private var isRegister = false
    private val paramMap = HashMap<String, String>()
    private var userId: Long = -1

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
        RfidService.setListener(this)
        RfidService.register()
        userSettingsFragmentViewModel.loadUserFromServer()
        setAdapter()
        initSearchFilter()
        setText()
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
                (requireActivity() as MainActivity).restartTimer()
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

    override fun onDetach() {
        super.onDetach()
        RfidService.unregister()
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
        paramMap["editor"] = userId.toString()
        binding.personalIdText.text = user.id.toString()
        binding.firstNameText.text = user.firstName
        binding.lastNameText.text = user.lastName
        binding.hireDateText.text = Utils.getDateFromTimestamp(user.hireDate)
        binding.buttonRfid.isEnabled = true
        binding.buttonPin.isEnabled = true
        binding.buttonFingerprint.isEnabled = true
    }

    private fun setUpOnClickListeners() {
        binding.buttonUserLoad.setOnClickListener {
            userSettingsFragmentViewModel.loadUserFromServer()
            (requireActivity() as MainActivity).restartTimer()
        }

        binding.buttonRfid.setOnClickListener {
            (requireActivity() as MainActivity).restartTimer()
            isRegister = true
        }
        binding.buttonFingerprint.setOnClickListener {

        }
        binding.buttonPin.setOnClickListener {

        }

        binding.fragmentUserSettingsRootLayout.setOnClickListener {
            (requireActivity() as MainActivity).restartTimer()
        }
        binding.fragmentUserSettingsNestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            (requireActivity() as MainActivity).restartTimer()
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            (requireActivity() as MainActivity).restartTimer()
            if (isRegister) {
                paramMap["card"] = oct
                userSettingsFragmentViewModel.updateUser(paramMap, this)
            } else {
                binding.searchView.show()
                binding.searchView.setText(oct)
            }
        }
    }

    fun afterUpdate(success: Boolean, message: String) {
        if (success) {
            paramMap.clear()
            isRegister = false
            requireActivity().runOnUiThread {
                binding.buttonRfid.isEnabled = false
                binding.buttonPin.isEnabled = false
                binding.buttonFingerprint.isEnabled = false
            }
        } else {
            requireActivity().runOnUiThread {
                Utils.showMessage(parentFragmentManager ,message)
            }
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
}