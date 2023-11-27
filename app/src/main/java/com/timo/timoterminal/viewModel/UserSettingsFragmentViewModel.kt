package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.fragmentViews.UserSettingsFragment
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.UserService
import kotlinx.coroutines.launch

class UserSettingsFragmentViewModel(
    private val userRepository: UserRepository,
    private val userService: UserService
): ViewModel() {

    suspend fun getAllAsList() = userRepository.getAllAsList()
    fun getAll() = userRepository.getAllEntities

    fun addEntity(userEntity: UserEntity) {
        viewModelScope.launch {
            userRepository.insertOne(userEntity)
        }
    }

    fun saveOrUpdate(user: UserEntity) {
        viewModelScope.launch {
            userRepository.insertOne(user)
        }
    }

    fun loadUserFromServer() {
        //Get User from Server
        userService.loadUserFromServer(viewModelScope)
    }

    fun deleteEntity(user: UserEntity){
        viewModelScope.launch {
            userRepository.delete(user)
        }
    }

    fun updatePin(paramMap: HashMap<String,String>, fragment: UserSettingsFragment){
        userService.sendUpdateRequestFragment(paramMap,fragment,viewModelScope)
    }
}