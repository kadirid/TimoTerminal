package com.timo.timoterminal.activities

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.viewModelScope
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.ActivityLoginBinding
import com.timo.timoterminal.fragmentViews.LoginFragment
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.LoginActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.URL


class LoginActivity : AppCompatActivity() {

    companion object {
        const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private val loginActivityViewModel: LoginActivityViewModel by viewModel()
    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)

        //init login fragment
        supportFragmentManager.commit {
            replace(R.id.fragment_container_view, LoginFragment())
        }

        val view = binding.root
        setContentView(view)

        setUpObserver()
    }

    private fun setUpObserver() {
        loginActivityViewModel.viewModelScope.launch {
            loginActivityViewModel.liveHidePleaseWaitMask.value = false
            loginActivityViewModel.liveHidePleaseWaitMask.observeForever {
                if (it) {
                    alertDialog?.dismiss()
                    loginActivityViewModel.liveHidePleaseWaitMask.value = false
                }
            }

            loginActivityViewModel.liveShowPleaseWaitMask.value = false
            loginActivityViewModel.liveShowPleaseWaitMask.observeForever {
                if (it) {
                    showDialog()
                    loginActivityViewModel.liveShowPleaseWaitMask.value = false
                }
            }
        }
    }

    private fun showDialog() {
        loginActivityViewModel.viewModelScope.launch {
            val message: String

            val locale = loginActivityViewModel.getLocal()
            val config = Configuration()
            config.setLocale(locale)
            val context = baseContext?.createConfigurationContext(config)
            message = context?.getText(R.string.please_wait)?.toString()
                ?: getText(R.string.please_wait).toString()

            val dlgAlert: AlertDialog.Builder =
                AlertDialog.Builder(this@LoginActivity, R.style.MyDialog)
            dlgAlert.setMessage(message)

            alertDialog = dlgAlert.create()
            alertDialog!!.setOnShowListener {
                alertDialog?.setCancelable(false)
                alertDialog?.setCanceledOnTouchOutside(false)
                val textView = alertDialog?.findViewById<TextView>(android.R.id.message)
                textView?.textSize = 50f
                textView?.textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
            alertDialog!!.show()
        }
    }

    //check if logged in then open main activity else check connection
    override fun onResume() {
        super.onResume()
        loginActivityViewModel.onResume(this) {
            loginActivityViewModel.loadPermissions(this) { worked ->
                if (worked) {
                    openMainView()
                }
                loginActivityViewModel.liveHidePleaseWaitMask.postValue(true)
            }
        }
        Utils.hideStatusAndNavbar(this)
    }

    //setting button Listener getting url and setting and saving url and company

    //load permission from server and open main activity
    private fun openMainView() {
        val goToMainActivity = Intent(this, MainActivity::class.java)
        goToMainActivity.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(goToMainActivity.addCategory(Intent.CATEGORY_LAUNCHER))
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