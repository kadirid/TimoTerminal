package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.ActivityTypeMatrixDAO
import com.timo.timoterminal.entityClasses.ActivityTypeMatrixEntity

class ActivityTypeMatrixRepository(private val activityTypeMatrixDAO: ActivityTypeMatrixDAO) {

    fun getAllActivityTypeMatricesFlow() = activityTypeMatrixDAO.getAllActivityTypeMatricesFlow()

    suspend fun getAllActivityTypeMatrices() = activityTypeMatrixDAO.getAllActivityTypeMatrices()

    suspend fun getActivityTypeMatrixById(id: Long) =
        activityTypeMatrixDAO.getActivityTypeMatrixById(id)

    suspend fun countActivityTypeMatrices() = activityTypeMatrixDAO.countActivityTypeMatrices()

    @WorkerThread
    suspend fun insertActivityTypeMatrix(activityTypeMatrix: ActivityTypeMatrixEntity): Long {
        return activityTypeMatrixDAO.insertActivityTypeMatrix(activityTypeMatrix)
    }

    @WorkerThread
    suspend fun insertAllActivityTypeMatrices(activityTypeMatrices: List<ActivityTypeMatrixEntity>) {
        activityTypeMatrixDAO.insertAllActivityTypeMatrices(activityTypeMatrices)
    }

    suspend fun updateActivityTypeMatrix(activityTypeMatrix: ActivityTypeMatrixEntity) {
        activityTypeMatrixDAO.updateActivityTypeMatrix(activityTypeMatrix)
    }

    suspend fun deleteActivityTypeMatrix(activityTypeMatrix: ActivityTypeMatrixEntity) {
        activityTypeMatrixDAO.deleteActivityTypeMatrix(activityTypeMatrix)
    }

    suspend fun deleteAllActivityTypeMatrices() {
        activityTypeMatrixDAO.deleteAllActivityTypeMatrices()
    }
}