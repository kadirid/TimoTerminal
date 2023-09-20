package com.timo.timoterminal.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuItemImpl
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
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.zkteco.android.core.sdk.sources.IHardwareSource
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val mainActivityViewModel: MainActivityViewModel by viewModel()
    private val hardwareSource: IHardwareSource by inject()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideStatusAndNavbar()
        initNavbarListener()

        //How to start a worker
        mainActivityViewModel.initHeartbeatService(application, this)

        mainActivityViewModel.viewModelScope.launch {

            mainActivityViewModel.demoEntities.collect {
                //dieser Code wird immer ausgeführt, wenn die Daten vom ViewModel aktualisiert werden
                // Hier kann man dann das UI demenstprechend updaten
                Log.d("DATABASE", "demoEntities changed ")
            }

            mainActivityViewModel.userEntities.collect {
                Log.d("DATABASE", "userEntities was changed")
            }
        }

        clickListeners()
    }

    private fun clickListeners() {
        binding.buttonSettings.setOnClickListener {
            mainActivityViewModel.viewModelScope.launch {
                if (mainActivityViewModel.count() == 0) {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, SettingsFragment.newInstance("", ""))
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

        binding.navigationRail.setOnItemSelectedListener {

            it.isChecked = false
            val fragment: Fragment? = when (it.itemId) {
                R.id.attendance -> AttendanceFragment.newInstance("", "")
                R.id.absence -> AbsenceFragment.newInstance("", "")
                R.id.project -> ProjectFragment.newInstance("", "")

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
        }

    }

    private fun hideStatusAndNavbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.apply {
                // Hide both the navigation bar and the status bar.
                systemUiVisibility =
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                            View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

            }
        }
    }

    // verify user if present before opening settings page
    private fun showVerificationAlert() {
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)

        val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(this)
        dlgAlert.setMessage("Please Scan finger print or card for verification or enter credentials")
        dlgAlert.setTitle("Verification")
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton("Cancel") { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton("OK") { _, _ ->
            val code = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (!code.isNullOrEmpty()) {
                mainActivityViewModel.viewModelScope.launch {
                    val user = mainActivityViewModel.getUserEntity(code.toLong())
                    if (user != null && user.pin == pin) {
                        supportFragmentManager.commit {
                            replace(
                                R.id.fragment_container_view,
                                SettingsFragment.newInstance("", "")
                            )
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
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        dialog.show()
    }
}