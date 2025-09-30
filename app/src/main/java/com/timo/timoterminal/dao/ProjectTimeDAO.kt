package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.timo.timoterminal.entityClasses.ProjectTimeEntity

@Dao
interface ProjectTimeDAO {

    @Query("SELECT * FROM ProjectTimeEntity WHERE isSend = 0 ORDER BY id LIMIT 1")
    suspend fun getNextNotSend(): ProjectTimeEntity?

    @Query("SELECT * FROM ProjectTimeEntity ORDER BY id")
    suspend fun getAllAsList(): List<ProjectTimeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = ProjectTimeEntity::class)
    suspend fun insert(entity: ProjectTimeEntity)

    @Query("UPDATE ProjectTimeEntity SET isSend = :isSend WHERE id = :id")
    suspend fun setIsSend(id: Long, isSend: Boolean): Int

    @Query("DELETE FROM ProjectTimeEntity WHERE isSend = :isSend AND DATETIME(createdTime) < DATETIME('now','start of day', '-1 month','-1 day')")
    suspend fun deleteOldProjectTimes(isSend: Boolean = true)

    @Query("DELETE FROM ProjectTimeEntity")
    suspend fun deleteAll()

    @Query("SELECT * FROM ProjectTimeEntity ORDER BY id Limit :currentPage, 100")
    suspend fun getPageAsList(currentPage: Int): List<ProjectTimeEntity>

    @Query("SELECT COUNT(*) FROM ProjectTimeEntity")
    suspend fun count(): Int

    @Query("DELETE FROM ProjectTimeEntity WHERE id = :id" )
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM ProjectTimeEntity WHERE id = :id")
    suspend fun getById(id: Long): ProjectTimeEntity?
}