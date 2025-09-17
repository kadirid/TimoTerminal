package com.timo.timoterminal.repositories

import androidx.annotation.WorkerThread
import com.timo.timoterminal.dao.JourneyDAO
import com.timo.timoterminal.entityClasses.JourneyEntity

class JourneyRepository(private val journeyDAO: JourneyDAO) {

    fun getAllJourneysFlow() = journeyDAO.getAllJourneysFlow()

    suspend fun getAllJourneys() = journeyDAO.getAllJourneys()

    suspend fun getJourneyById(id: Long) = journeyDAO.getJourneyById(id)

    suspend fun getJourneysByDateRange(start: Long, end: Long, userId: Int) =
        journeyDAO.getJourneysByDateRange(start, end, userId)

    suspend fun countJourneys() = journeyDAO.countJourneys()

    @WorkerThread
    suspend fun insertJourney(journey: JourneyEntity): Long {
        return journeyDAO.insertJourney(journey)
    }

    @WorkerThread
    suspend fun insertAllJourneys(journeys: List<JourneyEntity>) {
        journeyDAO.insertAllJourneys(journeys)
    }

    suspend fun updateJourney(journey: JourneyEntity) {
        journeyDAO.updateJourney(journey)
    }

    suspend fun deleteJourney(journey: JourneyEntity) {
        journeyDAO.deleteJourney(journey)
    }

    suspend fun deleteAllJourneys() {
        journeyDAO.deleteAllJourneys()
    }
}