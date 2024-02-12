package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.modalBottomSheets.MBSheetFingerprintCardReader
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.BookingService
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MBSheetFingerprintCardReaderViewModel(
    private val userRepository: UserRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val bookingService: BookingService,
    private val languageService: LanguageService,
    private val soundSource: SoundSource
) : ViewModel() {

    private val ioDispatcher = Dispatchers.IO

    private fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    private fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    private fun getTerminalID(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
    }

    private fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    // maybe useful for fingerprint
    private suspend fun getUserEntity(id: Long): UserEntity? {
        return withContext(ioDispatcher) {
            val users = userRepository.getEntity(id)
            if (users.isNotEmpty()) {
                return@withContext users[0]
            }
            null
        }
    }

    private suspend fun getUserEntityByCard(card: String): UserEntity? {
        return withContext(ioDispatcher) {
            val users = userRepository.getEntityByCard(card)
            if (users.isNotEmpty()) {
                return@withContext users[0]
            }
            null
        }
    }

    private suspend fun getUserEntityByLogin(login: String): UserEntity? {
        return withContext(ioDispatcher) {
            val users = userRepository.getEntityByLogin(login)
            if (users.isNotEmpty()) {
                return@withContext users[0]
            }
            null
        }
    }

    fun sendBookingById(id: Long, sheet: MBSheetFingerprintCardReader) {
        viewModelScope.launch {
            val user = getUserEntity(id)
            if (user != null) {
                val greg = Utils.getCal()
                val time = Utils.getTimeFromGC(greg)
                sheet.activity?.runOnUiThread {
                    sheet.getBinding().nameContainer.text = user.name()
                    sheet.getBinding().timeTextContainer.text = time
                }
                sheet.showLoadMask()
                sendBooking(user.card, 2, Utils.getDateTimeFromGC(greg), sheet)
                sheet.setStatus(-1)
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
                sheet.animateSuccess()
                sheet.getBinding().textViewBookingMessage.text =
                    languageService.getText("#VerificationFailed")
                val color =
                    sheet.activity?.resources?.getColorStateList(R.color.error_booking, null)
                if (color != null)
                    sheet.getBinding().bookingInfoContainer.backgroundTintList = color
            }
        }
    }

    fun sendBookingByCard(card: String, sheet: MBSheetFingerprintCardReader) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                val greg = Utils.getCal()
                val time = Utils.getTimeFromGC(greg)
                sheet.activity?.runOnUiThread {
                    sheet.getBinding().nameContainer.text = user.name()
                    sheet.getBinding().timeTextContainer.text = time
                }
                sheet.showLoadMask()
                sendBooking(user.card, 1, Utils.getDateTimeFromGC(greg), sheet)
                sheet.setStatus(-1)
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
                sheet.animateSuccess()
                sheet.getBinding().textViewBookingMessage.text =
                    languageService.getText("#VerificationFailed")
                val color =
                    sheet.activity?.resources?.getColorStateList(R.color.error_booking, null)
                if (color != null)
                    sheet.getBinding().bookingInfoContainer.backgroundTintList = color
            }
        }
    }

    fun sendBookingByLogin(login: String, pin: String, sheet: MBSheetFingerprintCardReader) {
        viewModelScope.launch {
            val user = getUserEntityByLogin(login)
            if (user != null && user.pin == pin) {
                val greg = Utils.getCal()
                val time = Utils.getTimeFromGC(greg)
                sheet.activity?.runOnUiThread {
                    sheet.getBinding().nameContainer.text = user.name()
                    sheet.getBinding().timeTextContainer.text = time
                }
                sheet.showLoadMask()
                sendBooking(user.card, 1, Utils.getDateTimeFromGC(greg), sheet)
                sheet.setStatus(-1)
            } else {
                soundSource.playSound(SoundSource.authenticationFailed)
                sheet.animateSuccess()
                sheet.getBinding().textViewBookingMessage.text =
                    languageService.getText("#VerificationFailed")
                val color =
                    sheet.activity?.resources?.getColorStateList(R.color.error_booking, null)
                if (color != null)
                    sheet.getBinding().bookingInfoContainer.backgroundTintList = color
            }
        }
    }

    // send all necessary information to timo to create a booking
    private suspend fun sendBooking(
        card: String,
        inputCode: Int,
        date: String,
        sheet: MBSheetFingerprintCardReader
    ) {
        if (Utils.isOnline(sheet.requireContext())) {
            val url = getURl()
            val company = getCompany()
            val terminalId = getTerminalID()
            val token = getToken()
            if (!company.isNullOrEmpty() && terminalId > 0 && sheet.getStatus() > 0 && token.isNotEmpty()) {
                withContext(ioDispatcher) {
                    httpService.post(
                        "${url}services/rest/zktecoTerminal/booking",
                        mapOf(
                            Pair("card", card),
                            Pair("firma", company),
                            Pair("date", date),
                            Pair("funcCode", "${sheet.getStatus()}"),
                            Pair("inputCode", "$inputCode"),
                            Pair("terminalId", terminalId.toString()),
                            Pair("token", token),
                            Pair("validate", "true")
                        ),
                        sheet.requireContext(),
                        { obj, _, _ ->
                            if (obj != null) {
                                sheet.animateSuccess()
                                RfidService.unregister()
                                FingerprintService.unregister()
                                sheet.activity?.runOnUiThread {
                                    sheet.getBinding().textViewBookingMessage.text =
                                        obj.getString("message")
                                    if (!obj.getBoolean("success")) {
                                        soundSource.playSound(SoundSource.failedSound)
                                        val color =
                                            sheet.activity?.resources?.getColorStateList(
                                                R.color.error_booking,
                                                null
                                            )
                                        if (color != null)
                                            sheet.getBinding().bookingInfoContainer.backgroundTintList =
                                                color
                                    }else{
                                        soundSource.playSound(SoundSource.successSound)
                                    }
                                }
                            }
                            sheet.hideLoadMask()
                        }, { e, res, context, output ->
                            sheet.hideLoadMask()
                            soundSource.playSound(SoundSource.offlineSaved)
                            viewModelScope.launch {
                                bookingService.insertBooking(
                                    card,
                                    inputCode,
                                    date,
                                    sheet.getStatus()
                                )
                            }
                            HttpService.handleGenericRequestError(
                                e,
                                res,
                                context,
                                output,
                                languageService.getText("#TimoServiceNotReachable") + " " +
                                        languageService.getText("#BookingTemporarilySaved")
                            )
                        }
                    )
                }
            }
        } else {
            viewModelScope.launch {
                soundSource.playSound(SoundSource.offlineSaved)
                bookingService.insertBooking(card, inputCode, date, sheet.getStatus())
                sheet.activity?.runOnUiThread {
                    sheet.getBinding().textViewBookingMessage.text =
                        languageService.getText("#BookingTemporarilySaved")
                    sheet.animateSuccess()
                    RfidService.unregister()
                    FingerprintService.unregister()
                }
            }
        }
    }

}