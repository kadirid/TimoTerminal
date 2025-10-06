package com.timo.timoterminal.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.ProjectTimeEntity
import com.timo.timoterminal.repositories.ProjectTimeRepository
import kotlinx.coroutines.launch

class ProjectTimeListFragmentViewModel(
    private val projectTimeRepository: ProjectTimeRepository
) : ViewModel() {
    private val _items = MutableLiveData<List<ProjectTimeEntity>>(emptyList())
    val items: LiveData<List<ProjectTimeEntity>> = _items
    private var currentPage = 0
    private var isLoading = false

    fun loadMoreItems(userId: String?) {
        if (!isLoading) {
            isLoading = true
            viewModelScope.launch {
                // Fetch data
                val newItems = projectTimeRepository.getPageAsList(currentPage, userId)
                _items.value = _items.value?.plus(newItems) ?: newItems
                currentPage++
                isLoading = false
            }
        }
    }
}