package com.timo.timoterminal.viewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.AbsenceDeputyEntity
import com.timo.timoterminal.entityClasses.AbsenceEntryEntity
import com.timo.timoterminal.entityClasses.AbsenceTypeEntity
import com.timo.timoterminal.entityClasses.AbsenceTypeMatrixEntity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.repositories.AbsenceDeputyRepository
import com.timo.timoterminal.repositories.AbsenceTypeMatrixRepository
import com.timo.timoterminal.repositories.AbsenceTypeRepository
import com.timo.timoterminal.repositories.AbsenceTypeRightRepository
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.AbsenceService
import com.timo.timoterminal.service.LanguageService
import kotlinx.coroutines.launch

class AbsenceEditViewModel(
    private val languageService: LanguageService,
    private val absenceService: AbsenceService,
    private val absenceTypeRepository: AbsenceTypeRepository,
    private val absenceTypeMatrixRepository: AbsenceTypeMatrixRepository,
    private val absenceTypeRightRepository: AbsenceTypeRightRepository,
    private val absenceDeputyRepository: AbsenceDeputyRepository,
    private val configRepository: ConfigRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveMessage: MutableLiveData<String> = MutableLiveData()
    val liveAbsenceType: MutableLiveData<AbsenceTypeEntity> = MutableLiveData()
    val liveAbsenceTypeMatrix: MutableLiveData<List<AbsenceTypeMatrixEntity>> = MutableLiveData()
    val liveAbsenceDeputies: MutableLiveData<List<AbsenceDeputyEntity>> = MutableLiveData()
    val liveRequireDeputy: MutableLiveData<Boolean> = MutableLiveData(false)
    val livePostWtId: MutableLiveData<Long> = MutableLiveData(-1L)
    val liveSwitchView: MutableLiveData<Boolean> = MutableLiveData(false)
    val liveUser: MutableLiveData<UserEntity> = MutableLiveData()

    var absenceTypeId: Long = -1
    var userId: Long = -1

    private var absenceType: AbsenceTypeEntity? = null
    private var absenceTypeMatrixEntities: List<AbsenceTypeMatrixEntity> = emptyList()
    private var absenceDeputyEntities: List<AbsenceDeputyEntity> = emptyList()
    private var requireDeputy: Boolean = false

    fun loadAbsenceValuesForEdit() {
        viewModelScope.launch {
            absenceType = absenceTypeRepository.getById(absenceTypeId.toInt())
            absenceTypeMatrixEntities =
                absenceTypeMatrixRepository.getByAbsenceTypeId(absenceTypeId.toInt())
            absenceDeputyEntities = absenceDeputyRepository.getByUserId(userId.toInt())
            requireDeputy =
                absenceTypeRightRepository.getByAbsenceTypeIdAndUserId(
                    absenceTypeId.toInt(),
                    userId.toInt()
                )?.deputyRequired ?: false

            val user = userRepository.getEntity(userId)
            if (user.isNotEmpty()) {
                liveUser.postValue(user[0])
            }

            if (absenceType != null) {
                liveAbsenceType.postValue(absenceType!!)
            }
            liveAbsenceTypeMatrix.postValue(absenceTypeMatrixEntities)
            liveAbsenceDeputies.postValue(absenceDeputyEntities)
            liveRequireDeputy.postValue(requireDeputy)
        }
    }

    fun loadValuesForStartStop() {
        viewModelScope.launch {
            absenceType = absenceTypeRepository.getById(absenceTypeId.toInt())
            absenceDeputyEntities = absenceDeputyRepository.getByUserId(userId.toInt())

            if (absenceType != null) {
                liveAbsenceType.postValue(absenceType!!)
            }
            liveAbsenceDeputies.postValue(absenceDeputyEntities)
        }
    }

    fun saveAbsenceEntry(data: HashMap<String, String?>, context: Context) {
        liveShowMask.postValue(true)
        absenceService.saveAbsenceEntry(data, context) { success, obj ->
            liveHideMask.postValue(true)
            if (success){
                if(obj?.optBoolean("success") == true){
                    liveMessage.postValue(obj.optString("message", ""))
                    if (!data["id"].isNullOrEmpty()) {
                        viewModelScope.launch {
                            absenceService.deleteById(data["id"]!!.toLong())
                        }
                    }
                } else {
                    if (obj?.has("message") == true) {
                        liveMessage.postValue("e${obj.getString("message")}")
                    }
                }
            } else {
                liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                val entry = AbsenceEntryEntity.parseFromMap(data)
                viewModelScope.launch {
                    absenceService.saveAbsenceEntry(entry)
                }
            }
        }
    }

    fun startAbsenceTime(data: HashMap<String, String?>, context: Context) {
        liveShowMask.postValue(true)
        absenceService.startAbsenceTime(data, context) { success, obj ->
            liveHideMask.postValue(true)
            if (success){
                if (obj?.optBoolean("success") == true) {
                    val wtId = obj.optLong("absenceTimeId", -1L)
                    val absence = AbsenceEntryEntity.parseFromMap(data)
                    absence.wtId = if (wtId != -1L) wtId else null
                    absence.isVisible = false
                    viewModelScope.launch {
                        absenceService.saveAbsenceEntry(absence)
                    }
                    liveMessage.postValue(obj.optString("message", ""))
                    livePostWtId.postValue(wtId)
                    liveSwitchView.postValue(true)
                } else {
                    if (obj?.has("message") == true) {
                        liveMessage.postValue("e${obj.getString("message")}")
                    }
                }
            } else {
                liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                val entry = AbsenceEntryEntity.parseFromMap(data)
                viewModelScope.launch {
                    absenceService.saveAbsenceEntry(entry)
                }
                liveSwitchView.postValue(true)
            }
        }
    }

    fun stopAbsenceTime(data: HashMap<String, String?>, context: Context, offlineSaved: Boolean) {
        liveShowMask.postValue(true)
        if (offlineSaved) {
            viewModelScope.launch {
                absenceService.stopOfflineAbsenceTime(data)
                liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                liveHideMask.postValue(true)
            }
            liveSwitchView.postValue(false)
        } else {
            absenceService.stopAbsenceTime(data, context) { success, obj ->
                liveHideMask.postValue(true)
                if (success) {
                    if (obj?.optBoolean("success") == true) {
                        liveMessage.postValue(obj.optString("message", ""))
                        liveSwitchView.postValue(false)
                    } else {
                        if (obj?.has("message") == true) {
                            liveMessage.postValue("e${obj.getString("message")}")
                        }
                    }
                } else {
                    liveMessage.postValue(languageService.getText("#BookingTemporarilySaved"))
                    val entry = AbsenceEntryEntity.parseFromMap(data)
                    viewModelScope.launch {
                        absenceService.saveAbsenceEntry(entry)
                    }
                    liveSwitchView.postValue(false)
                }
            }
        }
    }

    suspend fun permission(name: String): String {
        return configRepository.getPermissionValue(name)
    }
}