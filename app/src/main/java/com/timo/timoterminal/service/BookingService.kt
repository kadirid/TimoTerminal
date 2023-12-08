package com.timo.timoterminal.service

import com.timo.timoterminal.entityClasses.BookingEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.BookingBURepository
import com.timo.timoterminal.repositories.BookingRepository
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class BookingService (
    private val bookingRepository: BookingRepository,
    private val bookingBURepository: BookingBURepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService
): KoinComponent {

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

    suspend fun insertBooking(
        card: String,
        inputCode: Int,
        date: String,
        status: Int
    ) {
        val entity = BookingEntity(card, inputCode, Utils.parseToDBDate(date), status)
        bookingRepository.insertBookingEntity(entity)
        check()
    }

    private suspend fun insertBU(){
        val entities = bookingRepository.getAllAsList()
        bookingBURepository.insertBookingEntities(entities)
    }

    suspend fun check(){
        insertBU()
        val buEntities = bookingBURepository.getAllAsList()
        for(entity in buEntities){
            println(entity)
        }
        val entities = bookingRepository.getAllAsList()
        for(entity in entities){
            println(entity)
        }
    }

    suspend fun sendSavedBooking(scope: CoroutineScope){
        bookingBURepository.deleteOldBUBookings()
        if(bookingRepository.count() > 0){
            val url = getURl()
            val company = getCompany()
            val terminalId = getTerminalID()
            val token = getToken()
            if (!company.isNullOrEmpty() && terminalId > 0 && token.isNotEmpty()) {
                val bookings = bookingRepository.getAllAsList()
                for (booking in bookings) {
                    httpService.post(
                        "${url}services/rest/zktecoTerminal/booking",
                        mapOf(
                            Pair("card", booking.card),
                            Pair("firma", company),
                            Pair("date", Utils.parseFromDBDate(booking.date)),
                            Pair("funcCode", "${booking.status}"),
                            Pair("inputCode", "${booking.inputCode}"),
                            Pair("terminalId", terminalId.toString()),
                            Pair("token", token),
                            Pair("validate", "false")
                        ),
                        null,
                        { obj, _, _ ->
                            if (obj != null) {
                                scope.launch {
                                    if(booking.id != null)
                                        bookingBURepository.setIsSend(booking.id!!)
                                    bookingRepository.delete(booking)
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}