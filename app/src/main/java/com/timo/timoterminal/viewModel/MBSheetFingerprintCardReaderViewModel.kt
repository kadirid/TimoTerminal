package com.timo.timoterminal.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.modalBottomSheets.MBSheetFingerprintCardReader
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class MBSheetFingerprintCardReaderViewModel(
    private val userRepository: UserRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService
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

    fun sendBookingByCard(card: String, sheet: MBSheetFingerprintCardReader) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                val greg = GregorianCalendar()
                val time = Utils.getTimeFromGC(greg)
                sheet.activity?.runOnUiThread {
                    sheet.getBinding().nameContainer.text = user.name
                    sheet.getBinding().timeTextContainer.text = time
                }
                sendBooking(user.card, 1, Utils.getDateTimeFromGC(greg), sheet)
                sheet.setStatus(-1)
            } else {
                Snackbar.make(sheet.getBinding().root, "Verification failed", Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }

    fun sendBookingById(id: String, pin: String, sheet: MBSheetFingerprintCardReader) {
        viewModelScope.launch {
            val user = getUserEntity(id.toLong())
            if (user != null && user.pin == pin) {
                val greg = GregorianCalendar()
                val time = Utils.getTimeFromGC(greg)
                sheet.activity?.runOnUiThread {
                    sheet.getBinding().nameContainer.text = user.name
                    sheet.getBinding().timeTextContainer.text = time
                }
                sendBooking(user.card, 1, Utils.getDateTimeFromGC(greg), sheet)
                sheet.setStatus(-1)
            } else {
                Snackbar.make(sheet.getBinding().root, "Verification failed", Snackbar.LENGTH_LONG)
                    .show()
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
        sheet.animateSuccess()
        val url = getURl()
        val company = getCompany()
        val terminalId = getTerminalID()
        if (!company.isNullOrEmpty() && terminalId > 0 && sheet.getStatus() > 0) {
            withContext(ioDispatcher) {
                httpService.post(
                    "${url}services/rest/zktecoTerminal/booking",
                    mapOf(
                        Pair("card", card),
                        Pair("firma", company),
                        Pair("date", date),
                        Pair("funcCode", "${sheet.getStatus()}"),
                        Pair("inputCode", "$inputCode"),
                        Pair("terminalId", terminalId.toString())
                    ),
                    sheet.requireContext(),
                    { obj, _, _ ->
                        if (obj != null) {
                            val bar = Snackbar.make(
                                sheet.getBinding().root,
                                obj.getString("message"),
                                Snackbar.LENGTH_LONG
                            )
                            if (obj.getBoolean("success")) {
                                RfidService.unregister()
                                FingerprintService.unregister()

                                bar.addCallback(object :
                                    BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                    override fun onDismissed(
                                        transientBottomBar: Snackbar?,
                                        event: Int
                                    ) {
                                        super.onDismissed(transientBottomBar, event)
                                        sheet.dismiss()
                                    }
                                })
                            }
                            bar.show()
                        }
                    }
                )
            }
        }
    }
}