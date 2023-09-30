package com.timo.timoterminal.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.timo.timoterminal.utils.classes.ResponseToJSON
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.util.Collections
import java.util.Locale


class Utils {

    companion object {

        fun isOnline(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
            return false
        }

        fun getMACAddress(interfaceName : String) : String {
            try {
                val interfaces :List<NetworkInterface> = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (inf in interfaces) {
                    if (interfaceName.isNotEmpty()) {
                        if (inf.name.lowercase() == interfaceName.lowercase()) continue;
                    }

                    val mac : ByteArray = inf.hardwareAddress
                    if (mac.isEmpty()) return ""
                    val buf : StringBuilder = StringBuilder()
                    for (byte in mac) buf.append(String.format("%02X:", byte))
                    if (buf.length > 0) buf.deleteCharAt(buf.length - 1)
                    return buf.toString()
                }
            } catch (ignored : Exception) {}
            return ""
        }

        fun getIPAddress(useIPv4: Boolean): String {
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress) {
                            val sAddr = addr.hostAddress
                            //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                            val isIPv4 = sAddr.indexOf(':') < 0
                            if (useIPv4) {
                                if (isIPv4) return sAddr
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                    return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                        0,
                                        delim
                                    ).uppercase(
                                        Locale.getDefault()
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (ignored: Exception) {
            } // for now eat exceptions
            return ""
        }

        fun sha256(str: String): ByteArray = MessageDigest.getInstance("SHA-256").digest(str.toByteArray(UTF_8))

        fun parseResponseToJSON(responseString: String) : ResponseToJSON {

            var obj : JSONObject? = null
            var arr : JSONArray? = null
            var str : String? = null

            if (responseString.startsWith("{")) {
                obj = JSONObject(responseString)
            } else if (responseString.startsWith("[")) {
                arr = JSONArray(responseString)
            } else {
                str = responseString
            }

            return ResponseToJSON(obj, arr, str)
        }
    }

}