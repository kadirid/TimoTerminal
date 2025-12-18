package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.AbsenceTypeRightDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeRightEntity

class AbsenceTypeRightRepository(private val absenceTypeRightDAO: AbsenceTypeRightDAO) {

    @WorkerThread
    suspend fun insertAll(entities: List<AbsenceTypeRightEntity>) {
        absenceTypeRightDAO.deleteAll()
        absenceTypeRightDAO.insertAll(entities)
    }

    suspend fun getAll(): List<AbsenceTypeRightEntity> = absenceTypeRightDAO.getAll()

    suspend fun getByAbsenceTypeIdAndUserId(absenceTypeId: Int, userId: Int): AbsenceTypeRightEntity? =
        absenceTypeRightDAO.getByAbsenceTypeIdAndUserId(absenceTypeId, userId)
}