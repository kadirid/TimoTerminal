package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.ProjectTimeDAO
import com.timo.timoterminal.entityClasses.ProjectTimeEntity

class ProjectTimeRepository(private val projectTimeDAO: ProjectTimeDAO) {

    suspend fun getNextNotSend() = projectTimeDAO.getNextNotSend()

    suspend fun getAllAsList() = projectTimeDAO.getAllAsList()

    @WorkerThread
    suspend fun insert(entity: ProjectTimeEntity) = projectTimeDAO.insert(entity)

    suspend fun setIsSend(id: Long, isSend: Boolean = true) = projectTimeDAO.setIsSend(id, isSend)

    suspend fun deleteOldProjectTimes(isSend: Boolean = true) =
        projectTimeDAO.deleteOldProjectTimes(isSend)

    suspend fun deleteAll() = projectTimeDAO.deleteAll()

    suspend fun getPageAsList(currentPage: Int) = projectTimeDAO.getPageAsList(currentPage * 100)

    suspend fun count(): Int = projectTimeDAO.count()

    suspend fun deleteById(id: Long) = projectTimeDAO.deleteById(id)

    suspend fun getById(id: Long) = projectTimeDAO.getById(id)
}