package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.LanguageDAO
import com.timo.timoterminal.entityClasses.LanguageEntity
import kotlinx.coroutines.flow.Flow

class LanguageRepository(private val languageDAO: LanguageDAO) {

    val getAllEntities: Flow<List<LanguageEntity>> = languageDAO.getAll()

    @WorkerThread
    suspend fun insertLanguageEntities(entities: List<LanguageEntity>) {
        languageDAO.insertAll(*entities.toTypedArray())
    }

    suspend fun getAllAsList(): List<LanguageEntity> = languageDAO.getAllAsList()

    suspend fun getAllAsListByLanguage(lang: String): List<LanguageEntity> =
        languageDAO.getAllAsListByLanguage(lang)

    suspend fun delete(entity: LanguageEntity) = languageDAO.delete(entity)

    suspend fun deleteAll() = languageDAO.deleteAll()

}