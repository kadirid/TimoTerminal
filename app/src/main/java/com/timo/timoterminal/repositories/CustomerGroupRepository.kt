package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.CustomerGroupDAO
import com.timo.timoterminal.entityClasses.CustomerGroupEntity

class CustomerGroupRepository(private val customerGroupDAO: CustomerGroupDAO) {

    fun getAllCustomerGroupsFlow() = customerGroupDAO.getAllCustomerGroupsFlow()

    suspend fun getAllCustomerGroups() = customerGroupDAO.getAllCustomerGroups()

    suspend fun getCustomerGroupById(id: Long) = customerGroupDAO.getCustomerGroupById(id)

    suspend fun countCustomerGroups() = customerGroupDAO.countCustomerGroups()

    @WorkerThread
    suspend fun insertCustomerGroup(customerGroup: CustomerGroupEntity): Long {
        return customerGroupDAO.insertCustomerGroup(customerGroup)
    }

    @WorkerThread
    suspend fun insertAllCustomerGroups(customerGroups: List<CustomerGroupEntity>) {
        customerGroupDAO.insertAllCustomerGroups(customerGroups)
    }

    suspend fun updateCustomerGroup(customerGroup: CustomerGroupEntity) {
        customerGroupDAO.updateCustomerGroup(customerGroup)
    }

    suspend fun deleteCustomerGroup(customerGroup: CustomerGroupEntity) {
        customerGroupDAO.deleteCustomerGroup(customerGroup)
    }

    suspend fun deleteAllCustomerGroups() {
        customerGroupDAO.deleteAllCustomerGroups()
    }
}