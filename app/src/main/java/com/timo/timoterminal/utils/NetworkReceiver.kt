package com.timo.timoterminal.utils

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import com.timo.timoterminal.enums.NetworkType

class NetworkChangeReceiver(private val callback : NetworkStatusCallback) : BroadcastReceiver() {

    interface NetworkStatusCallback {
        fun onNetworkChanged(networkType: NetworkType)
    }
    override fun onReceive(p0: Context, intent: Intent) {
        if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            // Handle network connectivity change here.
            // You can check the network type using the method mentioned in the previous response.
            // Example: val networkType = getNetworkType(context)

            // You can also send a broadcast or update your UI to reflect the change.
            val networkType = getNetworkType(p0)

            // Call the callback method in your activity
            callback.onNetworkChanged(networkType)
        }

    }

    private fun getNetworkType(context: Context): NetworkType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check for network connectivity
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                return NetworkType.WIFI
            } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                return NetworkType.ETHERNET
            } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_PHONE_STATE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    when (telephonyManager.networkType) {
                        TelephonyManager.NETWORK_TYPE_UNKNOWN -> return NetworkType.UNKNOWN
                        TelephonyManager.NETWORK_TYPE_GPRS,
                        TelephonyManager.NETWORK_TYPE_EDGE,
                        TelephonyManager.NETWORK_TYPE_CDMA,
                        TelephonyManager.NETWORK_TYPE_1xRTT,
                        TelephonyManager.NETWORK_TYPE_IDEN -> return NetworkType.SECOND_GEN
                        TelephonyManager.NETWORK_TYPE_UMTS,
                        TelephonyManager.NETWORK_TYPE_EVDO_0,
                        TelephonyManager.NETWORK_TYPE_EVDO_A,
                        TelephonyManager.NETWORK_TYPE_HSDPA,
                        TelephonyManager.NETWORK_TYPE_HSUPA,
                        TelephonyManager.NETWORK_TYPE_HSPA,
                        TelephonyManager.NETWORK_TYPE_EVDO_B,
                        TelephonyManager.NETWORK_TYPE_EHRPD,
                        TelephonyManager.NETWORK_TYPE_HSPAP -> return NetworkType.THIRD_GEN
                        TelephonyManager.NETWORK_TYPE_LTE -> return NetworkType.LTE

                        else -> return NetworkType.UNKNOWN
                    }
                }
            }
        }

        return NetworkType.NOT_CONNECTED
    }
}