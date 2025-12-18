package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.ProjectTimeEntity

@Dao
interface ProjectTimeDAO {

    @Query("SELECT * FROM ProjectTimeEntity WHERE isSend = 0 AND isVisible = 1 ORDER BY id LIMIT 1")
    suspend fun getNextNotSend(): ProjectTimeEntity?

    @Query("SELECT * FROM ProjectTimeEntity ORDER BY id")
    suspend fun getAllAsList(): List<ProjectTimeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = ProjectTimeEntity::class)
    suspend fun insert(entity: ProjectTimeEntity)

    @Query("""
        INSERT INTO ProjectTimeEntity (
            userId, date, dateTo, `from`, `to`, hours, manDays, description, customerId, ticketId,
            projectId, taskId, orderNo, activityType, activityTypeMatrix, skillLevel,
            performanceLocation, teamId, journeyId, travelTime, drivenKm, kmFlatRate, billable,
            premium, units, evaluation, isSend, timeEntryType, isVisible, wtId, editorId, message
        ) VALUES (
            :userId, :date, :dateTo, :from, :to, :hours, :manDays, :description, :customerId,
            :ticketId, :projectId, :taskId, :orderNo, :activityType, :activityTypeMatrix,
            :skillLevel, :performanceLocation, :teamId, :journeyId, :travelTime, :drivenKm,
            :kmFlatRate, :billable, :premium, :units, :evaluation, :isSend, :timeEntryType,
            :isVisible, :wtId, :editorId, :message
        )
    """)
    suspend fun insertWithoutCreatedTime(
        userId: String, date: String, dateTo: String, from: String, to: String, hours: String,
        manDays: String, description: String, customerId: String, ticketId: String,
        projectId: String, taskId: String, orderNo: String, activityType: String,
        activityTypeMatrix: String, skillLevel: String, performanceLocation: String, teamId: String,
        journeyId: String, travelTime: String, drivenKm: String, kmFlatRate: String,
        billable: String, premium: String, units: String, evaluation: String, isSend: Boolean,
        timeEntryType: String, isVisible: Boolean, wtId: String, editorId: String, message: String
    )

    @Query("UPDATE ProjectTimeEntity SET isSend = :isSend, message = :message WHERE id = :id")
    suspend fun setIsSend(id: Long, isSend: Boolean, message: String): Int

    @Query("DELETE FROM ProjectTimeEntity WHERE isSend = :isSend AND DATETIME(createdTime) < DATETIME('now','start of day', '-1 month','-1 day')")
    suspend fun deleteOldProjectTimes(isSend: Boolean = true)

    @Query("DELETE FROM ProjectTimeEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM ProjectTimeEntity WHERE isVisible = 1 ORDER BY id Limit :currentPage, 100")
    suspend fun getPageAsList(currentPage: Int): List<ProjectTimeEntity>

    @Query("SELECT * FROM ProjectTimeEntity WHERE userId = :userId AND isVisible = 1 AND isSend = 1 ORDER BY id Limit :currentPage, 100")
    suspend fun getPageAsList(currentPage: Int, userId: String): List<ProjectTimeEntity>

    @Query("SELECT COUNT(*) FROM ProjectTimeEntity WHERE userId = :userId AND isVisible = 1 AND isSend = 1")
    suspend fun getCountOfFailedForUser(userId: String): Int

    @Query("SELECT COUNT(*) FROM ProjectTimeEntity")
    suspend fun count(): Int

    @Query("DELETE FROM ProjectTimeEntity WHERE id = :id" )
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM ProjectTimeEntity WHERE id = :id")
    suspend fun getById(id: Long): ProjectTimeEntity?

    @Query("SELECT * FROM ProjectTimeEntity WHERE userId = :userId AND hours = '' AND manDays = '' AND `to` = '' ORDER BY createdTime DESC LIMIT 1")
    suspend fun getLatestOpenByUserId(userId: String): ProjectTimeEntity?

    @Query("DELETE FROM ProjectTimeEntity WHERE wtId = :wtId")
    suspend fun deleteByWtId(wtId: Long): Int
}