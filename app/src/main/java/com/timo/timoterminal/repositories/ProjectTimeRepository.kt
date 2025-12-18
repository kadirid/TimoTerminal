package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.ProjectTimeDAO
import com.timo.timoterminal.entityClasses.ProjectTimeEntity

class ProjectTimeRepository(private val projectTimeDAO: ProjectTimeDAO) {

    suspend fun getNextNotSend() = projectTimeDAO.getNextNotSend()

    suspend fun getAllAsList() = projectTimeDAO.getAllAsList()

    @WorkerThread
    suspend fun insert(entity: ProjectTimeEntity) = projectTimeDAO.insert(entity)

    @WorkerThread
    suspend fun insertWithoutCreatedTime(entity: ProjectTimeEntity) =
        projectTimeDAO.insertWithoutCreatedTime(
            entity.userId,
            entity.date,
            entity.dateTo,
            entity.from,
            entity.to,
            entity.hours,
            entity.manDays,
            entity.description,
            entity.customerId,
            entity.ticketId,
            entity.projectId,
            entity.taskId,
            entity.orderNo,
            entity.activityType,
            entity.activityTypeMatrix,
            entity.skillLevel,
            entity.performanceLocation,
            entity.teamId,
            entity.journeyId,
            entity.travelTime,
            entity.drivenKm,
            entity.kmFlatRate,
            entity.billable,
            entity.premium,
            entity.units,
            entity.evaluation,
            entity.isSend,
            entity.timeEntryType,
            entity.isVisible,
            entity.wtId.toString(),
            entity.editorId,
            entity.message
        )

    suspend fun setIsSend(id: Long, isSend: Boolean = true, message: String) =
        projectTimeDAO.setIsSend(id, isSend, message)

    suspend fun deleteOldProjectTimes(isSend: Boolean = true) =
        projectTimeDAO.deleteOldProjectTimes(isSend)

    suspend fun deleteAll() = projectTimeDAO.deleteAll()

    suspend fun getPageAsList(currentPage: Int, userId: String?) =
        if (userId != null) {
            projectTimeDAO.getPageAsList(currentPage * 100, userId)
        } else {
            projectTimeDAO.getPageAsList(currentPage * 100)
        }

    suspend fun getCountOfFailedForUser(userId: String): Int =
        projectTimeDAO.getCountOfFailedForUser(userId)

    suspend fun count(): Int = projectTimeDAO.count()

    suspend fun deleteById(id: Long) = projectTimeDAO.deleteById(id)

    suspend fun getById(id: Long) = projectTimeDAO.getById(id)

    suspend fun getLatestOpenByUserId(userId: String) = projectTimeDAO.getLatestOpenByUserId(userId)

    suspend fun deleteByWtId(wtId: Long): Int = projectTimeDAO.deleteByWtId(wtId)
}