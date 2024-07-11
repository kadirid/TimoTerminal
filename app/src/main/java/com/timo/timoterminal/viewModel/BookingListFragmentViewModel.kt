package com.timo.timoterminal.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.BookingBUEntity
import com.timo.timoterminal.repositories.BookingBURepository
import kotlinx.coroutines.launch

class BookingListFragmentViewModel(
    private val bookingBURepository: BookingBURepository
) : ViewModel() {
    private val _items = MutableLiveData<List<BookingBUEntity>>(emptyList())
    val items: LiveData<List<BookingBUEntity>> = _items
    private var currentPage = 0
    private var isLoading = false

    fun loadMoreItems() {
        if (!isLoading) {
            isLoading = true
            viewModelScope.launch {
                // Fetch data from a remote source or local database
                val newItems = bookingBURepository.getPageAsList(currentPage)
                _items.value = _items.value?.plus(newItems) ?: newItems
                currentPage++
                isLoading = false
            }
        }
    }

}