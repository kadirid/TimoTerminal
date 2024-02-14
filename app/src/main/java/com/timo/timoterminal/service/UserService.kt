package com.timo.timoterminal.service

import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.fragmentViews.UserSettingsFragment
import com.timo.timoterminal.modalBottomSheets.MBUserWaitSheet
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.utils.Utils
import com.zkteco.android.core.sdk.service.FingerprintService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.koin.core.component.KoinComponent

class UserService(
    private val httpService: HttpService,
    private val sharedPrefService: SharedPrefService,
    private val userRepository: UserRepository,
    private val languageService: LanguageService
) : KoinComponent {

    fun loadUsersFromServer(scope: CoroutineScope, unique: String = "") {
        scope.launch {
            //Load from server
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            val params = HashMap<String, String>()
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                params["company"] = company
                params["token"] = token
                params["terminalId"] = terminalId.toString()
                httpService.get("${url}services/rest/zktecoTerminal/loadAllUsers",
                    params,
                    null,
                    { _, arrResponse, _ ->
                        //Save it persistently offline
                        loadIntoDB(arrResponse, scope)
                        getFPFromServer(scope)
                        if(unique.isNotEmpty()){
                            httpService.responseForCommand(unique)
                        }
                    })
            }
        }
    }

    private fun loadIntoDB(arrResponse: JSONArray?, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            val users = ArrayList<UserEntity>()
            if (arrResponse!!.length() > 0) {
                userRepository.deleteAll()
                FingerprintService.clear()
            }
            for (i in 0 until arrResponse.length()) {
                val obj = arrResponse.getJSONObject(i)
                val userEntity: UserEntity = UserEntity.parseJsonToUserEntity(obj)
                users.add(userEntity)
            }
            userRepository.insertUserEntity(users)
        }
    }

    fun deleteAllUsers(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            userRepository.deleteAll()
            FingerprintService.clear()
        }
    }

    fun sendUpdateRequestSheet(
        paramMap: HashMap<String, String>,
        sheet: MBUserWaitSheet,
        scope: CoroutineScope
    ) {
        scope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                paramMap["company"] = company
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                withContext(Dispatchers.IO) {
                    httpService.post("${url}services/rest/zktecoTerminal/updateUserCard",
                        paramMap,
                        sheet.requireContext(),
                        { obj, _, _ ->
                            if (obj != null) {
                                if (obj.getBoolean("success")) {
                                    val userEntity: UserEntity =
                                        UserEntity.parseJsonToUserEntity(obj)
                                    scope.launch(Dispatchers.IO) {
                                        userRepository.insertOne(userEntity)
                                    }
                                }
                                sheet.afterUpdate(
                                    obj.getBoolean("success"),
                                    obj.optString("message", "")
                                )
                            }
                            sheet.hideLoadMask()
                        }, { e, res, context, output ->
                            sheet.hideLoadMask()
                            HttpService.handleGenericRequestError(
                                e,
                                res,
                                context,
                                output,
                                languageService.getText("#TimoServiceNotReachable") + " " +
                                        languageService.getText("#ChangesReverted")
                            )
                        }
                    )
                }
            }
        }
    }

    fun sendUpdateRequestFragment(
        paramMap: HashMap<String, String>,
        fragment: UserSettingsFragment,
        scope: CoroutineScope
    ) {
        scope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                paramMap["company"] = company
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                withContext(Dispatchers.IO) {
                    httpService.post("${url}services/rest/zktecoTerminal/updateUserPIN",
                        paramMap,
                        fragment.requireContext(),
                        { obj, _, _ ->
                            if (obj != null) {
                                if (obj.getBoolean("success")) {
                                    val userEntity: UserEntity =
                                        UserEntity.parseJsonToUserEntity(obj)
                                    scope.launch(Dispatchers.IO) {
                                        userRepository.insertOne(userEntity)
                                    }
                                }
                                fragment.activity?.runOnUiThread {
                                    Utils.showMessage(
                                        fragment.parentFragmentManager,
                                        obj.optString("message", "")
                                    )
                                }
                            }
                            (fragment.activity as MainActivity?)?.hideLoadMask()
                        }, { e, res, context, output ->
                            (fragment.activity as MainActivity?)?.hideLoadMask()
                            HttpService.handleGenericRequestError(
                                e,
                                res,
                                context,
                                output,
                                languageService.getText("#TimoServiceNotReachable") + " " +
                                        languageService.getText("#ChangesReverted")
                            )
                        }
                    )
                }
            }
        }
    }

    fun saveFPonServer(
        id: String?,
        finger: Int,
        template: String,
        viewModelScope: CoroutineScope,
        sheet: MBUserWaitSheet,
        callback: () -> Any?
    ) {
        if (id != null) {
            viewModelScope.launch {
                var user = "-1"
                val users = userRepository.getEntity(id.toLong())
                if (users.isNotEmpty()) {
                    user = users[0].card.ifEmpty { id }
                }

                val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
                val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
                val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
                val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

                if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty() && user != "-1") {
                    val paramMap = mutableMapOf(Pair("fp", template))
                    paramMap["user"] = user
                    paramMap["fingerNo"] = "$finger"
                    paramMap["company"] = company
                    paramMap["token"] = token
                    paramMap["terminalId"] = terminalId.toString()
                    httpService.post(
                        "${url}services/rest/zktecoTerminal/saveFP",
                        paramMap,
                        sheet.requireContext(),
                        { _, _, _ ->
                            callback()
                            sheet.hideLoadMask()
                        }, { e, response, context, output ->
                            HttpService.handleGenericRequestError(
                                e,
                                response,
                                context,
                                output,
                                languageService.getText("#TimoServiceNotReachable") + " " +
                                        languageService.getText("#ChangesReverted")
                            )
                            FingerprintService.delete("$user|$finger")
                            sheet.hideLoadMask()
                        }
                    )
                }
            }
        }
    }

    private fun getFPFromServer(viewModelScope: CoroutineScope) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                val paramMap = mutableMapOf(Pair("company", company))
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                httpService.get(
                    "${url}services/rest/zktecoTerminal/getAllFPForTerminal",
                    paramMap,
                    null,
                    { _, arr, _ ->
                        if (arr != null && arr.length() > 0) {
                            FingerprintService.clear()
                        }
                        processFPArray(arr)
                    }
                )
            }
        }
    }

    fun assignUser(id: String, callback: () -> Unit?, viewModelScope: CoroutineScope) {
        viewModelScope.launch {
            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
                val paramMap = mutableMapOf(Pair("company", company))
                paramMap["user"] = id
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                httpService.post(
                    "${url}services/rest/zktecoTerminal/assignUser",
                    paramMap,
                    null,
                    { _, _, str ->
                        if ("true" == str) {
                            viewModelScope.launch {
                                val user = userRepository.getEntity(id.toLong())[0]
                                user.assignedToTerminal = true
                                userRepository.insertOne(user)
                                getFPForUser(url, callback, id, company, terminalId, token)
                            }
                        }
                    }, { e, res, context, output ->
                        HttpService.handleGenericRequestError(
                            e,
                            res,
                            context,
                            output,
                            languageService.getText("#TimoServiceNotReachable") + " " +
                                    languageService.getText("#ChangesReverted")
                        )
                    }
                )
            }
        }
    }

    private fun getFPForUser(
        url: String?,
        callback: () -> Unit?,
        id: String,
        company: String,
        terminalId: Int,
        token: String
    ) {
        val paramMap = mutableMapOf(Pair("company", company))
        paramMap["user"] = id
        paramMap["token"] = token
        paramMap["terminalId"] = terminalId.toString()
        httpService.get(
            "${url}services/rest/zktecoTerminal/getFP",
            paramMap,
            null,
            { _, arr, _ ->
                processFPArray(arr)
                callback()
            }
        )
    }

    fun loadUserFromServer(scope: CoroutineScope, userId: String, unique: String = "") {
        //Load from server
        val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
        val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
        val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
        val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)
        val params = HashMap<String, String>()
        if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty()) {
            params["company"] = company
            params["token"] = token
            params["terminalId"] = terminalId.toString()
            params["userId"] = userId
            httpService.get("${url}services/rest/zktecoTerminal/loadUser",
                params,
                null,
                { obj, _, _ ->
                    //Save it persistently offline
                    if (obj != null) {
                        val userEntity: UserEntity = UserEntity.parseJsonToUserEntity(obj)
                        scope.launch {
                            userRepository.insertOne(userEntity)
                            if (userEntity.assignedToTerminal) {
                                getFPForUser(url, { }, userId, company, terminalId, token)
                                if (unique.isNotEmpty()) {
                                    httpService.responseForCommand(unique)
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    suspend fun deleteUser(userId: String, unique: String = "") {
        val list = userRepository.getEntity(userId.toLong())
        if (list.isNotEmpty()) {
            userRepository.delete(list[0])
            deleteFPForUser(userId)
            if (unique.isNotEmpty()) {
                httpService.responseForCommand(unique)
            }
        }
    }

    fun deleteFPForUser(userId: String, unique: String = "") {
        FingerprintService.delete("$userId|0")
        FingerprintService.delete("$userId|1")
        FingerprintService.delete("$userId|2")
        FingerprintService.delete("$userId|3")
        FingerprintService.delete("$userId|4")
        FingerprintService.delete("$userId|5")
        FingerprintService.delete("$userId|6")
        FingerprintService.delete("$userId|7")
        FingerprintService.delete("$userId|8")
        FingerprintService.delete("$userId|9")
        if (unique.isNotEmpty()) {
            httpService.responseForCommand(unique)
        }
    }

    private fun processFPArray(arr: JSONArray?) {
        if (arr != null && arr.length() > 0) {
            for (c in 0 until arr.length()) {
                val obj = arr.getJSONObject(c)
                val user = obj.optString("user", "")
                if (user.isNotEmpty()) {
                    val names = obj.names()
                    if (names != null) {
                        for (i in 0 until names.length()) {
                            val name = names.getString(i)
                            if ("user" != name) {
                                val template = obj.getString(name)
                                val finger =
                                    (name.substring(name.length - 1)).toInt()
                                saveFPinDB(template, finger, user)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveFPinDB(template: String, finger: Int, id: String) {
        val key = "$id|$finger"
        FingerprintService.getTemplate(key)?.let {//Already registered
            return
        }
        FingerprintService.load(key, template)
    }

    fun deleteFP(
        id: String,
        finger: Int,
        viewModelScope: CoroutineScope,
        sheet: MBUserWaitSheet,
        callback: () -> Any?
    ) {
        viewModelScope.launch {
            var user = "-1"
            val users = userRepository.getEntity(id.toLong())
            if (users.isNotEmpty()) {
                user = users[0].card.ifEmpty { id }
            }

            val url = sharedPrefService.getString(SharedPreferenceKeys.SERVER_URL)
            val company = sharedPrefService.getString(SharedPreferenceKeys.COMPANY)
            val terminalId = sharedPrefService.getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, 0)
            val token = sharedPrefService.getString(SharedPreferenceKeys.TOKEN)

            if (!url.isNullOrEmpty() && !company.isNullOrEmpty() && !token.isNullOrEmpty() && user != "-1") {
                val paramMap = mutableMapOf(Pair("user", user))
                paramMap["fingerNo"] = "$finger"
                paramMap["company"] = company
                paramMap["token"] = token
                paramMap["terminalId"] = terminalId.toString()
                httpService.post(
                    "${url}services/rest/zktecoTerminal/delFP",
                    paramMap,
                    sheet.requireContext(),
                    { _, _, _ ->
                        sheet.hideLoadMask()
                        callback()
                        FingerprintService.delete("$id|$finger")
                    }, { e, response, context, output ->
                        sheet.hideLoadMask()
                        HttpService.handleGenericRequestError(
                            e,
                            response,
                            context,
                            output,
                            languageService.getText("#TimoServiceNotReachable") +
                                    " " + languageService.getText("#ChangesReverted")
                        )
                    }
                )
            }
        }
    }

}