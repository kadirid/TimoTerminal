package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.repositories.BookingBURepository
import kotlinx.coroutines.launch

class BookingListFragmentViewModel(
    private val bookingBURepository: BookingBURepository
) : ViewModel() {

    fun getAll() = bookingBURepository.getAllEntities()

    fun getValues(){
        viewModelScope.launch {

        }
    }

}