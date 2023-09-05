package com.timo.timoterminal.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityMainBinding
import com.timo.timoterminal.fragmentViews.AbsenceFragment
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.fragmentViews.ProjectFragment
import com.timo.timoterminal.fragmentViews.SettingsFragment
import com.timo.timoterminal.viewModel.MainActivityViewModel
import com.zkteco.android.core.sdk.sources.IHardwareSource
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
        mainActivityViewModel.initHearbeatService(application, this)

        mainActivityViewModel.viewModelScope.launch {
            mainActivityViewModel.demoEntities.collect {
                //dieser Code wird immer ausgeführt, wenn die Daten vom ViewModel aktualisiert werden
                // Hier kann man dann das UI demenstprechend updaten
                Log.d("DATABASE", "demoEntities changed ")
            }
        }
        mainActivityViewModel.viewModelScope.launch {
            mainActivityViewModel.userEntities.collect {
                Log.d("DATABASE", "userEntities was changed")
            }
        }
        binding.settingsFab.setOnClickListener {
            mainActivityViewModel.viewModelScope.launch {
                if (mainActivityViewModel.count() == 0) {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, SettingsFragment.newInstance("", ""))
                    }
                } else {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, SettingsFragment.newInstance("", ""))
                    }
                }
            }
        }
        binding.imageViewLogo.setOnClickListener {
            mainActivityViewModel.killHeartBeatWorkers(application)
        }
    }

    private fun initNavbarListener() {
        binding.navigationRail.setOnItemSelectedListener {

            val fragment: Fragment? = when (it.itemId) {
                R.id.attendance -> AttendanceFragment.newInstance("","")
                R.id.absence -> AbsenceFragment.newInstance("","")
                R.id.project -> ProjectFragment.newInstance("","")

                else -> null
            }

            if (fragment == null){
                return@setOnItemSelectedListener false
            }else{
                supportFragmentManager.commit {
                    replace(R.id.fragment_container_view, fragment)
                }
            }

            true
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
}