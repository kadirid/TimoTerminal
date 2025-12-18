package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity

@Dao
interface AbsenceEntryDAO {

    @Query("INSERT INTO AbsenceEntryEntity (user_id, absence_type_id, date, date_to, `from`, `to`," +
            " hours, description, deputy, matrix, half_day, full_day, is_send, editor_id, is_visible," +
            " wt_id, message) VALUES" +
            " (:userId, :absenceTypeId, :date, :dateTo, :from, :to," +
            " :hours, :description, :deputy, :matrix, :halfDay, :fullDay, :isSend, :editorId, :isVisible," +
            " :wtId, :message)")
    suspend fun insertAbsenceEntry(
        userId: String,
        absenceTypeId: String,
        date: String,
        dateTo: String,
        from: String,
        to: String,
        hours: String,
        description: String,
        deputy: String,
        matrix: String,
        halfDay: String,
        fullDay: String,
        isSend: Boolean,
        editorId: String,
        isVisible: Boolean,
        wtId: Long?,
        message: String
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = AbsenceEntryEntity::class)
    suspend fun insert(entity: AbsenceEntryEntity)

    @Query("DELETE FROM AbsenceEntryEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM AbsenceEntryEntity WHERE is_visible = 1 ORDER BY id Limit :currentPage, 100")
    suspend fun getPageAsList(currentPage: Int): List<AbsenceEntryEntity>

    @Query("SELECT * FROM AbsenceEntryEntity WHERE user_id = :userId AND is_visible = 1 AND is_send = 1 ORDER BY id Limit :currentPage, 100")
    suspend fun getPageAsList(currentPage: Int, userId: String): List<AbsenceEntryEntity>

    @Query("SELECT COUNT(*) FROM AbsenceEntryEntity WHERE user_id = :userId AND is_visible = 1 AND is_send = 1")
    suspend fun getCountOfFailedForUser(userId: String): Int

    @Query("UPDATE AbsenceEntryEntity SET is_send = :isSend, message = :message WHERE id = :id")
    suspend fun setIsSend(id: Long, isSend: Boolean, message: String): Int

    @Query("DELETE FROM AbsenceEntryEntity WHERE is_send = :isSend AND DATETIME(createdTime) < DATETIME('now','start of day', '-1 month','-1 day')")
    suspend fun deleteOldAbsenceEntries(isSend: Boolean = true)

    @Query("SELECT * FROM AbsenceEntryEntity WHERE is_send = 0 AND is_visible = 1 ORDER BY id LIMIT 1")
    suspend fun getNextNotSend(): AbsenceEntryEntity?

    @Query("SELECT * FROM AbsenceEntryEntity WHERE hours = '' AND `to` = '' AND full_day = '' AND user_id = :userId ORDER BY id DESC LIMIT 1")
    suspend fun getLatestOpenByUserId(userId: String): AbsenceEntryEntity?

    @Query("DELETE FROM AbsenceEntryEntity WHERE wt_id = :wtId")
    suspend fun deleteByWtId(wtId: Long): Int

    @Query("DELETE FROM AbsenceEntryEntity")
    suspend fun deleteAll()
}