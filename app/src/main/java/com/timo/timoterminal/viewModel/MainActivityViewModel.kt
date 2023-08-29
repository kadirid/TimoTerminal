package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entities.DemoEntity
import com.timo.timoterminal.repositories.DemoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivityViewModel(private val demoRepository: DemoRepository) : ViewModel() {
    val demoEntities : Flow<List<DemoEntity>> = demoRepository.getAllEntities

    // db changes should not run on mainScope, this is why we run it in viewModelScope independently
    fun addEntitiy(demoEntity: DemoEntity) {
        viewModelScope.launch {
            demoRepository.insertDemoEntity(demoEntity)
        }
    }
}

