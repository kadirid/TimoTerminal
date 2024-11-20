package com.timo.timoterminal.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import androidx.core.os.postDelayed
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.databinding.ActivityNoInternetNetworkSettingsBinding
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

// An activity to show if terminal has no connection to internet
// Has three buttons to change connection settings and one to proceed
class NoInternetNetworkSettingsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNoInternetNetworkSettingsBinding
    private val loginActivityViewModel : LoginActivityViewModel by viewModel()
    private var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoInternetNetworkSettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        Utils.hideStatusAndNavbar(this)
        setupOnClickListeners()

        Handler(this.mainLooper).postDelayed(200) {
            Log.d("NoInternetNetworkSettingsActivity","showSystemUI")
            MainApplication.lcdk.showSystemUI()
        }
    }

    override fun onResume() {
        super.onResume()
        if(!first) {
            loginActivityViewModel.viewModelScope.launch {
                if (Utils.isOnline(this@NoInternetNetworkSettingsActivity)) {
                    finish()
                }
            }
        }
        first = false
    }

    private fun setupOnClickListeners() {
        binding.buttonRetry.setSafeOnClickListener {
            loginActivityViewModel.viewModelScope.launch {
                if (Utils.isOnline(this@NoInternetNetworkSettingsActivity)) {
                    finish()
                }
            }
        }
        binding.buttonEthernet.setSafeOnClickListener {
            MainApplication.lcdk.openAndroidEthernetNetworkSettings()
        }
        binding.buttonMobileNetwork.setSafeOnClickListener {
            val intent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
            startActivity(intent)
        }
        binding.buttonWifi.setSafeOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }
    }
}