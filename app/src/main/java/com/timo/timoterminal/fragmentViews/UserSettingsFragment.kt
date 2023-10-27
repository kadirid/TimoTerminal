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
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserSettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserSettingsFragment : Fragment(), RfidListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val userSettingsFragmentViewModel: UserSettingsFragmentViewModel by viewModel()
    private lateinit var binding: FragmentUserSettingsBinding
    private lateinit var adapter: UserEntityAdaptor
    private val httpService: HttpService = HttpService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
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
        return binding.root
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

    override fun onResume() {
        super.onResume()
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserSettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UserSettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
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