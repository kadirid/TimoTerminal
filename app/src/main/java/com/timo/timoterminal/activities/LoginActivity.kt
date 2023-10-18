package com.timo.timoterminal.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityLoginBinding
import com.timo.timoterminal.fragmentViews.LoginFragment
import com.timo.timoterminal.service.PropertyService
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mcv.facepass.BuildConfig
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL


class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private val loginActivityViewModel: LoginActivityViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusAndNavbar()
        binding = ActivityLoginBinding.inflate(layoutInflater)

        //init login fragment
        supportFragmentManager.commit {
            replace(R.id.fragment_container_view, LoginFragment())
        }

        val view = binding.root
        setContentView(view)
    }

    //check if logged in then open main activity else check connection
    override fun onResume() {
        super.onResume()
        loginActivityViewModel.syncTimezone(this)
        loginActivityViewModel.onResume(this) {
            loginActivityViewModel.loadPermissions(this) { worked ->
                if (worked) {
                    openMainView()
                }
            }
        }

    }

    //setting button Listener getting url and setting and saving url and company

    //load permission from server and open main activity
    private fun openMainView() {
        var goToMainActivity = Intent(this, MainActivity::class.java)
        goToMainActivity.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(goToMainActivity.addCategory(Intent.CATEGORY_LAUNCHER))
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
    private fun checkNetworkConnection(context: Context) = runBlocking(Dispatchers.IO) {
        try {
            val myUrl = URL("https://timo24.de")
            val connection = myUrl.openConnection()
            connection.connectTimeout = 5000
            connection.connect()
        } catch (e: Exception) {
            Log.d("INTERNET CHECK", "checkNetworkConnection: ${e.localizedMessage}")
            startActivity(Intent(context, NoInternetNetworkSettingsActivity::class.java))
        }
    }
}