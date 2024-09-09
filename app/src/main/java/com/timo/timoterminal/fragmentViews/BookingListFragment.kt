package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
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
    private var recyclerViewState: Parcelable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookingListBinding.inflate(inflater, container, false)

        setAdapter()
        setUp()

        return binding.root
    }

    private fun setUp() {
        binding.fragmentBookingListRootLayout.setOnClickListener {
            (activity as MainActivity?)?.restartTimer()
        }
        binding.fragmentBookingListEmptyListTextView.text =
            languageService.getText("#NoEntriesForBookedTimes")
        binding.buttonClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun setAdapter() {
        adapter = BookingBUEntityAdapter(
            emptyList(),
            emptyMap(),
            emptyMap(),
            emptyMap(),
            object : BookingBUEntityAdapter.OnItemClickListener {
                override fun onItemClick(entity: BookingBUEntity) {}
            })
        binding.viewRecyclerBuBookingAll.adapter = adapter

        binding.viewRecyclerBuBookingAll.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (activity as MainActivity?)?.restartTimer()
                if (dy > 0 && !recyclerView.canScrollVertically(1)) { // Scrolled to bottom
                    bookingListFragmentViewModel.loadMoreItems()
                }
            }
        })

        bookingListFragmentViewModel.items.observe(viewLifecycleOwner) {
            bookingListFragmentViewModel.viewModelScope.launch {
                val userEntities = userRepository.getAllAsList()
                val userMap = HashMap<String, String>()
                val userIdMap = HashMap<Long, String>()
                for (user in userEntities) {
                    userMap[user.card] = user.name()
                    userIdMap[user.id] = user.name()
                }
                val statusMap = HashMap<Int, String>()
                statusMap[100] = languageService.getText("#Kommt")
                statusMap[200] = languageService.getText("#Geht")
                statusMap[110] = languageService.getText("ALLGEMEIN#Pause")
                statusMap[210] = languageService.getText("ALLGEMEIN#Pausenende")

                recyclerViewState =
                    binding.viewRecyclerBuBookingAll.layoutManager?.onSaveInstanceState()
                binding.viewRecyclerBuBookingAll.adapter =
                    BookingBUEntityAdapter(it, userMap, userIdMap, statusMap,
                        object : BookingBUEntityAdapter.OnItemClickListener {
                            override fun onItemClick(entity: BookingBUEntity) {
                                (activity as MainActivity?)?.restartTimer()
                                // do stuff here
                            }
                        })
                binding.viewRecyclerBuBookingAll.layoutManager?.onRestoreInstanceState(
                    recyclerViewState
                )
            }
            if (it.isNotEmpty()) {
                binding.viewRecyclerBuBookingAll.visibility = View.VISIBLE
                binding.fragmentBookingListEmptyListTextView.visibility = View.GONE
            }
        }
        bookingListFragmentViewModel.loadMoreItems()
    }

}