package com.timo.timoterminal.repositories

import com.timo.timoterminal.dao.AbsenceDeputyDAO
import com.timo.timoterminal.entityClasses.AbsenceDeputyEntity

class AbsenceDeputyRepository(private val absenceDeputyDAO: AbsenceDeputyDAO) {

    suspend fun insertAll(entities: List<AbsenceDeputyEntity>) =
        absenceDeputyDAO.insertAll(entities)

    suspend fun getByUserId(userId: Int) = absenceDeputyDAO.getByUserId(userId)

    suspend fun getAll(): List<AbsenceDeputyEntity> = absenceDeputyDAO.getAll()
}