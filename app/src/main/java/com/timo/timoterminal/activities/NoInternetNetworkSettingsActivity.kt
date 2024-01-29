package com.timo.timoterminal.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import com.timo.timoterminal.databinding.ActivityNoInternetNetworkSettingsBinding
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.zkteco.android.core.sdk.sources.IHardwareSource
import org.koin.android.ext.android.inject

// An activity to show if terminal has no connection to internet
// Has three buttons to change connection settings and one to proceed
class NoInternetNetworkSettingsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNoInternetNetworkSettingsBinding
    private val hardwareSource: IHardwareSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoInternetNetworkSettingsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        Utils.hideStatusAndNavbar(this)
        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        binding.buttonRetry.setSafeOnClickListener {
            finish()
        }
        binding.buttonEthernet.setSafeOnClickListener {
            hardwareSource.openAndroidEthernetSettings()
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