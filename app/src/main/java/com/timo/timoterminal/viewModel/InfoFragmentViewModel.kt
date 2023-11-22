package com.timo.timoterminal.viewModel

import android.annotation.SuppressLint
import android.os.CountDownTimer
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.FragmentInfoBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.fragmentViews.InfoFragment
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

    private lateinit var fragment: InfoFragment
    private val timer = object : CountDownTimer(5000, 900) {
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
        val binding = fragment.getBinding()
        fragment.unregister()
        fragment.setVerifying(false)
        fragment.activity?.runOnUiThread {
            binding.linearValidationContainer.isVisible = false
            binding.linearLoadContainer.isVisible = true
        }
        val url = getURl()
        val company = getCompany()
        val terminalId = getTerminalID()
        if (!company.isNullOrEmpty() && terminalId > 0) {
            withContext(Dispatchers.IO) {
                httpService.get(
                    "${url}services/rest/zktecoTerminal/info",
                    mapOf(
                        Pair("card", user.card),
                        Pair("firma", company),
                        Pair("terminalId", terminalId.toString())
                    ),
                    fragment.requireContext(),
                    { obj, _, _ ->
                        if (obj != null) {
                            if (obj.getBoolean("success")) {
                                this@InfoFragmentViewModel.fragment = fragment
                                fragment.activity?.runOnUiThread {
                                    val res = JSONObject(obj.getString("message"))
                                    setText(res, binding)
                                    binding.linearLoadContainer.isVisible = false
                                    binding.linearTextContainer.isVisible = true
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
        val binding = fragment.getBinding()
        fragment.activity?.runOnUiThread {
            binding.textviewSecondClose.text = (millisUntilFinished / 900).toString()
        }
    }

    private fun hideUserInformation() {
        fragment.activity?.runOnUiThread {
            fragment.parentFragmentManager.commit {
                replace(
                    R.id.fragment_container_view,
                    AttendanceFragment.newInstance("", ""),
                    AttendanceFragment.TAG
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setText(res: JSONObject, binding: FragmentInfoBinding) {
        var ist = res.getDouble("ist")
        if (res.optInt("zeitTyp", 0) in listOf(1, 4, 6) && !res.optString("zeitLB", "")
                .isNullOrBlank()
        ) {
            val gcDate = GregorianCalendar()
            val greg = Utils.parseDateTime(res.getString("zeitLB"))
            var diff = gcDate.timeInMillis - greg.timeInMillis
            diff /= 1000
            diff /= 60
            ist += diff
        }
        binding.textviewTimeTarget.text = "${getText("#Target")}: ${res.getString("soll")}"
        binding.textviewTimeActual.text = "${getText("ALLGEMEIN#Ist")}: ${Utils.convertTime(ist)}"
        binding.textviewTimeStartOfWork.text =
            "${getText("#CheckIn")}: ${res.getString("kommen")}"
        binding.textviewTimeBreakTotal.text =
            "${getText("ALLGEMEIN#Pause")}: ${res.getString("pause")}"
        binding.textviewTimeEndOfWork.text =
            "${getText("#CheckOut")}: ${res.getString("gehen")}"
        binding.textviewTimeOvertime.text =
            "${getText("PDFSOLLIST#spalteGzGleitzeit")}: ${res.getString("overtime")}"
        binding.textviewVacationEntitlement.text =
            "${getText("ALLGEMEIN#Anspruch")}: ${res.getString("vacation")}"
        binding.textviewVacationTaken.text =
            "${getText("ALLGEMEIN#Genommen")}: ${res.getString("gVacation")}"
        binding.textviewVacationRequested.text =
            "${getText("ALLGEMEIN#Beantragt")}: ${res.getString("bVacation")}"
        binding.textviewVacationRemaining.text =
            "${getText("#Remaining")}: ${res.getString("rVacation")}"
    }

    fun restartTimer() {
        timer.cancel()
        timer.start()
    }

    private fun getText(key: String) = languageService.getText(key)
}