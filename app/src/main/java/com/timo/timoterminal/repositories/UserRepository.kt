package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.UserDAO
import com.timo.timoterminal.entityClasses.UserEntity

class UserRepository(private val userDAO: UserDAO) {

    @WorkerThread
    suspend fun insertUserEntity(entity: List<UserEntity>) {
        userDAO.insertAll(entity)
    }

    suspend fun count() = userDAO.count()

    suspend fun getAllAsList() = userDAO.getAllAsList()

    suspend fun insertOne(user: UserEntity) = userDAO.insertOne(user)

    suspend fun delete(user: UserEntity) = userDAO.delete(user)

    suspend fun getEntity(id: Long): List<UserEntity> = userDAO.loadEntityById(id)

    suspend fun getEntityByCard(card: String): List<UserEntity> = userDAO.loadEntityByCard(card)

    suspend fun getEntityByLogin(login: String): List<UserEntity> = userDAO.loadEntityByLogin(login)

    suspend fun getEntityByPIN(pin: String): List<UserEntity> = userDAO.loadEntityByPIN(pin)

    suspend fun deleteAll() = userDAO.deleteAll()

    suspend fun getPageAsList(pageNo: Int): List<UserEntity> = userDAO.getPageAsList(pageNo * 50)

    suspend fun updateProjectValues(id: Long, customerBasedProjectTime: Boolean, timeEntryType: Long) =
        userDAO.updateProjectValues(id, customerBasedProjectTime, timeEntryType)

    suspend fun updateCrossDay(id: Long, crossDay: Boolean) = userDAO.updateCrossDay(id, crossDay)
}
