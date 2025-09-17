package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.Customer2ProjectDAO
import com.timo.timoterminal.entityClasses.Customer2ProjectEntity

class Customer2ProjectRepository (private val customer2ProjectDAO: Customer2ProjectDAO) {

    fun getAllCustomer2ProjectsFlow() = customer2ProjectDAO.getAllCustomer2ProjectsFlow()

    suspend fun getAllCustomer2Projects() = customer2ProjectDAO.getAllCustomer2Projects()

    suspend fun getCustomer2ProjectById(id: Long) = customer2ProjectDAO.getCustomer2ProjectById(id)

    suspend fun countCustomer2Projects() = customer2ProjectDAO.countCustomer2Projects()

    @WorkerThread
    suspend fun insertCustomer2Project(customer2Project: Customer2ProjectEntity): Long {
        return customer2ProjectDAO.insertCustomer2Project(customer2Project)
    }

    @WorkerThread
    suspend fun insertAllCustomer2Projects(customer2Projects: List<Customer2ProjectEntity>) {
        customer2ProjectDAO.insertAllCustomer2Projects(customer2Projects)
    }

    suspend fun updateCustomer2Project(customer2Project: Customer2ProjectEntity) {
        customer2ProjectDAO.updateCustomer2Project(customer2Project)
    }

    suspend fun deleteCustomer2Project(customer2Project: Customer2ProjectEntity) {
        customer2ProjectDAO.deleteCustomer2Project(customer2Project)
    }

    suspend fun deleteAllCustomer2Projects() {
        customer2ProjectDAO.deleteAllCustomer2Projects()
    }
}