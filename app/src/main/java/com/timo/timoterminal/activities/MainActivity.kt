package com.timo.timoterminal.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.postDelayed
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityMainBinding
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.NetworkType
import com.timo.timoterminal.fragmentViews.AbsenceFragment
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.fragmentViews.InfoFragment
import com.timo.timoterminal.fragmentViews.ProjectFragment
import com.timo.timoterminal.fragmentViews.SettingsFragment
import com.timo.timoterminal.modalBottomSheets.MBLoginWelcomeSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.BatteryReceiver
import com.timo.timoterminal.utils.NetworkChangeReceiver
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.GregorianCalendar
import java.util.Timer
import kotlin.concurrent.schedule


class MainActivity : AppCompatActivity(), BatteryReceiver.BatteryStatusCallback,
    NetworkChangeReceiver.NetworkStatusCallback {

    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val languageService: LanguageService by inject()
    private val userService: UserService by inject()
    private val soundSource: SoundSource by inject()

    private lateinit var binding: ActivityMainBinding
    private lateinit var batteryReceiver: BatteryReceiver
    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var mbLoginWelcomeSheet: MBLoginWelcomeSheet
    private var alertTimer: Timer? = null
    private var dialog: AlertDialog? = null
    private var isInit: Boolean = false

    companion object {
        const val TAG = "MainActivity"
    }

    private var timer = Timer("showAttendanceFragment", false)

    fun restartTimer() {
        timer.cancel()
        timer = Timer("showAttendanceFragment", false)
        timer.schedule(10000L) {
            showAttendanceFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isNewTerminal = intent.getBooleanExtra("isNewTerminal", false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mbLoginWelcomeSheet = MBLoginWelcomeSheet()

        if (isNewTerminal) {
            showDialog()
        } else {
            mainActivityViewModel.viewModelScope.launch {
                if (mainActivityViewModel.count() <= 0)
                    userService.loadUsersFromServer(mainActivityViewModel.viewModelScope)
            }
        }

        Utils.setCal(GregorianCalendar())
        initNavbarListener()
        isInit = true

        registerBatteryReceiver()
        registerNetworkReceiver()

        //How to start a worker
        mainActivityViewModel.initHeartbeatService(this)

        setUpListeners()

        val view = binding.root
        setContentView(view)
        Utils.hideStatusAndNavbar(this)
    }

    private fun showDialog() {
        mbLoginWelcomeSheet.show(supportFragmentManager, MBLoginWelcomeSheet.TAG)
    }

    private fun registerNetworkReceiver() {
        networkChangeReceiver = NetworkChangeReceiver(this)
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)
    }

    private fun registerBatteryReceiver() {
        batteryReceiver = BatteryReceiver(this)
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, filter)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Utils.hideStatusAndNavbar(this)
    }

    override fun onPause() {
        timer.cancel()
        super.onPause()
    }

    override fun onResume() {
        val frag = supportFragmentManager.findFragmentByTag(AttendanceFragment.TAG)
        if (!isInit && (frag == null || !frag.isVisible))
            restartTimer()
        Utils.hideStatusAndNavbar(this)
        Handler(this.mainLooper).postDelayed(1000) {
            mainActivityViewModel.hideSystemUI()
        }
        isInit = false
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        unregisterReceiver(networkChangeReceiver)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpListeners() {
        mainActivityViewModel.viewModelScope.launch {
            binding.buttonSettings.setSafeOnClickListener {
                showVerificationAlert()
            }
//            binding.batteryIcon.setSafeOnClickListener {
//                mainActivityViewModel.hideSystemUI()
//            }
//            binding.networkConnectionIcon.setSafeOnClickListener {
//                mainActivityViewModel.showSystemUI()
//            }

            mainActivityViewModel.liveUserEntity.value = null
            mainActivityViewModel.liveUserEntity.observe(this@MainActivity) {
                if (it != null) {
                    showSettings(it)
                    mainActivityViewModel.liveUserEntity.value = null
                }
            }
        }
    }

    private fun initNavbarListener() {
        // use loaded permission to hide project menu entry as example
        mainActivityViewModel.viewModelScope.launch {
            val projectPermission = mainActivityViewModel.permission("projekt.use")
            binding.navigationRail.menu.findItem(R.id.project).isVisible =
                projectPermission == "true" && false// currently no functionality

            val attendancePermission = mainActivityViewModel.permission("kommengehen.use")
            binding.navigationRail.menu.findItem(R.id.attendance).isVisible =
                attendancePermission == "true"
        }

        setText()
        binding.navigationRail.menu.findItem(R.id.absence).isVisible =
            false// currently no functionality

        binding.navigationRail.setOnItemSelectedListener {

            it.isChecked = false
            val fragment: Fragment? = when (it.itemId) {
                R.id.attendance -> AttendanceFragment()
                R.id.absence -> AbsenceFragment.newInstance("", "")
                R.id.project -> ProjectFragment.newInstance("", "")
                R.id.info -> InfoFragment()

                else -> null
            }

            if (fragment == null) {
                return@setOnItemSelectedListener false
            } else {
                if (it.itemId == R.id.attendance) {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, fragment, AttendanceFragment.TAG)
                    }
                    timer.cancel()
                } else {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, fragment)
                    }
                    restartTimer()
                }
            }
            true
        }

        //init correct fragment, this could be also configurable if needed, but this is for the future
        if (binding.navigationRail.menu.findItem(R.id.attendance).isVisible) {
            showAttendanceFragment()
            timer.cancel()
        } else if (binding.navigationRail.menu.findItem(R.id.project).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ProjectFragment.newInstance("", ""))
            }
            restartTimer()
        } else if (binding.navigationRail.menu.findItem(R.id.absence).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AbsenceFragment.newInstance("", ""))
            }
            restartTimer()
        } else if (binding.navigationRail.menu.findItem(R.id.info).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, InfoFragment())
            }
            restartTimer()
        }
    }

    fun setText() {
        mainActivityViewModel.viewModelScope.launch {
            binding.navigationRail.menu.findItem(R.id.info).title =
                languageService.getText("ALLGEMEIN#Info")
            binding.navigationRail.menu.findItem(R.id.project).title =
                languageService.getText("ALLGEMEIN#Projekt")
            binding.navigationRail.menu.findItem(R.id.attendance).title =
                languageService.getText("#Attendance")
            binding.navigationRail.menu.findItem(R.id.absence).title =
                languageService.getText("#Absence")
        }
    }

    fun cancelTimer() {
        timer.cancel()
    }

    fun getViewModel(): MainActivityViewModel {
        return mainActivityViewModel
    }

    fun showLoadMask() {
        cancelTimer()
        runOnUiThread {
            binding.layoutLoadMaks.visibility = View.VISIBLE
        }
    }

    fun hideLoadMask() {
        restartTimer()
        runOnUiThread {
            binding.layoutLoadMaks.visibility = View.GONE
        }
    }

    fun reloadSoundSource() {
        mainActivityViewModel.reloadSoundSource()
    }

    // verify user if present before opening settings page
    private fun showVerificationAlert() {
        timer.cancel()
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)
        RfidService.unregister()
        RfidService.setListener(mainActivityViewModel)
        RfidService.register()
        FingerprintService.unregister()
        FingerprintService.setListener(mainActivityViewModel)
        FingerprintService.register()
        dialogBinding.textInputLayoutVerificationId.hint = languageService.getText("#Login","Login")
        dialogBinding.textInputLayoutVerificationPin.hint = languageService.getText("#PinCode","PIN")

        val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(this, R.style.MySmallDialog)
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton(
            languageService.getText(
                "BUTTON#Gen_Cancel",
                "CANCEL"
            )
        ) { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok", "OK")) { _, _ ->
            val login = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (login.isNotEmpty() && pin.isNotEmpty()) {
                mainActivityViewModel.viewModelScope.launch {
                    val user = mainActivityViewModel.getUserForLogin(login)
                    if (user != null && user.pin == pin && user.seeMenu) {
                        supportFragmentManager.commit {
                            replace(
                                R.id.fragment_container_view,
                                SettingsFragment.newInstance(user.id)
                            )
                        }
                        restartTimer()
                    } else {
                        soundSource.playSound(SoundSource.authenticationFailed)
                        Utils.showMessage(
                            supportFragmentManager,
                            languageService.getText("#VerificationFailed")
                        )
                    }
                }
            }
        }

        dialog = dlgAlert.create()
        Utils.hideNavInDialog(dialog)
        restartAlertTimer()

        dialogBinding.textInputEditTextVerificationId.doOnTextChanged { _, _, _, _ ->
            restartAlertTimer()
        }
        dialogBinding.textInputEditTextVerificationPin.doOnTextChanged { _, _, _, _ ->
            restartAlertTimer()
        }
        dialogBinding.textViewDialogVerificationMessage.text =
            languageService.getText("#FingerprintCardCredentials")
        dialog!!.setOnShowListener {
            dialogBinding.textInputEditTextVerificationId.isFocusable = true
            dialogBinding.textInputEditTextVerificationId.isFocusableInTouchMode = true
            dialogBinding.textInputEditTextVerificationId.transformationMethod = null
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        dialog!!.setOnDismissListener {
            RfidService.unregister()
            FingerprintService.unregister()
            alertTimer?.cancel()
            val frag = supportFragmentManager.findFragmentByTag(AttendanceFragment.TAG)
            if (!isInit && (frag == null || !frag.isVisible))
                restartTimer()

            if(frag != null && frag.isVisible)
                (frag as AttendanceFragment).onResume()

        }
        dialog!!.show()
    }

    private fun showSettings(user: UserEntity?) {
        dialog?.dismiss()
        if (user != null && user.seeMenu) {
            supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view,
                    SettingsFragment.newInstance(user.id)
                )
            }
            restartTimer()
        } else {
            Utils.showMessage(
                supportFragmentManager,
                languageService.getText("#VerificationFailed")
            )
        }
    }

    private fun showAttendanceFragment() {
        runOnUiThread {
            supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view,
                    AttendanceFragment(),
                    AttendanceFragment.TAG
                )
            }
        }
    }

    private fun restartAlertTimer() {
        alertTimer?.cancel()
        alertTimer = Timer("dialogClose", false)
        alertTimer!!.schedule(10000L) { dialog?.dismiss() }
    }

    override fun onBatteryStatusChanged(
        batteryPercentage: Int,
        usbCharge: Boolean,
        plugCharge: Boolean
    ) {
        val batteryText = "$batteryPercentage%"
        binding.batteryPercent.text = batteryText

        if (usbCharge || plugCharge) {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_charging_full_32)
        } else if (batteryPercentage < 10) {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_1_bar_32)
        } else if (batteryPercentage in 10..25) {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_2_bar_32)
        } else if (batteryPercentage in 26..50) {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_3_bar_32)
        } else if (batteryPercentage in 51..75) {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_4_bar_32)
        } else if (batteryPercentage in 76..99) {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_5_bar_32)
        } else {
            binding.batteryIcon.setImageResource(R.drawable.baseline_battery_full_32)
        }
    }

    override fun onNetworkChanged(networkType: NetworkType) {
        val res = when (networkType) {
            NetworkType.WIFI -> R.drawable.baseline_wifi_24
            NetworkType.ETHERNET -> R.drawable.baseline_lan_32
            NetworkType.LTE -> R.drawable.baseline_lte_mobiledata_24
            NetworkType.THIRD_GEN -> R.drawable.baseline_3g_mobiledata_24
            NetworkType.SECOND_GEN -> R.drawable.baseline_3g_mobiledata_24
            NetworkType.NOT_CONNECTED -> R.drawable.baseline_signal_cellular_connected_no_internet_0_bar_24
            else -> {
                R.drawable.baseline_signal_cellular_connected_no_internet_0_bar_24
            }
        }

        binding.networkConnectionIcon.setImageResource(res)
    }

    fun getBinding() = binding
}