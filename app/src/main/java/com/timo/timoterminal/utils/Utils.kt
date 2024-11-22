package com.timo.timoterminal.utils

import android.app.Activity
import android.app.Dialog
import android.app.DownloadManager
import android.app.DownloadManager.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
import kotlin.math.abs


class Utils {

    companion object {
        private var dayFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private var timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        private var dateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        private var dateNameFormatter = SimpleDateFormat("EE dd.MM.yyyy", Locale.getDefault())
        private var databaseFormatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

        private val handlerThread = HandlerThread("backgroundTimerThread")
        private val calendar = GregorianCalendar()
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

        fun getCurrentNetworkConnectionType(context: Context): Int {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val activeNetwork = connectivityManager.activeNetwork ?: return -1
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return -1

            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkCapabilities.TRANSPORT_WIFI
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkCapabilities.TRANSPORT_CELLULAR
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkCapabilities.TRANSPORT_ETHERNET
                else -> -1
            }
        }

        fun getMACAddress(interfaceName: String): String {
            try {
                val interfaces: List<NetworkInterface> =
                    Collections.list(NetworkInterface.getNetworkInterfaces())
                for (inf in interfaces) {
                    if (interfaceName.isNotEmpty()) {
                        if (inf.name.lowercase() != interfaceName.lowercase()) continue
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
                            val isIPv4 = (sAddr?.indexOf(':') ?: -1) < 0
                            if (useIPv4) {
                                if (isIPv4) return sAddr!!
                            } else {
                                if (!isIPv4) {
                                    val delim = sAddr!!.indexOf('%')// drop ip6 zone suffix
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

        fun getTimeFromGC(gc: GregorianCalendar): String {
            return timeFormatter.format(gc.time)
        }

        fun getDateTimeFromGC(gc: GregorianCalendar): String {
            return dateTimeFormatter.format(gc.time)
        }

        fun getDateWithNameFromGC(gc: GregorianCalendar): String {
            return dateNameFormatter.format(gc.time)
        }

        fun parseDateTime(date: String): GregorianCalendar {
            val greg = GregorianCalendar()
            val pDate = dateTimeFormatter.parse(date)
            if (pDate != null)
                greg.time = pDate
            return greg
        }

        fun parseDBDateTime(date: String): GregorianCalendar {
            val greg = GregorianCalendar()
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

            return getTimeString(hrs, "+", minOff)
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

        fun getCal() = calendar.clone() as GregorianCalendar

        fun showErrorMessage(context: Context, msg: String, isWarning: Boolean = true) {
            Handler(context.mainLooper).post {
                val dialog = MaterialAlertDialogBuilder(context, R.style.MySingleButtonDialog)
                if(isWarning) {
                    dialog.setTitle(context.getString(R.string.warning))
                    dialog.setIcon(
                        AppCompatResources.getDrawable(
                            context, R.drawable.baseline_warning_24
                        )
                    )
                }else{
                    dialog.setTitle(context.getString(R.string.info))
                    dialog.setIcon(
                        AppCompatResources.getDrawable(
                            context, R.drawable.baseline_info_24
                        )
                    )
                }
                dialog.setMessage(msg)
                dialog.setPositiveButton("OK") { dia, _ ->
                    dia.dismiss()
                }
                val dia = dialog.create()
                hideNavInDialog(dia)
                dia.setOnShowListener {
                    val textView = dia.findViewById<TextView>(android.R.id.message)
                    textView?.textSize = 30f

                    val imageView = dia.findViewById<ImageView>(android.R.id.icon)
                    val params = imageView?.layoutParams
                    params?.height = 48
                    params?.width = 48
                    imageView?.layoutParams = params
                }
                dia.show()
                val positiveButton: Button = dia.getButton(AlertDialog.BUTTON_POSITIVE)

                val parent = positiveButton.parent as LinearLayout
                parent.gravity = Gravity.CENTER_HORIZONTAL
                val leftSpacer = parent.getChildAt(1)
                leftSpacer.visibility = View.GONE
            }
        }

        /**
         * Parses HH:mm time to minutes
         */
        fun parseTimeToMinutes(time: String, parseFormat: String): Int {
            val format = SimpleDateFormat(parseFormat, Locale.getDefault())
            val date = format.parse(time)
            if (date != null) {
                val hours = date.hours
                val minutes = date.minutes
                return hours * 60 + minutes
            }
            return 0
        }

        fun getTimeInMilliseconds(date: Date): Long {
            val calendar = Calendar.getInstance().apply {
                time = date
            }

            val hours = calendar.get(Calendar.HOUR_OF_DAY)
            val minutes = calendar.get(Calendar.MINUTE)

            return (hours * 60 * 60 * 1000 + minutes * 60 * 1000).toLong()
        }

        fun isToday(date: Date): Boolean {
            val today = Calendar.getInstance()
            val specifiedDate = Calendar.getInstance().apply {
                time = date
            }

            return today.get(Calendar.YEAR) == specifiedDate.get(Calendar.YEAR) &&
                    today.get(Calendar.MONTH) == specifiedDate.get(Calendar.MONTH) &&
                    today.get(Calendar.DAY_OF_MONTH) == specifiedDate.get(Calendar.DAY_OF_MONTH)
        }

        fun isSameDay(date1: Date, date2: Date): Boolean {
            val cal1 = Calendar.getInstance().apply {
                time = date1
            }
            val cal2 = Calendar.getInstance().apply {
                time = date2
            }

            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                    cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
        }

        fun daysBetween(date1: Date, date2: Date): Int {
            val cal1 = Calendar.getInstance().apply {
                time = date1
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val cal2 = Calendar.getInstance().apply {
                time = date2
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val msDiff = cal2.timeInMillis - cal1.timeInMillis
            val daysDiff = abs(msDiff / (24 * 60 * 60 * 1000))
            return daysDiff.toInt()
        }

        fun downloadAndInstallApk(context: Context, activity: Activity, apkUrl: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    // Guide the user to enable "Unknown sources" in device settings
                    return
                }
            }

            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.REQUEST_INSTALL_PACKAGES
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return
            }
            val request = Request(Uri.parse(apkUrl))
                .setTitle("APK Download")
                .setDescription("Downloading APK file.")
                .setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk")

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onDownloadComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val downloadedFileId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
                    if (downloadedFileId == downloadId) {
                        val downloadedFileUri =
                            downloadManager.getUriForDownloadedFile(downloadedFileId)
                        installApk(context, downloadedFileUri)
                    }
                }
            }

            context.registerReceiver(onDownloadComplete, IntentFilter(ACTION_DOWNLOAD_COMPLETE))
        }

        fun installApk(context: Context, apkUri: Uri) {
            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
            intent.data = apkUri
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.startActivity(intent)
        }

        // takes hh or hh:mm or hh:mm:ss
        fun parseTimeToFloat(time: String): Float {
            val parts = time.split(":")
            return when (parts.size) {
                1 -> parts[0].toFloat() * 60
                2 -> (parts[0].toFloat() * 60) + parts[1].toFloat()
                3 -> (parts[0].toFloat() * 60) + parts[1].toFloat() + if (parts[2].toInt() > 29) 1 else 0
                else -> 0f
            }
        }

        fun hexStringToByteArray(hexString: String): ByteArray {
            return hexString.chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()
        }

        fun byteArrayToHex(byteArray: ByteArray): String {
            return StringBuilder().apply {
                byteArray.forEach { byte ->
                    append(String.format("%02x", byte.toInt() and 0xFF))
                }
            }.toString()
        }
    }
}