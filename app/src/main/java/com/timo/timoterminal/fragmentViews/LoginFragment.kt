package com.timo.timoterminal.fragmentViews

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.FragmentLoginBinding
import com.timo.timoterminal.viewModel.LoginFragmentViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginFragment : Fragment() {

    private val viewModel: LoginFragmentViewModel by viewModel()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        initButtonListener()
        return binding.root
    }

    private fun openMainView() {
        var goToMainActivity = Intent(context, MainActivity::class.java)
        goToMainActivity.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(goToMainActivity)
    }

    private fun showLangAndTimezone() {

        val animSet = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_out_move_up)
        val fadeInAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)

        animSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                binding.textInputLayoutLoginCompany.visibility = View.GONE
                binding.textInputLayoutLoginPassword.visibility = View.GONE
                binding.textInputLayoutLoginUser.visibility = View.GONE

                binding.textInputLayoutLanguage.startAnimation(fadeInAnimation)
                binding.textInputLayoutLanguage.visibility = View.VISIBLE

                binding.textInputLayoutTimezone.startAnimation(fadeInAnimation)
                binding.textInputLayoutTimezone.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })
        activity?.runOnUiThread {
            binding.textInputLayoutLoginCompany.startAnimation(animSet)
            binding.textInputLayoutLoginPassword.startAnimation(animSet)
            binding.textInputLayoutLoginUser.startAnimation(animSet)

        }
    }

    private fun initButtonListener() {
        binding.buttonLogin.setOnClickListener {
            val company = binding.textInputEditTextLoginCompany.text.toString()
            val user = binding.textInputEditTextLoginUser.text.toString()
            val password = binding.textInputEditTextLoginPassword.text.toString()
            val customUrl = binding.customUrl.text.toString()

            viewModel.loginCompany(
                company,
                user,
                password,
                customUrl,
                requireContext()
            ) {
                viewModel.loadPermissions(requireContext()) { worked ->
                    if (worked) {
                        //openMainView()
                        showLangAndTimezone()
                    }
                }
            }
        }
    }
}
