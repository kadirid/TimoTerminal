package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.ActivityTypeDAO
import com.timo.timoterminal.entityClasses.ActivityTypeEntity

class ActivityTypeRepository (private val activityTypeDAO: ActivityTypeDAO) {

    fun getAllActivityTypesFlow() = activityTypeDAO.getAllActivityTypesFlow()

    suspend fun getAllActivityTypes() = activityTypeDAO.getAllActivityTypes()

    suspend fun getActivityTypeById(id: Long) = activityTypeDAO.getActivityTypeById(id)

    suspend fun countActivityTypes() = activityTypeDAO.countActivityTypes()

    @WorkerThread
    suspend fun insertActivityType(activityType: ActivityTypeEntity): Long {
        return activityTypeDAO.insertActivityType(activityType)
    }

    @WorkerThread
    suspend fun insertAllActivityTypes(activityTypes: List<ActivityTypeEntity>) {
        activityTypeDAO.insertAllActivityTypes(activityTypes)
    }

    suspend fun updateActivityType(activityType: ActivityTypeEntity) {
        activityTypeDAO.updateActivityType(activityType)
    }

    suspend fun deleteActivityType(activityType: ActivityTypeEntity) {
        activityTypeDAO.deleteActivityType(activityType)
    }

    suspend fun deleteAllActivityTypes() {
        activityTypeDAO.deleteAllActivityTypes()
    }
}