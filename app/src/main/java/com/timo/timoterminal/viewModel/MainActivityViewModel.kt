package com.timo.timoterminal.viewModel

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.entityClasses.DemoEntity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.DemoRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.SharedPrefService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val demoRepository: DemoRepository,
    private val userRepository: UserRepository,
    private val configRepository: ConfigRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService
) : ViewModel() {
    val demoEntities: Flow<List<DemoEntity>> = demoRepository.getAllEntities
    val userEntities: Flow<List<UserEntity>> = userRepository.getAllEntities

    fun initHeartbeatService(application: Application, lifecycleOwner: LifecycleOwner) {
        httpService.initHeartbeatWorker(application, lifecycleOwner)
    }

    fun killHeartBeatWorkers(application: Application) {
        httpService.killHeartBeatWorkers(application)
        viewModelScope.launch {
            sharedPrefService.removeAllCreds()
        }
    }

    // db changes should not run on mainScope, this is why we run it in viewModelScope independently
    fun addEntity(demoEntity: DemoEntity) {
        viewModelScope.launch {
            demoRepository.insertDemoEntity(demoEntity)
        }
    }

    fun addEntity(userEntity: UserEntity) {
        viewModelScope.launch {
            userRepository.insertOne(userEntity)
        }
    }

    suspend fun count() = userRepository.count()

    suspend fun getUserForLogin(login: String): UserEntity? {
        val users = userRepository.getEntityByLogin(login)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    suspend fun permission(name: String): String {
        return configRepository.getPermissionValue(name)
    }

    fun getUserForCard(card: String, mainActivity: MainActivity) {
        viewModelScope.launch {
            val users = userRepository.getEntityByCard(card)
            if (users.isNotEmpty()) {
                mainActivity.showSettings(users[0])
            }
        }
    }


}

