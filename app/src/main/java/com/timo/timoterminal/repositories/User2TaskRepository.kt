package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.User2TaskDAO
import com.timo.timoterminal.entityClasses.User2TaskEntity

class User2TaskRepository (private val user2TaskDAO: User2TaskDAO) {

    fun getAllUser2TasksFlow() = user2TaskDAO.getAllUser2TasksFlow()

    suspend fun getAllUser2Tasks() = user2TaskDAO.getAllUser2TAsks()

    suspend fun getUser2TaskById(id: Long) = user2TaskDAO.getUser2TaskById(id)

    suspend fun countUser2Tasks() = user2TaskDAO.countUser2Tasks()

    @WorkerThread
    suspend fun insertUser2Task(user2Task: User2TaskEntity): Long {
        return user2TaskDAO.insertUser2Task(user2Task)
    }

    @WorkerThread
    suspend fun insertAllUser2Tasks(user2Tasks: List<User2TaskEntity>) {
        user2TaskDAO.insertAllUser2Tasks(user2Tasks)
    }

    suspend fun updateUser2Task(user2Task: User2TaskEntity) {
        user2TaskDAO.updateUser2Task(user2Task)
    }

    suspend fun deleteUser2Task(user2Task: User2TaskEntity) {
        user2TaskDAO.deleteUser2Task(user2Task)
    }

    suspend fun deleteAllUser2Tasks() {
        user2TaskDAO.deleteAllUser2Tasks()
    }
}