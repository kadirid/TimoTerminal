package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.CustomerDAO
import com.timo.timoterminal.entityClasses.CustomerEntity

class CustomerRepository(private val customerDAO: CustomerDAO) {

    fun getAllCustomersFlow() = customerDAO.getAllCustomersFlow()

    suspend fun getAllCustomers() = customerDAO.getAllCustomers()

    suspend fun getCustomerById(id: Long) = customerDAO.getCustomerById(id)

    suspend fun countCustomers() = customerDAO.countCustomers()

    @WorkerThread
    suspend fun insertCustomer(customer: CustomerEntity): Long {
        return customerDAO.insertCustomer(customer)
    }

    @WorkerThread
    suspend fun insertAllCustomers(customers: List<CustomerEntity>) {
        customerDAO.insertAllCustomers(customers)
    }

    suspend fun updateCustomer(customer: CustomerEntity) {
        customerDAO.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: CustomerEntity) {
        customerDAO.deleteCustomer(customer)
    }

    suspend fun deleteAllCustomers() {
        customerDAO.deleteAllCustomers()
    }
}