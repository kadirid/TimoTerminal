package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentBookingListBinding
import com.timo.timoterminal.entityAdaptor.BookingBUEntityAdapter
import com.timo.timoterminal.entityClasses.BookingBUEntity
import com.timo.timoterminal.repositories.BookingBURepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.viewModel.BookingListFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class BookingListFragment : Fragment() {
    private lateinit var binding: FragmentBookingListBinding
    private lateinit var adapter: BookingBUEntityAdapter
    private val bookingBURepository: BookingBURepository by inject()
    private val userRepository: UserRepository by inject()
    private val languageService: LanguageService by inject()
    private val bookingListFragmentViewModel = BookingListFragmentViewModel(bookingBURepository)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookingListBinding.inflate(inflater, container, false)

        setAdapter()
        setListener()

        return binding.root
    }

    private fun setListener() {
        binding.fragmentBookingListRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.fragmentBookingListNestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            (activity as MainActivity?)?.restartTimer()
        }
    }

    private fun setAdapter() {
        bookingListFragmentViewModel.viewModelScope.launch {
            val userEntities = userRepository.getAllAsList()
            val userMap = HashMap<String,String>()
            for(user in userEntities){
                userMap[user.card] = user.name()
            }
            val statusMap = HashMap<Int, String>()
            statusMap[100] = languageService.getText("#Kommt")
            statusMap[200] = languageService.getText("#Geht")
            statusMap[110] = languageService.getText("ALLGEMEIN#Pause")
            bookingListFragmentViewModel.getAll().collect {
                adapter = BookingBUEntityAdapter(it, userMap, statusMap,
                    object : BookingBUEntityAdapter.OnItemClickListener {
                        override fun onItemClick(entity: BookingBUEntity) {
                            (activity as MainActivity?)?.restartTimer()
                            // do stuff here
                        }
                    })
                binding.viewRecyclerBuBookingAll.adapter = adapter
            }
        }
    }

}