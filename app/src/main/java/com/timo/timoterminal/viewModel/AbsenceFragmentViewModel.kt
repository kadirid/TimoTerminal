package com.timo.timoterminal.viewModel

import android.util.Log
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
import com.timo.timoterminal.service.AbsenceService
import kotlinx.coroutines.launch
import org.json.JSONObject

class AbsenceFragmentViewModel(
    private val absenceService: AbsenceService,
    private val absenceTypeRepository: AbsenceTypeRepository,
    private val absenceTypeMatrixRepository: AbsenceTypeMatrixRepository,
    private val absenceTypeRightRepository: AbsenceTypeRightRepository,
    private val absenceDeputyRepository: AbsenceDeputyRepository
): ViewModel() {
    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowOfflineNotice: MutableLiveData<Boolean> = MutableLiveData()
    val liveAbsenceTypes: MutableLiveData<List<AbsenceTypeEntity>> = MutableLiveData()
    val liveLatestOpen: MutableLiveData<JSONObject> = MutableLiveData()

    var userId: Long = -1L
    private var repoLoad: Long = 0L

    private var absenceTypeEntities: List<AbsenceTypeEntity> = emptyList()
    private var absenceTypeMatrixEntities: List<AbsenceTypeMatrixEntity> = emptyList()
    private var absenceTypeRightEntities: List<AbsenceTypeRightEntity> = emptyList()
    private var absenceDeputyEntities = emptyList<AbsenceDeputyEntity>()

    fun loadForAbsence() {
        liveShowMask.postValue(true)
        repoLoad = 0L
        absenceService.getValuesForAbsence(userId) { success, obj ->
            if (success) {
                absenceTypeEntities = AbsenceTypeEntity.parseFromJson(obj!!)
                absenceTypeMatrixEntities = AbsenceTypeMatrixEntity.parseFromJson(obj)
                absenceTypeRightEntities = AbsenceTypeRightEntity.parseFromJson(obj)
                absenceDeputyEntities = AbsenceDeputyEntity.parseFromJson(obj)

                val latestOpen = obj.getJSONObject("latestOpenAbsenceTime")
                val userRights = absenceTypeRightEntities.filter {
                    it.userId == userId
                }.map { it.absenceTypeId }.toSet()
                val filteredAbsence = absenceTypeEntities.filter {
                    userRights.contains(it.id)
                }
                if(!latestOpen.has("wtId")) {
                    liveAbsenceTypes.postValue(filteredAbsence)
                }

                viewModelScope.launch {
                    absenceTypeRepository.insertAll(absenceTypeEntities)
                }
                viewModelScope.launch {
                    absenceTypeMatrixRepository.insertAll(absenceTypeMatrixEntities)
                }
                viewModelScope.launch {
                    absenceTypeRightRepository.insertAll(absenceTypeRightEntities)
                }
                viewModelScope.launch {
                    absenceDeputyRepository.insertAll(absenceDeputyEntities)
                }

                if(latestOpen.has("wtId")) {
                    liveLatestOpen.postValue(latestOpen)
                }
                liveHideMask.postValue(true)
            } else {
                liveHideMask.postValue(true)
                viewModelScope.launch {
                    val lastOpen = absenceService.getLatestOpenByUserId(userId.toString())
                    if (lastOpen != null) {
                        lastOpen.toJsonObject().let {
                            liveLatestOpen.postValue(it)
                        }
                    } else {
                        liveShowOfflineNotice.postValue(true)
                        onRepoLoaded()
                    }
                }
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

    private fun onRepoLoaded() {
        repoLoad++
        if (repoLoad == 3L) {
            liveHideMask.postValue(true)
            val userRights = absenceTypeRightEntities.filter {
                it.userId == userId
            }.map { it.absenceTypeId }.toSet()
            val filteredAbsence = absenceTypeEntities.filter {
                userRights.contains(it.id)
            }
            liveAbsenceTypes.postValue(filteredAbsence)
        }
    }

    suspend fun hasUserErrorAbsenceEntries() : Boolean {
        val count = absenceService.getCountOfFailedForUser(userId.toString())
        return count > 0
    }
}