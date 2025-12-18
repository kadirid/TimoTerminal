package com.timo.timoterminal.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity
import com.timo.timoterminal.repositories.AbsenceEntryRepository
import kotlinx.coroutines.launch

class AbsenceListViewModel(
    private val absenceEntryRepository: AbsenceEntryRepository
): ViewModel() {
    private val _items = MutableLiveData <List<AbsenceEntryEntity>>(emptyList())
    val items: LiveData<List<AbsenceEntryEntity>> = _items
    private var currentPage = 0
    private var isLoading = false

    fun loadMoreItems(userId: String?){
        if(isLoading) return
        isLoading = true
        viewModelScope.launch {
            val newItems = absenceEntryRepository.getPageAsList(currentPage, userId)
            val currentList = _items.value ?: emptyList()
            _items.postValue(currentList + newItems)
            if(newItems.isNotEmpty()) {
                currentPage++
            }
            isLoading = false
        }
    }
}