package com.timo.timoterminal.repositories

import com.timo.timoterminal.dao.AbsenceTypeFavoriteDAO
import com.timo.timoterminal.entityClasses.AbsenceTypeFavoriteEntity

class AbsenceTypeFavoriteRepository(private val favoriteDAO: AbsenceTypeFavoriteDAO) {

    suspend fun addFavorite(userId: Long, absenceTypeId: Long) {
        favoriteDAO.insert(AbsenceTypeFavoriteEntity(userId = userId, absenceTypeId = absenceTypeId))
    }

    suspend fun removeFavorite(userId: Long, absenceTypeId: Long) {
        favoriteDAO.deleteForUser(userId, absenceTypeId)
    }

    suspend fun getFavoritesForUser(userId: Long): List<Long> = favoriteDAO.getFavoritesForUser(userId)

    suspend fun isFavorite(userId: Long, absenceTypeId: Long): Boolean = favoriteDAO.isFavorite(userId, absenceTypeId) > 0
}

