package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.AbsenceTypeDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeEntity

class AbsenceTypeRepository(private val absenceTypeDAO: AbsenceTypeDAO) {

    @WorkerThread
    suspend fun insertAll(entities: List<AbsenceTypeEntity>) =
        absenceTypeDAO.insertAll(entities)

    suspend fun getById(id: Int): AbsenceTypeEntity? = absenceTypeDAO.getById(id)

    suspend fun getAll(): List<AbsenceTypeEntity> = absenceTypeDAO.getAll()
}