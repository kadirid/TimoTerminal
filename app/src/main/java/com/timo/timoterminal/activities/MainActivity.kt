package com.timo.timoterminal.activities

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityMainBinding
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.fragmentViews.AbsenceFragment
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.fragmentViews.ProjectFragment
import com.timo.timoterminal.fragmentViews.SettingsFragment
import com.timo.timoterminal.utils.BatteryReceiver
import com.timo.timoterminal.utils.NetworkChangeReceiver
import com.timo.timoterminal.enums.NetworkType
import com.timo.timoterminal.fragmentViews.InfoFragment
import com.timo.timoterminal.modalBottomSheets.MBLoginWelcomeSheet
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.MainActivityViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity(), BatteryReceiver.BatteryStatusCallback,
    NetworkChangeReceiver.NetworkStatusCallback {

    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val languageService: LanguageService by inject()

    private lateinit var binding: ActivityMainBinding
    private lateinit var batteryReceiver: BatteryReceiver
    private lateinit var networkChangeReceiver: NetworkChangeReceiver
    private lateinit var mbLoginWelcomeSheet: MBLoginWelcomeSheet

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isNewTerminal = intent.getBooleanExtra("isNewTerminal", false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mbLoginWelcomeSheet = MBLoginWelcomeSheet()

        Utils.hideStatusAndNavbar(this)


        if (isNewTerminal) {
            showDialog()
        }

        setContentView(binding.root)

        initNavbarListener()

        registerBatteryReceiver()
        registerNetworkReceiver()

        //How to start a worker
        mainActivityViewModel.initHeartbeatService(application, this)

        clickListeners()
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

    override fun onResume() {
        Utils.hideStatusAndNavbar(this)
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        unregisterReceiver(networkChangeReceiver)
    }

    private fun clickListeners() {
        binding.buttonSettings.setOnClickListener {
            mainActivityViewModel.viewModelScope.launch {
                if (mainActivityViewModel.count() == 0) {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, SettingsFragment())
                    }
                } else {
                    showVerificationAlert()
                }
            }
        }
        // to kill heart beat worker and clear some of the db data
        binding.imageViewLogo.setOnClickListener {
            mainActivityViewModel.killHeartBeatWorkers(application)
        }
    }

    private fun initNavbarListener() {
        // use loaded permission to hide project menu entry as example
        mainActivityViewModel.viewModelScope.launch {
            val projectPermission = mainActivityViewModel.permission("projekt.use")
            binding.navigationRail.menu.findItem(R.id.project).isVisible =
                projectPermission == "true"

            val attendancePermission = mainActivityViewModel.permission("kommengehen.use")
            binding.navigationRail.menu.findItem(R.id.attendance).isVisible =
                attendancePermission == "true"
        }

        binding.navigationRail.menu.findItem(R.id.info).title =
            languageService.getText("ALLGEMEIN#Info")
        binding.navigationRail.menu.findItem(R.id.project).title =
            languageService.getText("ALLGEMEIN#Projekt")
        binding.navigationRail.menu.findItem(R.id.attendance).title =
            languageService.getText("#Attendance")
        binding.navigationRail.menu.findItem(R.id.absence).title =
            languageService.getText("#Absence")

        binding.navigationRail.setOnItemSelectedListener {

            it.isChecked = false
            val fragment: Fragment? = when (it.itemId) {
                R.id.attendance -> AttendanceFragment.newInstance("", "")
                R.id.absence -> AbsenceFragment.newInstance("", "")
                R.id.project -> ProjectFragment.newInstance("", "")
                R.id.info -> InfoFragment()

                else -> null
            }

            if (fragment == null) {
                return@setOnItemSelectedListener false
            } else {
                supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, fragment)
                }
            }
            true
        }

        //init correct fragment, this could be also configurable if needed, but this is for the future
        if (binding.navigationRail.menu.findItem(R.id.attendance).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AttendanceFragment.newInstance("", ""))
            }
        } else if (binding.navigationRail.menu.findItem(R.id.project).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, ProjectFragment.newInstance("", ""))
            }
        } else if (binding.navigationRail.menu.findItem(R.id.absence).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, AbsenceFragment.newInstance("", ""))
            }
        } else if (binding.navigationRail.menu.findItem(R.id.info).isVisible) {
            supportFragmentManager.commit {
                replace(R.id.fragment_container_view, InfoFragment())
            }
        }
    }

    // verify user if present before opening settings page
    private fun showVerificationAlert() {
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)

        val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(this)
        dlgAlert.setMessage(languageService.getText("#FingerprintCardCredentials"))
        dlgAlert.setTitle(languageService.getText("#Verification"))
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
            val code = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (code.isNotEmpty()) {
                mainActivityViewModel.viewModelScope.launch {
                    val user = mainActivityViewModel.getUserEntity(code.toLong())
                    if (user != null && user.pin == pin) {
                        supportFragmentManager.commit {
                            replace(R.id.fragment_container_view, SettingsFragment())
                        }
                    } else {
                        Snackbar.make(binding.root, "Verification failed", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }

        val dialog = dlgAlert.create()
        dialog.setOnShowListener {
            dialogBinding.textInputEditTextVerificationId.isFocusable = true
            dialogBinding.textInputEditTextVerificationId.isFocusableInTouchMode = true
            dialogBinding.textInputEditTextVerificationId.transformationMethod = null
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        dialog.show()
    }

    override fun onBatteryStatusChanged(
        batteryPercentage: Int,
        usbCharge: Boolean,
        plugCharge: Boolean
    ) {
        binding.batteryPercent.text = "$batteryPercentage%"

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
}