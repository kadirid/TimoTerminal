package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.AbsenceTypeMatrixDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeMatrixEntity

class AbsenceTypeMatrixRepository(private val absenceTypeMatrixDAO: AbsenceTypeMatrixDAO) {

    @WorkerThread
    suspend fun insertAll(entities: List<AbsenceTypeMatrixEntity>) =
        absenceTypeMatrixDAO.insertAll(entities)

    suspend fun getByAbsenceTypeId(absenceTypeId: Int): List<AbsenceTypeMatrixEntity> =
        absenceTypeMatrixDAO.getByAbsenceTypeId(absenceTypeId)

    suspend fun getAll(): List<AbsenceTypeMatrixEntity> = absenceTypeMatrixDAO.getAll()
}