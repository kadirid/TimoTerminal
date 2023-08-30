package com.timo.timoterminal.activities

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.viewModelScope
import com.google.android.material.navigationrail.NavigationRailView
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityMainBinding
import com.timo.timoterminal.fragments.AbsenceFragment
import com.timo.timoterminal.fragments.AttendanceFragment
import com.timo.timoterminal.fragments.ProjectFragment
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
        hideStatusAndNavbar()

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_view, AttendanceFragment.newInstance("", ""))
            .commit()

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
    }

    private fun initNavbarListener() {
        val navbar: NavigationRailView = binding.navigationRail
        navbar.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.attendance -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, AttendanceFragment.newInstance("", ""))
                        .commit()
                    it.isEnabled = true
                    true
                }
                R.id.absence -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, AbsenceFragment.newInstance("", ""))
                        .commit()
                    it.isEnabled = true
                    true
                }
                R.id.project -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, ProjectFragment.newInstance("", ""))
                        .commit()
                    it.isEnabled = true
                    true
                }

                else -> {
                    false
                }
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
}