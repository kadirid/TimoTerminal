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
import com.timo.timoterminal.databinding.FragmentUserSettingsBinding
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor.OnItemClickListener
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.android.ext.android.inject

class UserSettingsFragment : Fragment(), RfidListener {

    private val userSettingsFragmentViewModel: UserSettingsFragmentViewModel by viewModel()
    private lateinit var binding: FragmentUserSettingsBinding
    private lateinit var adapter: UserEntityAdaptor
    private val languageService: LanguageService by inject()

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
        binding.textviewHireDateLabel.text = languageService.getText("MITARBEITER#Einstellungsdatum")
    }

    private fun initSearchFilter() {
        val search = binding.searchView.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // This method is called before the text changes.
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                userSettingsFragmentViewModel.viewModelScope.launch {
                    val queriedList = userSettingsFragmentViewModel.getAllAsList().filter {
                        it.lastName.toLowerCase(Locale.current).contains(s) || s.toString() == it.card
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
        binding.personalIdText.text = user.id.toString()
        binding.firstNameText.text = user.firstName
        binding.lastNameText.text = user.lastName
        binding.hireDateText.text = user.hireDate.toString()
    }

    private fun setUpOnClickListeners() {

        binding.buttonUserLoad.setOnClickListener {
            userSettingsFragmentViewModel.loadUserFromServer()
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
            binding.searchView.show()
            binding.searchView.setText(oct)
        }
    }

}