package com.timo.timoterminal.service

import com.timo.timoterminal.entityClasses.BookingEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.BookingBURepository
import com.timo.timoterminal.repositories.BookingRepository
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class BookingService(
    private val bookingRepository: BookingRepository,
    private val bookingBURepository: BookingBURepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService
) : KoinComponent {
    private val hardware: IHardwareSource by inject()

    private fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    private fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    private fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    suspend fun insertBooking(
        entity: BookingEntity
    ) {
        bookingRepository.insertBookingEntity(entity)
        insertBU()
    }

    private suspend fun insertBU() {
        val entities = bookingRepository.getAllAsList()
        bookingBURepository.insertBookingEntities(entities)
    }

    suspend fun sendSavedBooking(scope: CoroutineScope) {
        bookingBURepository.deleteOldBUBookings()
        if (bookingRepository.count() > 0) {
            val url = getURl()
            val company = getCompany()
            val token = getToken()
            if (!company.isNullOrEmpty() && token.isNotEmpty()) {
                val bookings = bookingRepository.getAllAsList()
                val params = JSONObject()
                params.put("terminalSN", hardware.serialNumber())
                params.put("token", token)
                params.put("firma", company)
                val arr = JSONArray()
                for (booking in bookings) {
                    val obj = JSONObject()
                    obj.put("card", booking.card)
                    obj.put("date", booking.date)
                    obj.put("funcCode", "${booking.status}")
                    obj.put("inputCode", "${booking.inputCode}")
                    arr.put(obj)
                }
                params.put("data", arr)
                httpService.postJson(
                    "${url}services/rest/zktecoTerminal/offlineBooking",
                    params.toString(),
                    null,
                    { _, _, msg ->
                        if(msg != null && msg.toLong() == bookings.size.toLong()) {
                            scope.launch {
                                for (booking in bookings) {
                                    if (booking.id != null)
                                        bookingBURepository.setIsSend(booking.id!!)
                                    bookingRepository.delete(booking)
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    fun deleteAll(scope: CoroutineScope) {
        scope.launch {
            bookingRepository.deleteAll()
            bookingBURepository.deleteAll()
        }
    }
}