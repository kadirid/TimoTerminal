package com.timo.timoterminal.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.timo.timoterminal.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideStatusAndNavbar()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        initButtonListener()

        val view = binding.root
        setContentView(view)
    }

    private fun initButtonListener() {
        binding.buttonGoMain.setOnClickListener {
            var goToMainActivity = Intent(this, MainActivity::class.java)
            goToMainActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(goToMainActivity)
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