package com.timo.timoterminal.service

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import androidx.core.os.postDelayed
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.modalBottomSheets.MBRemoteRegisterSheet
import com.timo.timoterminal.utils.Utils
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class HeartbeatService : KoinComponent {

    private val sharedPrefService: SharedPrefService by inject()
    private val settingsService: SettingsService by inject()
    private val userService: UserService by inject()
    private val languageService: LanguageService by inject()
    private val loginService: LoginService by inject()
    private val bookingService: BookingService by inject()
    private val httpService: HttpService by inject()
    private val hardware: IHardwareSource by inject()
    private lateinit var handler: Handler
    private var client: OkHttpClient = OkHttpClient().newBuilder()
        .retryOnConnectionFailure(false)
        .connectTimeout(10000, TimeUnit.MILLISECONDS)
        .callTimeout(10000, TimeUnit.MILLISECONDS)
        .build()

    private fun getCompany(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
    }

    private fun getURl(): String? {
        return sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
    }

    private fun getTerminalId(): Int {
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID,-1)
    }

    private fun getToken(): String {
        return sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""
    }

    fun initHeartbeatWorker(activity: MainActivity) {
        val handlerThread = HandlerThread("backgroundThread")
        if (!handlerThread.isAlive) handlerThread.start()
        handler = Handler(handlerThread.looper)
        var runnable: Runnable? = null

        runnable = Runnable {
            handler.postDelayed(runnable!!, 30000L)
            val url = getURl()
            val tId = getTerminalId()
            val company = getCompany()
            val token = getToken()
            val date = Utils.getDateTimeFromGC(Utils.getCal())
            if (!company.isNullOrEmpty() && token.isNotEmpty()) {
                httpService.postWithClient(
                    client,
                    "${url}services/rest/zktecoTerminal/heartbeat",
                    mapOf(
                        Pair("firma", company),
                        Pair("date", date),
                        Pair("terminalSN", hardware.serialNumber()),
                        Pair("terminalId", "$tId"),
                        Pair("token", token)
                    ),
                    null,
                    { obj, _, _ ->
                        if (obj != null) {
                            handelHeartBeatResponse(obj, activity)
                        }
                    }, { _, _, _, _ ->
                        val color = activity.resources?.getColorStateList(R.color.red, null)
                        if (activity.getBinding().serverConnectionIcon.imageTintList != color) {
                            activity.runOnUiThread {
                                activity.getBinding().serverConnectionIcon.imageTintList = color
                            }
                        }
                    }
                )
            }
        }
        handler.postDelayed(runnable, 1000L)
    }

    private fun handelHeartBeatResponse(obj: JSONObject, activity: MainActivity) {
        val timezone = sharedPrefService.getString(SharedPreferenceKeys.TIMEZONE, "Europe/Berlin")
        if (obj.has("timezone") && !obj.isNull("timezone") && !timezone.equals(obj.getString("timezone"))) {
            settingsService.setTimeZone(activity, obj.getString("timezone")) {}
            Utils.updateLocale()
        }
        if (obj.has("time") && !obj.isNull("time")) {
            val tTime = Utils.parseDBDateTime(obj.getString("time"))
            Utils.setCal(tTime)
        }
        var updateAllUser = ""
        var loadPermissions = ""
        var loadLanguage = ""
        var rebootTerminal = ""
        var enrollCard = Pair("", "")
        var enrollFinger = Pair("", "")
        val updateIds = arrayListOf<Pair<String, String>>()
        val deleteIds = arrayListOf<Pair<String, String>>()
        val deleteFP = arrayListOf<Pair<String, String>>()
        var lang = Pair("", "")
        var url = ""
        if (!obj.isNull("commands") && obj.getJSONArray("commands").length() > 0) {
            val array = obj.getJSONArray("commands")
            for (i in 0 until array.length()) {
                val cmdObj = array.getJSONObject(i)
                val command = cmdObj.optString("command")
                Log.d("HeartbeatService", command)
                val id = cmdObj.optString("unique")
                if (id.isNotEmpty()) {
                    if (command == "updateAllUser") {
                        updateAllUser = id
                    } else if (command.startsWith("updateUser:")) {
                        updateIds.add(Pair(command.substring(11, command.length), id))
                    } else if (command.startsWith("deleteUser:")) {
                        deleteIds.add(Pair(command.substring(11, command.length), id))
                    } else if (command.startsWith("deleteFP:")) {
                        deleteFP.add(Pair(command.substring(9, command.length), id))
                    } else if (command.startsWith("enrollCard:")) {
                        enrollCard = Pair(command.substring(11, command.length), id)
                    } else if (command.startsWith("enrollFinger:")) {
                        enrollFinger = Pair(command.substring(13, command.length), id)
                    } else if (command == "loadPermissions") {
                        loadPermissions = id
                    } else if (command == "loadLanguage") {
                        loadLanguage = id
                    } else if (command == "rebootTerminal") {
                        rebootTerminal = id
                    } else if (command.startsWith("changeLanguage:")) {
                        lang = Pair(command.substring(15, command.length), id)
                    }
                } else if (command.startsWith("updateUrl:")) {
                    url = command.substring(10, command.length)
                }
            }
        }
        val scope = activity.getViewModel().viewModelScope
        scope.launch {
            if (url.isNotEmpty()) {
                val data = url.split(":;:")
                val editor = sharedPrefService.getEditor()
                if (data[1] != getCompany()) {
                    editor.putString(SharedPreferenceKeys.COMPANY.name, data[1])
                }
                if (data[0] != getURl()) {
                    editor.putString(SharedPreferenceKeys.SERVER_URL.name, data[0])
                }
                editor.apply()
            } else {
                val color = activity.resources?.getColorStateList(R.color.green, null)
                if (activity.getBinding().serverConnectionIcon.imageTintList != color) {
                    activity.runOnUiThread {
                        activity.getBinding().serverConnectionIcon.imageTintList = color
                    }
                }
                if (updateAllUser.isNotEmpty()) {
                    userService.loadUsersFromServer(scope, updateAllUser)
                } else {
                    for (no in deleteFP) {
                        val ids = no.first.split(":")
                        if (ids.size == 2) {
                            userService.deleteFP(ids[0], ids[1].toInt(), no.second)
                        }
                    }
                    for (no in updateIds) {
                        userService.loadUserFromServer(scope, no.first, no.second)
                    }
                    for (no in deleteIds) {
                        userService.deleteUser(no.first, no.second)
                    }
                }
                if (loadPermissions.isNotEmpty()) {
                    loginService.loadPermissions(
                        scope,
                        activity
                    ) { worked -> if (worked) httpService.responseForCommand(loadPermissions) }
                }
                if (loadLanguage.isNotEmpty()) {
                    languageService.requestLanguageFromServer(scope, activity, loadLanguage)
                }
                if (lang.first.isNotEmpty()) {
                    settingsService.changeLanguage(activity, lang.first, lang.second)
                    activity.setText()
                    activity.reloadSoundSource()
                    Utils.updateLocale()
                }
                if (enrollCard.first.isNotEmpty()) {
                    activity.cancelTimer()
                    val ids = enrollCard.first.split(":")
                    if (ids.size == 2) {
                        activity.runOnUiThread {
                            val frag =
                                activity.supportFragmentManager.findFragmentByTag(
                                    MBRemoteRegisterSheet.TAG
                                )
                            if (frag == null || !frag.isVisible) {
                                Handler(Looper.getMainLooper()).postDelayed(3000) {
                                    val sheet =
                                        MBRemoteRegisterSheet.newInstance(
                                            ids[0].substring(1, ids[0].length),
                                            ids[1].substring(1, ids[1].length),
                                            false,
                                            commandId = enrollCard.second
                                        )
                                    sheet.show(
                                        activity.supportFragmentManager,
                                        MBRemoteRegisterSheet.TAG
                                    )
                                }
                            }
                        }
                    }
                }
                if (enrollFinger.first.isNotEmpty()) {
                    val ids = enrollFinger.first.split(":")
                    if (ids.size == 3) {
                        activity.cancelTimer()
                        activity.loadSoundForFP(ids[2].substring(1, ids[2].length).toInt())
                        activity.runOnUiThread {
                            val frag =
                                activity.supportFragmentManager.findFragmentByTag(
                                    MBRemoteRegisterSheet.TAG
                                )
                            if (frag == null || !frag.isVisible) {
                                Handler(Looper.getMainLooper()).postDelayed(3000) {
                                    val sheet = MBRemoteRegisterSheet.newInstance(
                                        ids[0].substring(1, ids[0].length),
                                        ids[1].substring(1, ids[1].length),
                                        true,
                                        ids[2].substring(1, ids[2].length).toInt(),
                                        commandId = enrollFinger.second
                                    )
                                    sheet.show(
                                        activity.supportFragmentManager,
                                        MBRemoteRegisterSheet.TAG
                                    )
                                }
                            }
                        }
                    }
                }
                if (rebootTerminal.isNotEmpty()) {
                    if (loadLanguage.isNotEmpty() || loadPermissions.isNotEmpty() ||
                        updateAllUser.isNotEmpty() || updateIds.size > 0 || deleteIds.size > 0 ||
                        deleteFP.size > 0 || lang.first.isNotEmpty()
                    ) {
                        delay(10000L)
                    }
                    httpService.responseForCommand(rebootTerminal) { _, _, _ ->
                        activity.sendBroadcast(Intent("com.zkteco.android.action.REBOOT"))
                    }
                } else {
                    bookingService.sendSavedBooking(scope)
                }
            }
        }
    }

    fun stopHeartBeat() {
        handler.removeCallbacksAndMessages(null)
    }
}