package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.repositories.UserRepository
import kotlinx.coroutines.launch

class UserSettingsFragmentViewModel(private val userRepository: UserRepository): ViewModel() {

    fun getAllUserEntities() = userRepository.getAllAsList()

    fun addEntity(userEntity: UserEntity) {
        viewModelScope.launch {
            userRepository.insertUserEntity(userEntity)
        }
    }

    fun saveOrUpdate(user: UserEntity) {
        viewModelScope.launch {
            if(user.id == null){
                userRepository.insertOne(user)
            }else{
                userRepository.insertUserEntity(user)
            }
        }
    }

    fun deleteEntity(user: UserEntity){
        viewModelScope.launch {
            userRepository.delete(user)
        }
    }
}