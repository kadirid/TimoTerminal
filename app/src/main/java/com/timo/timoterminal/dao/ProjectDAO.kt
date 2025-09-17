package com.timo.timoterminal.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.timo.timoterminal.entityClasses.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDAO {

    @Query("SELECT * FROM ProjectEntity")
    fun getAllProjectsFlow(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM ProjectEntity")
    suspend fun getAllProjects(): List<ProjectEntity>

    @Query("SELECT * FROM ProjectEntity WHERE project_id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT COUNT(*) FROM ProjectEntity")
    suspend fun countProjects(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM ProjectEntity")
    suspend fun deleteAllProjects()
}