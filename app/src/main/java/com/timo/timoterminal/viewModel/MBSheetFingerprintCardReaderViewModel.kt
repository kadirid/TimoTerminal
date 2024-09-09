package com.timo.timoterminal.viewModel

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.entityClasses.BookingEntity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.BookingService
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.lcdk.data.IFingerprintListener
import com.zkteco.android.lcdk.data.IRfidListener
import kotlinx.coroutines.launch

class MBSheetFingerprintCardReaderViewModel(
    private val userRepository: UserRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val bookingService: BookingService,
    private val languageService: LanguageService,
    private val soundSource: SoundSource,
) : ViewModel(), IRfidListener, IFingerprintListener {

    val liveHideMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMask: MutableLiveData<Boolean> = MutableLiveData()
    val liveShowMessageSheet: MutableLiveData<Bundle> = MutableLiveData()

    var status = 0

    private fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    private fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    private fun getTerminalId(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
    }

    private fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    // maybe useful for fingerprint
    private suspend fun getUserEntity(id: Long): UserEntity? {
        val users = userRepository.getEntity(id)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private suspend fun getUserEntityByCard(card: String): UserEntity? {
        val users = userRepository.getEntityByCard(card)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private suspend fun getUserEntityByPIN(pin: String): UserEntity? {
        val users = userRepository.getEntityByPIN(pin)
        if (users.isNotEmpty()) {
            return users[0]
        }
        return null
    }

    private fun sendBookingById(id: Long) {
        viewModelScope.launch {
            val user = getUserEntity(id)
            if (user != null) {
                val greg = Utils.getCal()
                liveShowMask.postValue(true)
                sendBooking(
                    BookingEntity(
                        id.toString(),
                        2,
                        Utils.getDateTimeFromGC(greg),
                        status
                    ), user.name()
                )
            } else {
                errorNoUser("#VerificationFailed")
            }
        }
    }

    private fun sendBookingByCard(card: String) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                val greg = Utils.getCal()
                liveShowMask.postValue(true)
                sendBooking(
                    BookingEntity(
                        user.card,
                        1,
                        Utils.getDateTimeFromGC(greg),
                        status
                    ), user.name()
                )
            } else {
                errorNoUser("#Number unknown")
            }
        }
    }

    fun sendBookingByPIN(pin: String) {
        viewModelScope.launch {
            val user = getUserEntityByPIN(pin)
            if (user != null) {
                val greg = Utils.getCal()
                liveShowMask.postValue(true)
                sendBooking(
                    BookingEntity(
                        user.id.toString(),
                        0,
                        Utils.getDateTimeFromGC(greg),
                        status
                    ), user.name()
                )
            } else {
                errorNoUser("#VerificationFailed")
            }
        }
    }

    private fun errorNoUser(key: String) {
        soundSource.playSound(SoundSource.authenticationFailed)
        val bundle = Bundle()
        bundle.putBoolean("success", false)
        bundle.putString("error", languageService.getText(key))
        bundle.putString("message", "")
        liveShowMessageSheet.postValue(bundle)
    }

    // send all necessary information to timo to create a booking
    private fun sendBooking(
        entity: BookingEntity,
        name: String
    ) {
        viewModelScope.launch {
            val url = getURl()
            val tId = getTerminalId()
            val company = getCompany()
            val token = getToken()
            if (!company.isNullOrEmpty() && entity.status > 0 && token.isNotEmpty()) {
                httpService.post(
                    "${url}services/rest/zktecoTerminal/booking",
                    mapOf(
                        Pair("card", entity.card),
                        Pair("firma", company),
                        Pair("date", entity.date),
                        Pair("funcCode", entity.status.toString()),
                        Pair("inputCode", entity.inputCode.toString()),
                        Pair("terminalSN", MainApplication.lcdk.getSerialNumber()),
                        Pair("terminalId", "$tId"),
                        Pair("token", token),
                        Pair("validate", "true")
                    ),
                    null,
                    { obj, _, _ ->
                        if (obj != null) {
                            val bundle = Bundle()
                            if (obj.has("bookingType"))
                                bundle.putInt("status", obj.getInt("bookingType"))
                            if (obj.has("adjusted"))
                                bundle.putBoolean("adjusted", obj.getBoolean("adjusted"))
                            if (obj.has("error"))
                                bundle.putString("error", obj.getString("error"))
                            bundle.putBoolean("success", obj.getBoolean("success"))
                            bundle.putString("message", obj.getString("message"))
                            bundle.putString("name", name)
                            liveShowMessageSheet.postValue(bundle)
                            MainApplication.lcdk.setRfidListener(null)
                            MainApplication.lcdk.setFingerprintListener(null)
                            if (!obj.getBoolean("success")) {
                                soundSource.playSound(SoundSource.failedSound)
                            } else {
                                soundSource.playSound(SoundSource.successSound)
                            }
                        }
                        liveHideMask.postValue(true)
                    }, { _, _, _, _ ->
                        processOffline(entity, name)
                    }
                )
            }
        }
    }

    private fun processOffline(entity: BookingEntity, name: String) {
        viewModelScope.launch {
            soundSource.playSound(SoundSource.offlineSaved)
            MainApplication.lcdk.setRfidListener(null)
            MainApplication.lcdk.setFingerprintListener(null)
            val bundle = Bundle()
            bundle.putInt(
                "status", when (entity.status) {
                    100 -> 1
                    200 -> 2
                    else -> 3
                }
            )
            bundle.putBoolean("offline", true)
            bundle.putString("name", name)
            bundle.putString("message", "")
            liveShowMessageSheet.postValue(bundle)
            bookingService.insertBooking(entity)
        }
    }

    // get code of scanned card
    override fun onRfidRead(text: String) {
        viewModelScope.launch {
            val rfidCode = text.toLongOrNull(16)
            if (rfidCode != null) {
                var oct = rfidCode.toString(8)
                while (oct.length < 9) {
                    oct = "0$oct"
                }
                oct = oct.reversed()
                sendBookingByCard(oct)
            }
        }
    }

    override fun onFingerprintPressed(template: ByteArray): Boolean {
        viewModelScope.launch {
            // get Key associated to the fingerprint
            MainApplication.lcdk.identifyFingerPrint(template,70).run {
                if(this.isNotEmpty()) {
                    val id = this.substring(0, this.length - 2).toLong()
                    sendBookingById(id)
                    return@launch
                }
            }
        }
        return true
    }

    override fun onFingerprintPressed(template: ByteArray, bmp: Bitmap?) {}

}