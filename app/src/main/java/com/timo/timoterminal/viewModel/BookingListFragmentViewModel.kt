package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import com.timo.timoterminal.repositories.BookingBURepository

class BookingListFragmentViewModel(
    private val bookingBURepository: BookingBURepository
) : ViewModel() {

    fun getAll() = bookingBURepository.getAllEntities()

}