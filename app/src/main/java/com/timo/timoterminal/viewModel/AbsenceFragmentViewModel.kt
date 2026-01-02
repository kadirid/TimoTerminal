package com.timo.timoterminal.viewModel

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.AbsenceDeputyEntity
import com.timo.timoterminal.entityClasses.AbsenceTypeEntity
import com.timo.timoterminal.entityClasses.AbsenceTypeMatrixEntity
import com.timo.timoterminal.entityClasses.AbsenceTypeRightEntity
import com.timo.timoterminal.repositories.AbsenceDeputyRepository
import com.timo.timoterminal.repositories.AbsenceTypeMatrixRepository
import com.timo.timoterminal.repositories.AbsenceTypeRepository
import com.timo.timoterminal.repositories.AbsenceTypeRightRepository
import com.timo.timoterminal.repositories.AbsenceTypeFavoriteRepository
import com.timo.timoterminal.service.AbsenceService
import kotlinx.coroutines.launch
import org.json.JSONObject

class AbsenceFragmentViewModel(
    private val absenceService: AbsenceService,
    private val absenceTypeRepository: AbsenceTypeRepository,
    private val absenceTypeMatrixRepository: AbsenceTypeMatrixRepository,
    private val absenceTypeRightRepository: AbsenceTypeRightRepository,
    private val absenceDeputyRepository: AbsenceDeputyRepository,
    private val absenceTypeFavoriteRepository: AbsenceTypeFavoriteRepository
) : ViewModel() {

    // UI LiveData
    val liveHideMask = MutableLiveData<Boolean>()
    val liveShowMask = MutableLiveData<Boolean>()
    val liveShowOfflineNotice = MutableLiveData<Boolean>()
    val liveAbsenceTypes = MutableLiveData<List<AbsenceTypeEntity>>()
    val liveFavoriteAbsenceTypeIds = MutableLiveData<Set<Long>>()
    // derive favorite entities from liveAbsenceTypes + favorite ids so we keep a single source of truth
    val liveFavoriteAbsenceEntities = MediatorLiveData<List<AbsenceTypeEntity>>()
    val liveLatestOpen = MutableLiveData<JSONObject>()

    // internal state
    var userId: Long = -1L
    private var repoLoad: Long = 0L

    private var absenceTypeEntities: List<AbsenceTypeEntity> = emptyList()
    private var absenceTypeMatrixEntities: List<AbsenceTypeMatrixEntity> = emptyList()
    private var absenceTypeRightEntities: List<AbsenceTypeRightEntity> = emptyList()
    private var absenceDeputyEntities = emptyList<AbsenceDeputyEntity>()

    init {
        // recompute favorites whenever visible list or favorite ids change
        fun recompute() {
            val types = liveAbsenceTypes.value ?: emptyList()
            val ids = liveFavoriteAbsenceTypeIds.value ?: emptySet()
            // use postValue for thread-safety (might be called from background)
            liveFavoriteAbsenceEntities.postValue(types.filter { ids.contains(it.id) })
        }
        liveFavoriteAbsenceEntities.addSource(liveAbsenceTypes) { recompute() }
        liveFavoriteAbsenceEntities.addSource(liveFavoriteAbsenceTypeIds) { recompute() }
    }

    // --- Public API ---------------------------------------------------------

    fun loadForAbsence() {
        liveShowMask.postValue(true)
        repoLoad = 0L

        absenceService.getValuesForAbsence(userId) { success, obj ->
            if (success) {
                // parse server response into local entities
                absenceTypeEntities = AbsenceTypeEntity.parseFromJson(obj!!)
                absenceTypeMatrixEntities = AbsenceTypeMatrixEntity.parseFromJson(obj)
                absenceTypeRightEntities = AbsenceTypeRightEntity.parseFromJson(obj)
                absenceDeputyEntities = AbsenceDeputyEntity.parseFromJson(obj)

                val latestOpen = obj.getJSONObject("latestOpenAbsenceTime")

                // filter absence types by user rights and publish them
                val visible = filterByUserRights(absenceTypeEntities)
                if (!latestOpen.has("wtId")) {
                    liveAbsenceTypes.postValue(visible)

                    // load favorites asynchronously (refreshFavorites now only posts ids)
                    viewModelScope.launch {
                        refreshFavorites()
                    }
                }

                // persist all fetched entities into local DB (fire-and-forget)
                val tasks = listOf<suspend () -> Unit>(
                    { absenceTypeRepository.insertAll(absenceTypeEntities) },
                    { absenceTypeMatrixRepository.insertAll(absenceTypeMatrixEntities) },
                    { absenceTypeRightRepository.insertAll(absenceTypeRightEntities) },
                    { absenceDeputyRepository.insertAll(absenceDeputyEntities) }
                )
                tasks.forEach { task -> viewModelScope.launch { task() } }

                if (latestOpen.has("wtId")) liveLatestOpen.postValue(latestOpen)
                liveHideMask.postValue(true)
            } else {
                // offline: load from local repositories
                liveHideMask.postValue(true)

                viewModelScope.launch {
                    val lastOpen = absenceService.getLatestOpenByUserId(userId.toString())
                    if (lastOpen != null) {
                        liveLatestOpen.postValue(lastOpen.toJsonObject())
                    } else {
                        liveShowOfflineNotice.postValue(true)
                        onRepoLoaded()
                    }
                }

                // load from local DBs
                viewModelScope.launch {
                    absenceTypeEntities = absenceTypeRepository.getAll()
                    onRepoLoaded()
                }
                viewModelScope.launch {
                    absenceTypeRightEntities = absenceTypeRightRepository.getAll()
                    onRepoLoaded()
                }
            }
        }
    }

    // Call when local repositories finished loading (used in offline path)
    private fun onRepoLoaded() {
        repoLoad++
        if (repoLoad == 3L) {
            liveHideMask.postValue(true)

            val visible = filterByUserRights(absenceTypeEntities)
            liveAbsenceTypes.postValue(visible)

            // load favorites from local DB and compute favorite entities for visible list
            viewModelScope.launch {
                refreshFavorites()
            }
        }
    }

    suspend fun hasUserErrorAbsenceEntries(): Boolean {
        val count = absenceService.getCountOfFailedForUser(userId.toString())
        return count > 0
    }

    /** Toggle favorite for a given absence type (persist and refresh favorites LiveData). */
    fun setMarkedAsFavorite(absenceTypeId: Long, marked: Boolean) {
        viewModelScope.launch {
            persistFavoriteChange(absenceTypeId, marked)
            // after persisting, refresh favorite ids (entities are derived)
            refreshFavorites()
        }
    }

    // --- Helpers ------------------------------------------------------------

    // Return set of absence type ids that the current user has rights to
    private fun getUserRightsSet(): Set<Long> = absenceTypeRightEntities
        .filter { it.userId == userId }
        .map { it.absenceTypeId }
        .toSet()

    // Filter a list of AbsenceTypeEntity by current user's rights
    private fun filterByUserRights(list: List<AbsenceTypeEntity>): List<AbsenceTypeEntity> {
        val rights = getUserRightsSet()
        return list.filter { rights.contains(it.id) }
    }

    // Refresh favorite ids based on the provided visible list
    private suspend fun refreshFavorites() {
        try {
            val favIds = absenceTypeFavoriteRepository.getFavoritesForUser(userId).toSet()
            liveFavoriteAbsenceTypeIds.postValue(favIds)
        } catch (e: Exception) {
            Log.e("AbsenceViewModel", "Failed to refresh favorites", e)
        }
    }

    // Persist favorite add/remove into DB
    private suspend fun persistFavoriteChange(absenceTypeId: Long, marked: Boolean) {
        try {
            if (marked) absenceTypeFavoriteRepository.addFavorite(userId, absenceTypeId)
            else absenceTypeFavoriteRepository.removeFavorite(userId, absenceTypeId)
        } catch (e: Exception) {
            Log.e("AbsenceViewModel", "Failed to set favorite state", e)
        }
    }
}