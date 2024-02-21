package com.timo.timoterminal.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.fragment.app.FragmentManager
import com.timo.timoterminal.R
import com.timo.timoterminal.modalBottomSheets.MBMessageSheet
import com.timo.timoterminal.utils.classes.ResponseToJSON
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Collections
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale


class Utils {

    companion object {
        private var dayFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private var timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        private var dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        private var dateNameFormatter = SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault())
        private var databaseFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

        private val handlerThread = HandlerThread("backgroundTimerThread")
        private val calendar = object : GregorianCalendar() {}
        private var handler: Handler? = null

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

        fun getMACAddress(interfaceName: String): String {
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (inf in interfaces) {
                    if (interfaceName.isNotEmpty()) {
                        if (inf.name.lowercase() == interfaceName.lowercase()) continue
                    }

                    val mac: ByteArray = inf.hardwareAddress
                    if (mac.isEmpty()) return ""
                    val buf: StringBuilder = StringBuilder()
                    for (byte in mac) buf.append(String.format("%02X:", byte))
                    if (buf.isNotEmpty()) buf.deleteCharAt(buf.length - 1)
                    return buf.toString()
                }
            } catch (ignored: Exception) {
            }
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

        fun sha256(str: String): ByteArray =
            MessageDigest.getInstance("SHA-256").digest(str.toByteArray(UTF_8))

        fun parseResponseToJSON(responseString: String): ResponseToJSON {

            var obj: JSONObject? = null
            var arr: JSONArray? = null
            var str: String? = null

            if (responseString.startsWith("{")) {
                obj = JSONObject(responseString)
            } else if (responseString.startsWith("[")) {
                arr = JSONArray(responseString)
            } else {
                str = responseString
            }

            return ResponseToJSON(obj, arr, str)
        }

        fun hideNavInDialog(dialog: Dialog?) {
            if (dialog != null) {
                dialog.window?.decorView?.systemUiVisibility =
                    (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN)
                dialog.actionBar?.hide()
                //Clear the not focusable flag from the window
                dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            }
        }

        fun hideStatusAndNavbar(activity: Activity) {
            activity.window.navigationBarColor = activity.resources.getColor(R.color.white, null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                activity.window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity.window.setDecorFitsSystemWindows(false)
                activity.window.insetsController?.apply {
                    hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                activity.window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_LOW_PROFILE
                        )
            }
            activity.actionBar?.hide()
        }

        fun getTimeFromGC(gc: Calendar): String {
            return timeFormatter.format(gc.time)
        }

        fun getDateTimeFromGC(gc: Calendar): String {
            return dateTimeFormatter.format(gc.time)
        }

        fun getDateWithNameFromGC(gc: Calendar): String {
            return dateNameFormatter.format(gc.time)
        }

        fun parseToDBDate(date: String): String {
            return databaseFormatter.format(dateTimeFormatter.parse(date) ?: Date())
        }

        fun parseFromDBDate(date: String): String {
            return dateTimeFormatter.format(databaseFormatter.parse(date) ?: Date())
        }

        fun parseDateTime(date: String): GregorianCalendar {
            val greg = getCal()
            val pDate = dateTimeFormatter.parse(date)
            if (pDate != null)
                greg.time = pDate
            return greg
        }

        fun parseDBDateTime(date: String): GregorianCalendar {
            val greg = getCal()
            val pDate = databaseFormatter.parse(date)
            if (pDate != null)
                greg.time = pDate
            return greg
        }

        fun getDateFromTimestamp(date: Long): String {
            return dayFormatter.format(Date(date))
        }

        fun convertTime(zeit: Double): String {
            if (zeit == 0.toDouble()) {
                return "00:00"
            }

            val sign = ""
            val min = zeit.toInt() % 60
            val hrs = (zeit.toInt() - min) / 60

            return getTimeString(hrs, sign, min)
        }

        private fun getTimeString(hrs: Int, sign: String, min: Int): String {
            var hrs1 = hrs
            var sign1 = sign
            var min1 = min
            if (hrs1 < 0) {
                hrs1 *= -1
                sign1 = "-"
            }
            if (min1 < 0) {
                min1 *= -1
                sign1 = "-"
            }

            return sign1 + (if (hrs1 < 10) "0$hrs1" else "$hrs1") + ":" + (if (min1 < 10) "0$min1" else "$min1")
        }

        fun getOffset(offsetInMilliSec: Int): String {
            val sec = offsetInMilliSec / 1000
            val min = sec / 60
            val minOff = min % 60
            val hrs = (min - minOff) / 60

            val sign = "+"

            return getTimeString(hrs, sign, minOff)
        }

        fun showMessage(fragMan: FragmentManager, message: String): MBMessageSheet {
            val sheet = MBMessageSheet()
            val bundle = Bundle()
            bundle.putString("message", message)
            sheet.arguments = bundle
            sheet.show(fragMan, MBMessageSheet.TAG)

            return sheet
        }

        fun updateLocale() {
            dayFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            dateNameFormatter = SimpleDateFormat("EE, dd.MM.yyyy", Locale.getDefault())
            databaseFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        }

        private fun secTimer() {
            if (!handlerThread.isAlive) handlerThread.start()
            handler?.removeCallbacksAndMessages(null)
            handler = Handler(handlerThread.looper)
            var runnable: Runnable? = null

            runnable = Runnable {
                handler!!.postDelayed(runnable!!, 998L)
                calendar.add(Calendar.SECOND, 1)
            }
            handler!!.postDelayed(runnable, 998L)
            calendar.add(Calendar.SECOND, 1)
        }

        fun setCal(cal: GregorianCalendar) {
            calendar.time = cal.time
            secTimer()
        }

        fun getCal() = calendar
    }

}