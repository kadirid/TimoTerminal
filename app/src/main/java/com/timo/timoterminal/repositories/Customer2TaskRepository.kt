package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.Customer2TaskDAO
import com.timo.timoterminal.entityClasses.Customer2TaskEntity

class Customer2TaskRepository ( private val customer2TaskDAO: Customer2TaskDAO) {

    fun getAllCustomer2TasksFlow() = customer2TaskDAO.getAllCustomer2TasksFlow()

    suspend fun getAllCustomer2Tasks() = customer2TaskDAO.getAllCustomer2Tasks()

    suspend fun getCustomer2TaskById(id: Long) = customer2TaskDAO.getCustomer2TaskById(id)

    suspend fun countCustomer2Tasks() = customer2TaskDAO.countCustomer2Tasks()

    @WorkerThread
    suspend fun insertCustomer2Task(customer2Task: Customer2TaskEntity): Long {
        return customer2TaskDAO.insertCustomer2Task(customer2Task)
    }

    @WorkerThread
    suspend fun insertAllCustomer2Tasks(customer2Tasks: List<Customer2TaskEntity>) {
        customer2TaskDAO.insertAllCustomer2Tasks(customer2Tasks)
    }

    suspend fun updateCustomer2Task(customer2Task: Customer2TaskEntity) {
        customer2TaskDAO.updateCustomer2Task(customer2Task)
    }

    suspend fun deleteCustomer2Task(customer2Task: Customer2TaskEntity) {
        customer2TaskDAO.deleteCustomer2Task(customer2Task)
    }

    suspend fun deleteAllCustomer2Tasks() {
        customer2TaskDAO.deleteAllCustomer2Tasks()
    }
}