package com.timo.timoterminal.service

import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import androidx.core.os.postDelayed
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.enums.TerminalCommands
import com.timo.timoterminal.modalBottomSheets.MBRemoteRegisterSheet
import com.timo.timoterminal.utils.Utils
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
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
        return sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
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
        var updateAllUser = Pair("", "")
        var loadPermissions = Pair("", "")
        var loadLanguage = Pair("", "")
        var rebootTerminal = ""
        var enrollCard = Pair("", "")
        var enrollFinger = Pair("", "")
        val updateIds = arrayListOf<Pair<String, String>>()
        val deleteIds = arrayListOf<Pair<String, String>>()
        val deleteFP = arrayListOf<Pair<String, String>>()
        var lang = Pair("", "")
        var url = ""
        var updateAPK = Pair("", "")
        if (!obj.isNull("commands") && obj.getJSONArray("commands").length() > 0) {
            val array = obj.getJSONArray("commands")
            for (i in 0 until array.length()) {
                val cmdObj = array.getJSONObject(i)
                val command = cmdObj.optString("command")
                val type = cmdObj.optInt("type", -1)
                val id = cmdObj.optString("unique")
                if (id.isNotEmpty()) {
                    when (type) {
                        TerminalCommands.COMMAND_UPDATE_ALL_USER.ordinal -> {
                            updateAllUser = Pair(command, id)
                        }

                        TerminalCommands.COMMAND_UPDATE_USER.ordinal -> {
                            updateIds.add(Pair(command, id))
                        }

                        TerminalCommands.COMMAND_DELETE_USER.ordinal -> {
                            deleteIds.add(Pair(command, id))
                        }

                        TerminalCommands.COMMAND_DELETE_FP.ordinal -> {
                            deleteFP.add(Pair(command, id))
                        }

                        TerminalCommands.COMMAND_ENROLL_CARD.ordinal -> {
                            enrollCard = Pair(command, id)
                        }

                        TerminalCommands.COMMAND_ENROLL_FINGER.ordinal -> {
                            enrollFinger = Pair(command, id)
                        }

                        TerminalCommands.COMMAND_LOAD_PERMISSION.ordinal -> {
                            loadPermissions = Pair(command, id)
                        }

                        TerminalCommands.COMMAND_LOAD_LANGUAGE.ordinal -> {
                            loadLanguage = Pair(command, id)
                        }

                        TerminalCommands.COMMAND_REBOOT_TERMINAL.ordinal -> {
                            rebootTerminal = id
                        }

                        TerminalCommands.COMMAND_CHANGE_LANGUAGE.ordinal -> {
                            lang = Pair(command, id)
                        }

                        TerminalCommands.COMMAND_UPDATE_APK.ordinal -> {
                            updateAPK = Pair(command, id)
                        }
                    }
                } else if (type == TerminalCommands.COMMAND_UPDATE_URL.ordinal) {
                    url = command
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
                if (updateAllUser.first.isNotEmpty()) {
                    val resObj = Utils.parseResponseToJSON(updateAllUser.first)
                    userService.processUserArray(
                        resObj.array,
                        scope,
                        updateAllUser.second,
                        deleteOld = true
                    )
                } else {
                    val array = JSONArray()
                    val resIds = JSONArray()
                    for (no in updateIds) {
                        val resObj = Utils.parseResponseToJSON(no.first).obj
                        resIds.put(no.second)
                        if (resObj != null)
                            array.put(resObj)
                    }
                    if (updateIds.size > 0) {
                        httpService.responseForMultiCommand(resIds)
                        userService.processUserArray(array, scope, "")
                    }else if(deleteIds.size > 0) {
                        for (no in deleteIds) {
                            userService.deleteUser(no.first, no.second)
                        }
                    }else if(deleteFP.size > 0) {
                        for (no in deleteFP) {
                            val ids = no.first.split(":")
                            if (ids.size == 2) {
                                userService.deleteFP(ids[0], ids[1].toInt(), no.second)
                            }
                        }
                    }
                }
                if (loadPermissions.first.isNotEmpty()) {
                    val resObj = Utils.parseResponseToJSON(loadPermissions.first)
                    if (resObj.array != null) {
                        httpService.responseForCommand(loadPermissions.second)
                        loginService.processPermissions(
                            resObj.array,
                            scope
                        ) {}
                    }
                }
                if (loadLanguage.first.isNotEmpty()) {
                    languageService.processLanguageResponse(
                        Utils.parseResponseToJSON(loadLanguage.first),
                        scope,
                        loadLanguage.second
                    )
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
                if (updateAPK.first.isNotEmpty()) {
                    httpService.responseForCommand(updateAPK.second) { _, _, _ ->
                        val editor = sharedPrefService.getEditor()
                        editor.putBoolean(SharedPreferenceKeys.HAS_UPDATE.name, true)
                        editor.putString(SharedPreferenceKeys.UPDATE_VERSION.name, updateAPK.first)
                        editor.apply()
                        activity.runOnUiThread {
                            activity.getBinding().terminalHasUpdateButton.visibility = View.VISIBLE
                        }
                    }
                } else {
                    if (rebootTerminal.isNotEmpty()) {
                        if (loadLanguage.first.isNotEmpty() || loadPermissions.second.isNotEmpty() ||
                            updateAllUser.first.isNotEmpty() || updateIds.size > 0 || deleteIds.size > 0 ||
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
    }

    fun stopHeartBeat() {
        handler.removeCallbacksAndMessages(null)
    }
}