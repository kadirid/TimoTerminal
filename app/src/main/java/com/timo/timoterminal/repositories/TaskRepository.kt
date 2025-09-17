package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.TaskDAO
import com.timo.timoterminal.entityClasses.TaskEntity

class TaskRepository(private val taskDAO: TaskDAO) {

    fun getAllTasksFlow() = taskDAO.getAllTasksFlow()

    suspend fun getAllTasks() = taskDAO.getAllTasks()

    suspend fun getTaskById(id: Long) = taskDAO.getTaskById(id)

    suspend fun countTasks() = taskDAO.countTasks()

    @WorkerThread
    suspend fun insertTask(task: TaskEntity): Long {
        return taskDAO.insertTask(task)
    }

    @WorkerThread
    suspend fun insertAllTasks(tasks: List<com.timo.timoterminal.entityClasses.TaskEntity>) {
        taskDAO.insertAllTasks(tasks)
    }

    suspend fun updateTask(task: com.timo.timoterminal.entityClasses.TaskEntity) {
        taskDAO.updateTask(task)
    }

    suspend fun deleteTask(task: com.timo.timoterminal.entityClasses.TaskEntity) {
        taskDAO.deleteTask(task)
    }

    suspend fun deleteAllTasks() {
        taskDAO.deleteAllTasks()
    }
}