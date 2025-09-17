package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.ProjectDAO
import com.timo.timoterminal.entityClasses.ProjectEntity

class ProjectRepository(private val projectDAO: ProjectDAO) {

    fun getAllProjectsFlow() = projectDAO.getAllProjectsFlow()

    suspend fun getAllProjects() = projectDAO.getAllProjects()

    suspend fun getProjectById(id: Long) = projectDAO.getProjectById(id)

    @WorkerThread
    suspend fun insertProject(project: ProjectEntity): Long {
        return projectDAO.insertProject(project)
    }

    @WorkerThread
    suspend fun insertAllProjects(projects: List<ProjectEntity>) {
        projectDAO.insertAllProjects(projects)
    }

    suspend fun countProjects() = projectDAO.countProjects()

    suspend fun updateProject(project: ProjectEntity) {
        projectDAO.updateProject(project)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        projectDAO.deleteProject(project)
    }

    suspend fun deleteAllProjects() {
        projectDAO.deleteAllProjects()
    }
}