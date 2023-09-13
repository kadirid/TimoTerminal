package com.timo.timoterminal.activities

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.timo.timoterminal.databinding.ActivityLoginBinding
import com.timo.timoterminal.entityClasses.ConfigEntity
import com.timo.timoterminal.repositories.ConfigRepository
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginActivityViewModel: LoginActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusAndNavbar()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        initButtonListener()

        val view = binding.root
        setContentView(view)
    }

    //check if logged in then open main activity else check connection
    override fun onResume() {
        super.onResume()

        loginActivityViewModel.viewModelScope.launch {
            var loggedIn = true
            val url = loginActivityViewModel.getUrl()
            if (url == null || url.value.isEmpty()) {
                loggedIn = false
            }
            if (loggedIn) {
                val goToMainActivity = Intent(this@LoginActivity, MainActivity::class.java)
                goToMainActivity.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(goToMainActivity)
            } else {
                checkNetworkConnection()
            }
        }
    }

    //setting button Listener getting url and setting and saving url and company
    private fun initButtonListener() {
        binding.buttonLogin.setOnClickListener {
            val company = binding.textInputEditTextLoginCompany.text.toString()
            val user = binding.textInputEditTextLoginUser.text.toString()
            val password = binding.textInputEditTextLoginPassword.text.toString()
            val customUrl = binding.customUrl.text.toString()

            if(customUrl.isNotEmpty()){
                loginActivityViewModel.addConfig(
                    ConfigEntity(
                        ConfigRepository.TYPE_URL,
                        "url",
                        customUrl
                    )
                )
                loginActivityViewModel.addConfig(
                    ConfigEntity(
                        ConfigRepository.TYPE_COMPANY,
                        "company",
                        "standalone"
                    )
                )
                openMainView(user, customUrl, "standalone")
            }else {
                if (company.isNotEmpty()) {
                    loginActivityViewModel.getURlFromServer(company) { url ->
                        loginActivityViewModel.addConfig(
                            ConfigEntity(
                                ConfigRepository.TYPE_URL,
                                "url",
                                url
                            )
                        )
                        loginActivityViewModel.addConfig(
                            ConfigEntity(
                                ConfigRepository.TYPE_COMPANY,
                                "company",
                                company
                            )
                        )
                        Snackbar.make(binding.root, url, Snackbar.LENGTH_LONG).show()
                        openMainView(user, url, company)
                    }
                }
            }
        }
    }

    //load permission from server and open main activity
    private fun openMainView(user: String, url:String, company: String) {
        if (user.isNotEmpty()) {
            loginActivityViewModel.loadPermissions(url, company) {
                var goToMainActivity = Intent(this, MainActivity::class.java)
                goToMainActivity.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(goToMainActivity)
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

    // check connection to internet by pinging timo24.de
    private fun checkNetworkConnection() {
        var connected = true
        val connectivityManager = ContextCompat.getSystemService(
            this,
            ConnectivityManager::class.java
        ) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        if (network != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            if (networkCapabilities != null) {
                val runtime = Runtime.getRuntime()
                try {
                    val ipProcess = runtime.exec("/system/bin/ping -c 1 -w 1 timo24.de")
                    val exitValue = ipProcess.waitFor()
                    if (exitValue != 0) {
                        connected = false
                    }
                } catch (e: Exception) {
                    connected = false
                }
            } else {
                connected = false
            }
        } else {
            connected = false
        }

        if (!connected) {
            startActivity(Intent(this, NoInternetNetworkSettingsActivity::class.java))
        }
    }
}