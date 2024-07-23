package com.timo.timoterminal.service

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.provider.Settings.*
import com.timo.timoterminal.BuildConfig
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.LoginActivity
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.FeedReaderDbHelper
import com.timo.timoterminal.utils.classes.SoundSource
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale


class LoginService(
    private val httpService: HttpService,
    private val configRepository: ConfigRepository,
    private val sharedPrefService: SharedPrefService,
    private val hardware: IHardwareSource,
    private val userService: UserService,
    private val settingsService: SettingsService,
    private val languageService: LanguageService,
    private val bookingService: BookingService
) : KoinComponent {
    private val soundSource: SoundSource by inject()
    private val heartbeatService: HeartbeatService by inject()

    companion object {
        private const val TAG = "LoginService"
    }

    fun loginProcess(
        company: String,
        username: String,
        password: String,
        customUrl: String?,
        context: Context,
        successCallback: (isNewTerminal: Boolean) -> Unit?
    ) {
        if (Utils.isOnline(context)) {
            if (customUrl.isNullOrEmpty()) {
                getURlFromServer(
                    company,
                    context
                ) { url ->
                    loginCompany(
                        company,
                        username,
                        password,
                        url.substring(0, url.length - 1),
                        context,
                        successCallback,
                    )
                }
            } else {
                loginCompany(
                    company,
                    username,
                    password,
                    customUrl,
                    context,
                    successCallback
                )
            }
        } else {

            /*
            If we are offline, we need to check if
            - if a timo_id is existent
            - if a company is saved persistently and equal to inputs
            - if the password and username hash is identical to saved hashs
            (there has to be at least some kind of security)
            - if there is a token (indicator whether it has been validated online at least once)
             */


        }
    }

    private fun getURlFromServer(
        company: String,
        context: Context,
        successCallback: (url: String) -> Unit?,
    ) {
        httpService.get(
            "https://www.timo24.de/timoadmin/baseurl",
            mapOf(Pair("company", company)),
            context,
            { _, _, url ->
                if (!url.isNullOrEmpty()) {
                    successCallback(url)
                }
            }
        )
    }

    @SuppressLint("HardwareIds")
    private fun loginCompany(
        company: String,
        username: String,
        password: String,
        url: String,
        context: Context,
        callback: (isNewTerminal: Boolean) -> Unit?
    ) {

        val language = sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "de") ?: "de"
        //if not created, create a ID for the device. This ID is the recognizer for the timo
        // system and will be used as terminal id
        val hashedAndroidId =
            Utils.sha256(Secure.getString(context.contentResolver, Secure.ANDROID_ID))

        if (!url.startsWith("http")) {
            soundSource.playSound(SoundSource.loginFailed)
            val config = Configuration()
            config.setLocale(Locale(language))
            val nContext = context.createConfigurationContext(config)
            Utils.showErrorMessage(
                context,
                nContext.getText(R.string.company_does_not_exists_or_is_wrong).toString()
            )

            return
        }

        if (Utils.isOnline(context)) {
            val parameters = HashMap<String, String>()
            val timoTerminalId =
                sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
            val ip = Utils.getIPAddress(true)

            parameters["id"] = timoTerminalId.toString()
            parameters["company"] = company
            parameters["password"] = password
            parameters["username"] = username
            parameters["androidId"] = String(hashedAndroidId)
            parameters["device"] = hardware.getDevice().naming
            parameters["brand"] = Build.BRAND
            parameters["version"] = BuildConfig.VERSION_NAME
            parameters["timezone"] =
                sharedPrefService.getString(SharedPreferenceKeys.TIMEZONE, "Europe/Berlin")
                    ?: "Europe/Berlin"
            parameters["language"] = language
            parameters["ip"] = ip
            //Serial number is used as terminalId
            parameters["serialNumber"] = hardware.serialNumber()

            httpService.post(
                "${url}services/rest/zktecoTerminal/loginTerminal", parameters,
                context,
                { res, _, _ ->
                    val payload = res?.getJSONObject("payload")
                    val terminalObj = payload?.getJSONObject("terminal")
                    val isNewTerminal = payload?.getBoolean("isNewTerminal")
                    val id = terminalObj?.getInt("id")
                    val token = terminalObj?.getString("token")

                    if (id != null && !token.isNullOrEmpty()) {
                        //save credentials of terminal
                        sharedPrefService.saveLoginCredentials(
                            String(hashedAndroidId),
                            id,
                            token,
                            url,
                            company,
                            username,
                            password
                        )
                        callback(isNewTerminal!!)
                        soundSource.playSound(SoundSource.loginSuccessful)
                    }
                }, { e, res, rContext, obj ->
                    soundSource.playSound(SoundSource.loginFailed)
                    val config = Configuration()
                    config.setLocale(Locale(language))
                    val nContext = rContext?.createConfigurationContext(config)
                    val errorMessage =
                        if (obj?.obj != null && obj.obj.getString("code") == "-5") {
                            nContext?.getText(R.string.account_locked).toString()
                        } else if (e != null) {
                            nContext?.getText(R.string.timo_service_not_reachable).toString()
                        } else {
                            nContext?.getText(R.string.wrong_login_data).toString()
                        }
                    HttpService.handleGenericRequestError(
                        e,
                        res,
                        rContext,
                        obj,
                        errorMessage
                    )
                }
            )


            //OFFLINE
            //check if permissions, company und users are stored persistently
            //if everything is loaded, directly forward to the main activity

        }
    }

    fun loadPermissions(
        coroutineScope: CoroutineScope,
        context: Context,
        callback: (worked: Boolean) -> Unit
    ) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN, "") ?: ""

        if (Utils.isOnline(context)) {
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty()) {
                httpService.get(
                    "${url}services/rest/zktecoTerminal/permission",
                    mapOf(
                        Pair("firma", company),
                        Pair("terminalSN", hardware.serialNumber()),
                        Pair("terminalId", "$tId"),
                        Pair("token", token)
                    ),
                    context,
                    { _, array, _ ->
                        processPermissions(array, coroutineScope, callback)
                    }, { _, _, _, _ ->
                        //check if permissions table is really populated
                        coroutineScope.launch {
                            val size = configRepository.getItemCount()
                            if (size > 3) callback(true) else callback(false)
                        }
                        null
                    }
                )
            }
        } else {
            //check if permissions table is really populated
            coroutineScope.launch {
                val size = configRepository.getItemCount()
                if (size > 3) callback(true) else callback(false)
            }
        }
    }

    fun processPermissions(
        array: JSONArray?,
        coroutineScope: CoroutineScope,
        callback: (worked: Boolean) -> Unit
    ) {
        if (array != null && array.length() > 0) {
            val list = ArrayList<ConfigEntity>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ConfigEntity(
                        ConfigRepository.TYPE_PERMISSION,
                        obj.getString("name"),
                        obj.getString("value")
                    )
                )
            }
            coroutineScope.launch {
                insertOrUpdateConfigEntities(list)
                configRepository.initMap()
                callback(true)
            }
        } else {
            callback(false)
        }
    }

    private fun addConfig(scope: CoroutineScope, config: ConfigEntity) {
        scope.launch {
            configRepository.insertConfigEntity(config)
        }
    }

    fun autoLogin(context: Context, callback: () -> Unit) {
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val tId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        validateLogin(company, token, context, url, tId, callback)
    }

    private fun validateLogin(
        company: String?,
        token: String?,
        context: Context?,
        url: String?,
        tId: Int,
        callback: () -> Unit
    ) {
        val serverUrl =
            if (!url.isNullOrEmpty()) url else sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        if (!serverUrl.isNullOrEmpty()) {
            val parameterMap = HashMap<String, String>()
            parameterMap["company"] = company!!
            parameterMap["token"] = token!!
            parameterMap["terminalSN"] = hardware.serialNumber()
            parameterMap["terminalId"] = tId.toString()
            httpService.post(
                "${url}services/rest/zktecoTerminal/validateLogin",
                parameterMap,
                context,
                { res, _, _ ->
                    val payload = res?.getJSONObject("payload")
                    val terminalObj = payload?.getJSONObject("terminal")
                    val id = terminalObj?.getInt("id")
                    val nToken = terminalObj?.getString("token")
                    val language = terminalObj?.getString("language")
                    if (id != null && !nToken.isNullOrEmpty()) {
                        //save credentials of terminal
                        sharedPrefService.updateToken(nToken)
                        if (!language.isNullOrEmpty()) {
                            settingsService.changeLanguage(context, language)
                        }
                        callback()
                    }
                },
                { _, _, _, _ ->
                    callback()
                }
            )
        }
    }

    fun logout(context: Context) {
        sharedPrefService.removeAllCreds()
        val logoutIntent = Intent(context, LoginActivity::class.java)
        logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(logoutIntent)
    }

    fun resetTerminal(context: Context, coroutineScope: CoroutineScope) {
        userService.deleteAllUsers(coroutineScope)
        languageService.deleteAll(coroutineScope)
        coroutineScope.launch {
            configRepository.deleteAll()
        }
        bookingService.deleteAll(coroutineScope)
        heartbeatService.stopHeartBeat()
        val helperDB = FeedReaderDbHelper(context, "room.db").writableDatabase
        helperDB.execSQL("CREATE TABLE dummy (id INTEGER PRIMARY KEY AUTOINCREMENT);")
        helperDB.execSQL("DROP TABLE dummy;")
        helperDB.execSQL("DELETE FROM sqlite_sequence WHERE name='ConfigEntity';")
        helperDB.execSQL("DELETE FROM sqlite_sequence WHERE name='LanguageEntity';")
        helperDB.execSQL("DELETE FROM sqlite_sequence WHERE name='UserEntity';")
        helperDB.execSQL("DELETE FROM sqlite_sequence WHERE name='BookingBUEntity';")
        helperDB.execSQL("DELETE FROM sqlite_sequence WHERE name='BookingEntity';")
        logout(context)
    }

    private suspend fun insertOrUpdateConfigEntities(list: ArrayList<ConfigEntity>) {
        val current = configRepository.getAllAsList()
        for (element in list) {
            var saved = false
            for (entity in current) {
                if (entity == element) {
                    element.id = entity.id
                    configRepository.updateConfigEntity(element)
                    saved = true
                }
            }
            if (saved) continue
            configRepository.insertConfigEntity(element)
        }
    }
}