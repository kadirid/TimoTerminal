package com.timo.timoterminal.activities

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityMainBinding
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.entityClasses.UserEntity
import com.timo.timoterminal.enums.NetworkType
import com.timo.timoterminal.fragmentViews.AbsenceFragment
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.fragmentViews.InfoFragment
import com.timo.timoterminal.fragmentViews.ProjectFragment
import com.timo.timoterminal.fragmentViews.ProjectFragmentStopwatch
import com.timo.timoterminal.fragmentViews.SettingsFragment
import com.timo.timoterminal.modalBottomSheets.MBLoginWelcomeSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.ProjectPrefService
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.BatteryReceiver
import com.timo.timoterminal.utils.NetworkChangeReceiver
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.GregorianCalendar
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.text.toInt


class MainActivity : AppCompatActivity(), BatteryReceiver.BatteryStatusCallback,
    NetworkChangeReceiver.NetworkStatusCallback {

    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val languageService: LanguageService by inject()

    private val projectPrefService: ProjectPrefService by inject()
    private val userService: UserService by inject()
    private val soundSource: SoundSource by inject()
    private val propertyService: PropertyService by inject()

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
    private var timerLength =
        propertyService.getProperties().getProperty("timerLengthMS", "10000").toLong()

    fun restartTimer() {
        timer.cancel()
        timer = Timer("showAttendanceFragment", false)
        timer.schedule(timerLength) {
            showAttendanceFragment()
        }
    }

    private var useProjectStopwatch: Boolean = true

    private fun refreshProjectMode() {
        useProjectStopwatch = projectPrefService.isStopwatchMode()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set night mode, needs to be set before inflating the layout
        //AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

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

        mainActivityViewModel.checkAndSaveVersionName(this)

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
        isInit = false

        if (!mainActivityViewModel.hasUpdate()) {
            binding.terminalHasUpdateButton.visibility = View.INVISIBLE
        }else{
            binding.terminalHasUpdateButton.visibility = View.VISIBLE
        }
        testLanguage(mainActivityViewModel.viewModelScope)

        super.onResume()
    }

    override fun onDestroy() {
        unregisterReceiver(batteryReceiver)
        unregisterReceiver(networkChangeReceiver)
        super.onDestroy()
    }

    private fun setUpListeners() {
        binding.buttonSettings.setSafeOnClickListener {
            showVerificationAlert(::showSettings)
        }

        mainActivityViewModel.liveUserEntity.value = null
        binding.terminalHasUpdateButton.setOnClickListener {
            if (mainActivityViewModel.hasUpdate()) {
                val launchIntent: Intent? =
                    packageManager.getLaunchIntentForPackage("com.timo.timoupdate")
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    launchIntent.putExtra("src", mainActivityViewModel.getDownloadUrl())
                    launchIntent.putExtra("version", mainActivityViewModel.getFutureVersionName())
                    ContextCompat.startActivity(
                        this,
                        launchIntent,
                        null
                    ) //null pointer check in case package name was not found
                }
            }
        }
    }

    private fun initNavbarListener() {
        refreshProjectMode()

        mainActivityViewModel.viewModelScope.launch {
            val projectPermission = mainActivityViewModel.permission("projekt.use")
            binding.navigationRail.menu.findItem(R.id.project).isVisible =
                projectPermission == "true"

            val attendancePermission = mainActivityViewModel.permission("kommengehen.use")
            binding.navigationRail.menu.findItem(R.id.attendance).isVisible =
                attendancePermission == "true"

            val infoPermission = mainActivityViewModel.permission("terminal.infobutton.use")
            binding.navigationRail.menu.findItem(R.id.info).isVisible =
                infoPermission == "true"
        }

        setText()
        binding.navigationRail.menu.findItem(R.id.absence).isVisible = false

        binding.navigationRail.setOnItemSelectedListener {

            it.isChecked = false
            val fragment: Fragment? = when (it.itemId) {
                R.id.attendance -> AttendanceFragment()
                R.id.project -> null // handled below to choose correct fragment
                R.id.absence -> AbsenceFragment.newInstance("", "")
                R.id.info -> InfoFragment()
                else -> null
            }

            if (it.itemId == R.id.project) {
                showVerificationAlert { user ->
                    if (user != null) {
                        openProjectForUser(user)
                    }
                }
                return@setOnItemSelectedListener true
            }

            if (fragment == null) {
                return@setOnItemSelectedListener false
            } else {
                when (it.itemId) {
                    R.id.attendance -> {
                        supportFragmentManager.commit {
                            replace(R.id.fragment_container_view, fragment, AttendanceFragment.TAG)
                        }
                        timer.cancel()
                    }
                    else -> {
                        supportFragmentManager.commit {
                            addToBackStack(null)
                            replace(R.id.fragment_container_view, fragment)
                        }
                        restartTimer()
                    }
                }
            }
            true
        }

        // Initiales Fragment: wenn Attendance sichtbar -> Attendance, sonst Projekt nach aktuellem Modus
        if (binding.navigationRail.menu.findItem(R.id.attendance).isVisible) {
            showAttendanceFragment()
            timer.cancel()
        } else if (binding.navigationRail.menu.findItem(R.id.project).isVisible) {
            openProjectDefault()
        } else if (binding.navigationRail.menu.findItem(R.id.absence).isVisible) {
            supportFragmentManager.commit {
                addToBackStack(null)
                replace(R.id.fragment_container_view, AbsenceFragment.newInstance("", ""))
            }
            restartTimer()
        } else if (binding.navigationRail.menu.findItem(R.id.info).isVisible) {
            supportFragmentManager.commit {
                addToBackStack(null)
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
            binding.terminalHasUpdateButton.text =
                languageService.getText("#UpdateAvailable")
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

    fun hideLoadMask(restartTimer: Boolean = true) {
        if (restartTimer) {
            restartTimer()
        }
        runOnUiThread {
            binding.layoutLoadMaks.visibility = View.GONE
        }
    }

    fun reloadSoundSource() {
        mainActivityViewModel.reloadSoundSource()
    }

    fun loadSoundForFP(finger: Int) {
        mainActivityViewModel.loadSoundForFP(finger)
    }

    // verify user if present before executing action
    private fun showVerificationAlert(action: (UserEntity?) -> Unit) {
        val observer = Observer<UserEntity?> {
            if (it != null) {
                action(it)
                mainActivityViewModel.liveUserEntity.value = null
            }
        }
        mainActivityViewModel.liveUserEntity.observe(this@MainActivity, observer)

        timer.cancel()
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)
        RfidService.unregister()
        RfidService.setListener(mainActivityViewModel)
        RfidService.register()

        MainApplication.lcdk.setFingerprintListener(null)
        MainApplication.lcdk.setFingerprintListener(mainActivityViewModel)
        dialogBinding.textInputLayoutVerificationId.hint =
            languageService.getText("#Login", "Login")
        dialogBinding.textInputLayoutVerificationPin.hint =
            languageService.getText("#PinCode", "PIN")

        val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(this, R.style.MySmallDialog)
        dlgAlert.setView(dialogBinding.root)

        dialog = dlgAlert.create()
        Utils.hideNavInDialog(dialog)
        restartAlertTimer()

        // Custom Button Texte setzen
        dialogBinding.buttonCancel.text = languageService.getText("BUTTON#Gen_Cancel", "ABBRECHEN")
        dialogBinding.buttonOk.text = languageService.getText("ALLGEMEIN#ok", "OK")

        // Custom Button Click Listener
        dialogBinding.buttonCancel.setOnClickListener {
            dialog?.dismiss()
        }

        dialogBinding.buttonOk.setOnClickListener {
            val login = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (login.isNotEmpty() && pin.isNotEmpty()) {
                mainActivityViewModel.viewModelScope.launch {
                    val user = mainActivityViewModel.getUserForLogin(login)
                    if (user != null && user.pin == pin) {
                        action(user)
                        dialog?.dismiss()
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
            MainApplication.lcdk.setFingerprintListener(null)
            mainActivityViewModel.liveUserEntity.removeObserver(observer)
            alertTimer?.cancel()
            val frag = supportFragmentManager.findFragmentByTag(AttendanceFragment.TAG)
            if (!isInit && (frag == null || !frag.isVisible))
                restartTimer()

            if (frag != null && frag.isVisible)
                (frag as AttendanceFragment).onResume()

        }
        dialog!!.show()
    }

    private fun showSettings(user: UserEntity?) {
        dialog?.dismiss()
        if (user != null && user.seeMenu) {
            supportFragmentManager.commit {
                addToBackStack(null)
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

    fun showAttendanceFragment() {
        runOnUiThread {
            supportFragmentManager.commit {
                replace(
                    R.id.fragment_container_view, AttendanceFragment(), AttendanceFragment.TAG
                )
            }
            binding.navigationRail.selectedItemId = R.id.attendance
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

    private fun testLanguage(coroutineScope: CoroutineScope){
        if(languageService.getText("#Attendance","ErrorDefault") == "ErrorDefault"){
            languageService.requestLanguageFromServer(coroutineScope, this)
        }
    }


    // NEU: Öffnet das passende Projekt-Fragment nach erfolgreicher Verifikation
    private fun openProjectForUser(user: UserEntity) {
        if (useProjectStopwatch) {
            supportFragmentManager.commit {
                addToBackStack(null)
                replace(
                    R.id.fragment_container_view,
                    ProjectFragmentStopwatch.newInstance(
                        user.id,
                        user.customerBasedProjectTime,
                        user.timeEntryType,
                        user.crossDay
                    )
                )
            }
        } else {
            supportFragmentManager.commit {
                addToBackStack(null)
                replace(
                    R.id.fragment_container_view,
                    ProjectFragment.newInstance(
                        user.id,
                        user.customerBasedProjectTime,
                        user.timeEntryType,
                        user.crossDay
                    )
                )
            }
        }
        restartTimer()
    }

    // NEU: Öffnet die Projektseite ohne User-Kontext (Initialfall / Fallback)
    private fun openProjectDefault() {
        if (useProjectStopwatch) {
            supportFragmentManager.commit {
                addToBackStack(null)
                replace(
                    R.id.fragment_container_view,
                    ProjectFragmentStopwatch.newInstance(-1, false, -1, false)
                )
            }
        } else {
            supportFragmentManager.commit {
                addToBackStack(null)
                replace(
                    R.id.fragment_container_view,
                    ProjectFragment.newInstance(-1, false, -1, false)
                )
            }
        }
        restartTimer()
    }

    fun onProjectSettingsChanged() {
        refreshProjectMode()
    }
}
