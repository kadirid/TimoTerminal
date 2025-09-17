package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.SkillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkillDAO {

    @Query("SELECT * FROM SkillEntity")
    fun getAllSkillsFlow(): Flow<List<SkillEntity>>

    @Query("SELECT * FROM SkillEntity")
    suspend fun getAllSkills(): List<SkillEntity>

    @Query("SELECT * FROM SkillEntity WHERE skill_id = :id")
    suspend fun getSkillById(id: Long): SkillEntity?

    @Query("SELECT COUNT(*) FROM SkillEntity")
    suspend fun countSkills(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkill(skill: SkillEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSkills(skills: List<SkillEntity>)

    @Update
    suspend fun updateSkill(skill: SkillEntity)

    @Delete
    suspend fun deleteSkill(skill: SkillEntity)

    @Query("DELETE FROM SkillEntity")
    suspend fun deleteAllSkills()
}