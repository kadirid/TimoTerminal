package com.timo.timoterminal.viewModel

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entities.DemoEntity
import com.timo.timoterminal.repositories.DemoRepository
import com.timo.timoterminal.service.HttpService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivityViewModel(private val demoRepository: DemoRepository, private val httpService: HttpService) : ViewModel() {
    val demoEntities : Flow<List<DemoEntity>> = demoRepository.getAllEntities

    fun initHearbeatService(application: Application, lifecycleOwner: LifecycleOwner ) {
        httpService.initHearbeatWorker(application, lifecycleOwner)
    }

    // db changes should not run on mainScope, this is why we run it in viewModelScope independently
    fun addEntitiy(demoEntity: DemoEntity) {
        viewModelScope.launch {
            demoRepository.insertDemoEntity(demoEntity)
        }
    }
}

