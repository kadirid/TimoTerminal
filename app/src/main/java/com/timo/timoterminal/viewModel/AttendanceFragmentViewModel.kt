package com.timo.timoterminal.viewModel

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.modalBottomSheets.MBBookingMessageSheet
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.SharedPrefService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AttendanceFragmentViewModel(
    private val sharedPrefService: SharedPrefService,
    private val userRepository: UserRepository
) : ViewModel() {

    fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    fun getTerminalID(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
    }

    fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    private suspend fun getUserEntityByCard(card: String): UserEntity? {
        return withContext(Dispatchers.IO) {
            val users = userRepository.getEntityByCard(card)
            if (users.isNotEmpty()) {
                return@withContext users[0]
            }
            null
        }
    }

    fun showMessage(
        fragment: AttendanceFragment,
        card: String,
        funcCode: Int,
        success: Boolean,
        message: String
    ) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                fragment.activity?.runOnUiThread {
                    val bookingMessage = MBBookingMessageSheet()
                    val bundle = Bundle()
                    bundle.putInt("funcCode", funcCode)
                    bundle.putString("name", user.name())
                    bundle.putString("message", message)
                    bundle.putBoolean("success", success)
                    bookingMessage.arguments = bundle
                    bookingMessage.show(fragment.parentFragmentManager, MBBookingMessageSheet.TAG)
                }
            }
        }
    }

    suspend fun getUser(id:Long):UserEntity?{
        val users = userRepository.getEntity(id)
        if(users.isNotEmpty()){
            return users[0]
        }
        return null
    }

    suspend fun getUserByCard(card:String):UserEntity?{
        val users = userRepository.getEntityByCard(card)
        if(users.isNotEmpty()){
            return users[0]
        }
        return null
    }
}