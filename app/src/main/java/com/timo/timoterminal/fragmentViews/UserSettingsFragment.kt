package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.databinding.FragmentUserSettingsBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor
import com.timo.timoterminal.entityAdaptor.UserEntityAdaptor.OnItemClickListener
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.viewModel.UserSettingsFragmentViewModel
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
class UserSettingsFragment : Fragment() {
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
        setAdapter()
        return binding.root
    }

    private fun setAdapter() {
        userSettingsFragmentViewModel.viewModelScope.launch {
            adapter = UserEntityAdaptor(userSettingsFragmentViewModel.getAllUserEntities(),
                object : OnItemClickListener {
                    override fun onItemClick(user: UserEntity) {
                        loadFormData(user)
                    }
                })
            binding.viewRecyclerUser.adapter = adapter
        }
    }

    private fun loadFormData(user: UserEntity) {
        binding.textViewId.text = "${user.id}"
        binding.textInputEditTextName.setText(user.name)
        binding.textInputEditTextCard.setText(user.card)
        binding.textInputEditTextPin.setText(user.pin)
    }

    private fun setUpOnClickListeners() {
        binding.buttonUserSave.setOnClickListener {
            val user = UserEntity(
                binding.textInputEditTextName.text.toString(),
                binding.textInputEditTextCard.text.toString(),
                binding.textInputEditTextPin.text.toString()
            )
            user.setId(binding.textViewId.text.toString())
            userSettingsFragmentViewModel.saveOrUpdate(user)
        }
        binding.buttonUserDelete.setOnClickListener {
            if (!binding.textViewId.text.toString().isNullOrEmpty()) {
                val user = UserEntity(
                    binding.textViewId.text.toString().toLong(),
                    binding.textInputEditTextName.text.toString(),
                    binding.textInputEditTextCard.text.toString(),
                    binding.textInputEditTextPin.text.toString()
                )
                userSettingsFragmentViewModel.deleteEntity(user)
            }
        }
        binding.buttonUserLoad.setOnClickListener {
            httpService.get(
                "http://192.168.0.45/timo_prd/services/rest/zktecoTerminal/loadUser",
                mapOf(Pair("firma", "standalone")),
                { _, obj, _ ->
                    if (obj != null) {
                        for (c in 0 until obj.length()) {
                            val item = obj.getJSONObject(c)
                            val user = UserEntity(
                                item.getLong("id"),
                                item.getString("name"),
                                item.getString("card"),
                                item.getString("pin")
                            )
                            userSettingsFragmentViewModel.addEntity(user)
                        }
                    }
                },
                {})
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
}