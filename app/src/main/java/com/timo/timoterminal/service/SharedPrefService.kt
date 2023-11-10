package com.timo.timoterminal.service

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.utils.Constants
import com.timo.timoterminal.utils.Utils
import org.koin.core.component.KoinComponent
import android.content.ContentResolver
import android.provider.Settings.Secure
import java.util.UUID


//This service makes sure that every key shared preferences is using is documented. This is why I created a service.
// DO NOT USE context.sharedPreferences, this way we it is difficult to detect what keys are used or not
class SharedPrefService(context: Context) : KoinComponent {

    public var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.SHARED_PREFERENCE_KEY, Context.MODE_PRIVATE)

    fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }


    fun getString(p0: SharedPreferenceKeys, p1: String? = null): String? {
        return sharedPreferences.getString(p0.name, p1)
    }

    fun getStringSet(
        p0: SharedPreferenceKeys,
        p1: MutableSet<String>? = null
    ): MutableSet<String>? {
        return sharedPreferences.getStringSet(p0.name, p1)
    }

    fun getInt(p0: SharedPreferenceKeys, p1: Int): Int {
        return sharedPreferences.getInt(p0.name, p1)
    }

    fun getLong(p0: SharedPreferenceKeys, p1: Long): Long {
        return sharedPreferences.getLong(p0.name, p1)
    }

    fun getFloat(p0: SharedPreferenceKeys, p1: Float): Float {
        return sharedPreferences.getFloat(p0.name, p1)
    }

    fun getBoolean(p0: SharedPreferenceKeys, p1: Boolean): Boolean {
        return sharedPreferences.getBoolean(p0.name, p1)
    }

    fun contains(p0: SharedPreferenceKeys): Boolean {
        return sharedPreferences.contains(p0.name)
    }

    fun getDeviceUUID() : String {
        return getString(SharedPreferenceKeys.DEVICE_UUID, null)!!
    }

    fun saveLoginCredentials(
        terminalUUID: String,
        id: Int,
        token: String,
        url: String,
        company: String,
        username: String,
        password: String,
        language: String
    ) {
        val editor = getEditor()
        editor.putString(SharedPreferenceKeys.DEVICE_UUID.name, terminalUUID);
        editor.putInt(SharedPreferenceKeys.TIMO_TERMINAL_ID.name, id)
        editor.putString(SharedPreferenceKeys.TOKEN.name, token)
        editor.putString(SharedPreferenceKeys.COMPANY.name, company)
        editor.putString(SharedPreferenceKeys.SERVER_URL.name, url)
        editor.putString(SharedPreferenceKeys.USER.name, String(Utils.sha256(username)))
        editor.putString(SharedPreferenceKeys.PASSWORD.name, String(Utils.sha256(password)))
        editor.putString(SharedPreferenceKeys.LANGUAGE.name, language)
        editor.apply()
    }

    fun checkIfCredsAreSaved(): Boolean {
        val UUID = getString(SharedPreferenceKeys.DEVICE_UUID);
        val T_ID = getInt(SharedPreferenceKeys.TIMO_TERMINAL_ID, -1)
        val TOKEN = getString(SharedPreferenceKeys.TOKEN)
        val COMPANY = getString(SharedPreferenceKeys.COMPANY)
        val URL = getString(SharedPreferenceKeys.SERVER_URL)
        val USERHASH = getString(SharedPreferenceKeys.USER)
        val PWHASH = getString(SharedPreferenceKeys.PASSWORD)
        return !UUID.isNullOrEmpty() && T_ID != -1 && !TOKEN.isNullOrEmpty() &&
                !COMPANY.isNullOrEmpty() && !URL.isNullOrEmpty() &&
                !USERHASH.isNullOrEmpty() && !PWHASH.isNullOrEmpty()

    }

    fun removeAllCreds() {
        val editor = getEditor()
        editor.clear()
        editor.apply()
    }

    fun removeField(p0: SharedPreferenceKeys) {
        getEditor().remove(p0.name).commit()
    }

    fun updateToken(token: String) {
        val editor = getEditor()
        editor.putString(SharedPreferenceKeys.TOKEN.name, token)
        editor.apply()
    }

}