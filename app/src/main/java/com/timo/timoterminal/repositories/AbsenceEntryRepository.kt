package com.timo.timoterminal.repositories

import com.timo.timoterminal.dao.AbsenceEntryDAO
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity

class AbsenceEntryRepository(private val absenceEntryDAO: AbsenceEntryDAO) {

    suspend fun insert(entity: AbsenceEntryEntity) = absenceEntryDAO.insert(entity)

    suspend fun insertAbsenceEntry(entity: AbsenceEntryEntity) {
        absenceEntryDAO.insertAbsenceEntry(
            entity.userId,
            entity.absenceTypeId,
            entity.date,
            entity.dateTo,
            entity.from,
            entity.to,
            entity.hours,
            entity.description,
            entity.deputy,
            entity.matrix,
            entity.halfDay,
            entity.fullDay,
            entity.isSend,
            entity.editorId,
            entity.isVisible,
            entity.wtId,
            entity.message
        )
    }

    suspend fun deleteById(id: Long) = absenceEntryDAO.deleteById(id)

    suspend fun getPageAsList(currentPage: Int, userId: String?) =
        if (userId != null) {
            absenceEntryDAO.getPageAsList(currentPage * 100, userId)
        } else {
            absenceEntryDAO.getPageAsList(currentPage * 100)
        }

    suspend fun getCountOfFailedForUser(userId: String): Int =
        absenceEntryDAO.getCountOfFailedForUser(userId)

    suspend fun setIsSend(id: Long, isSend: Boolean = true, message: String) =
        absenceEntryDAO.setIsSend(id, isSend, message)

    suspend fun deleteOldAbsenceEntries(isSend: Boolean = true) =
        absenceEntryDAO.deleteOldAbsenceEntries(isSend)

    suspend fun getNextNotSend() = absenceEntryDAO.getNextNotSend()

    suspend fun getLatestOpenByUserId(userId: String) = absenceEntryDAO.getLatestOpenByUserId(userId)

    suspend fun deleteByWtId(wtId: Long): Int = absenceEntryDAO.deleteByWtId(wtId)

    suspend fun deleteAll() = absenceEntryDAO.deleteAll()
}