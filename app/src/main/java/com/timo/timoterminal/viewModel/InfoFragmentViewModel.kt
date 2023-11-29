package com.timo.timoterminal.viewModel

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.fragmentViews.InfoFragment
import com.timo.timoterminal.modalBottomSheets.MBFragmentInfoSheet
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.GregorianCalendar


class InfoFragmentViewModel(
    private val userRepository: UserRepository,
    private val sharedPrefService: SharedPrefService,
    private val httpService: HttpService,
    private val languageService: LanguageService
) : ViewModel() {

    private lateinit var sheet: MBFragmentInfoSheet
    private lateinit var fragment: InfoFragment
    private val timer = object : CountDownTimer(10000, 950) {
        override fun onTick(millisUntilFinished: Long) {
            showSeconds(millisUntilFinished)
        }

        override fun onFinish() {
            hideUserInformation()
        }
    }

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

    private suspend fun getUserForLogin(login: String): UserEntity? {
        return withContext(Dispatchers.IO) {
            val users = userRepository.getEntityByLogin(login)
            if (users.isNotEmpty()) {
                return@withContext users[0]
            }
            null
        }
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

    fun loadUserInfoByCard(card: String, fragment: InfoFragment) {
        viewModelScope.launch {
            val user = getUserEntityByCard(card)
            if (user != null) {
                loadUserInformation(user, fragment)
            } else {
                fragment.showCard(card)
            }
        }
    }

    fun loadUserInfoByLoginAndPin(login: String, pin: String, fragment: InfoFragment) {
        viewModelScope.launch {
            val user = getUserForLogin(login)
            if (user != null && user.pin == pin) {
                loadUserInformation(user, fragment)
            }
        }
    }

    private suspend fun loadUserInformation(user: UserEntity, fragment: InfoFragment) {
        fragment.unregister()
        fragment.setVerifying(false)
        val url = getURl()
        val company = getCompany()
        val terminalId = getTerminalID()
        val token = getToken()
        if (!company.isNullOrEmpty() && terminalId > 0 && !token.isNullOrEmpty()) {
            withContext(Dispatchers.IO) {
                httpService.get(
                    "${url}services/rest/zktecoTerminal/info",
                    mapOf(
                        Pair("card", user.card),
                        Pair("firma", company),
                        Pair("terminalId", terminalId.toString()),
                        Pair("token", token)
                    ),
                    null,
                    { obj, _, _ ->
                        if (obj != null) {
                            if (obj.getBoolean("success")) {
                                this@InfoFragmentViewModel.fragment = fragment
                                fragment.activity?.runOnUiThread {
                                    val res = obj.getString("message")
                                    val bundle = Bundle()
                                    bundle.putString("res", res)
                                    bundle.putString("card", user.card)
                                    sheet = MBFragmentInfoSheet()
                                    sheet.arguments = bundle
                                    sheet.viewModel = this@InfoFragmentViewModel
                                    sheet.show(
                                        fragment.parentFragmentManager,
                                        MBFragmentInfoSheet.TAG
                                    )
                                    timer.start()
                                }
                            } else {
                                fragment.activity?.runOnUiThread {
                                    Utils.showMessage(
                                        fragment.parentFragmentManager,
                                        obj.getString("message")
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    private fun showSeconds(millisUntilFinished: Long) {
        val binding = sheet.getBinding()
        fragment.activity?.runOnUiThread {
            binding.textviewSecondClose.text = (millisUntilFinished / 900).toString()
        }
    }

    private fun hideUserInformation() {
        sheet.dismiss()
        (fragment.activity as MainActivity?)?.cancelTimer()
        fragment.activity?.runOnUiThread {
            fragment.parentFragmentManager.commit {
                replace(
                    R.id.fragment_container_view,
                    AttendanceFragment(),
                    AttendanceFragment.TAG
                )
            }
        }
    }

    fun restartTimer() {
        timer.cancel()
        timer.start()
        (fragment.activity as MainActivity?)?.restartTimer()
    }
}