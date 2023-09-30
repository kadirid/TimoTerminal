package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.UserDAO
import com.timo.timoterminal.entityClasses.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDAO: UserDAO) {

    val getAllEntities : Flow<List<UserEntity>> = userDAO.getAll()

    @WorkerThread
    suspend fun insertUserEntity(entity: List<UserEntity>) {
        userDAO.insertAll(entity)
    }

    suspend fun count() = userDAO.count()

    suspend fun getAllAsList() = userDAO.getAllAsList()

    suspend fun insertOne(user : UserEntity) = userDAO.insertOne(user)

    suspend fun delete(user: UserEntity) = userDAO.delete(user)

    suspend fun getEntity(id: Long) = userDAO.loadEntityById(id)
}
