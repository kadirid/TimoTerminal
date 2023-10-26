package com.timo.timoterminal.modalBottomSheets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.repositories.UserRepository
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.ProgressBarAnimation
import com.timo.timoterminal.viewModel.MBSheetFingerprintCardReaderViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import org.koin.android.ext.android.inject


class MBSheetFingerprintCardReader : BottomSheetDialogFragment(), RfidListener,
    FingerprintListener {
    private val userRepository: UserRepository by inject()
    private val sharedPrefService: SharedPrefService by inject()
    private val httpService: HttpService by inject()

    private lateinit var binding: MbSheetFingerprintCardReaderBinding
    private var viewModel: MBSheetFingerprintCardReaderViewModel =
        MBSheetFingerprintCardReaderViewModel(userRepository, sharedPrefService, httpService)

    //    lateinit var textView: TextView
    private var status: Int = -1

    companion object {
        const val TAG = "MBSheetFingerprintCardReader"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbSheetFingerprintCardReaderBinding.inflate(inflater, container, false)

        setValues()
        setUpOnClickListeners()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setValues() {
        //before starting the animation, populate the fields with the correct data! Set the color
        // as well!
        status = arguments?.getInt("status") ?: -1
        val sStatus = when (arguments?.getInt("status")) {
            100 -> "Kommen"
            200 -> "Gehen"
            110 -> "Pause"
            210 -> "Pause Ende"
            else -> {
                "No known type"
            }
        }
        binding.nameContainer.text = "Elias Kadiri"
        binding.bookingTypeTextContainer.text = sStatus
        binding.timeTextContainer.text = "15:02"
    }

    private fun setUpOnClickListeners() {
        binding.fingerprintImage.setOnClickListener {
            animateSuccess()
        }

        //for test cases, can be removed later
        binding.cardImage.setOnClickListener {
            viewModel.sendBookingByCard("505650110", this)
        }

        binding.keyboardImage.setOnClickListener {
            showVerificationAlert()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val contentView = View.inflate(context, R.layout.mb_sheet_fingerprint_card_reader, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        return dialog
    }

    override fun onResume() {
        super.onResume()

        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
    }

    // remove listener on pause
    override fun onPause() {
        RfidService.unregister()
        FingerprintService.unregister()

        super.onPause()
    }

    fun addTextView(string: String) {
        val tv = TextView(requireContext())
        tv.text = string
        view?.findViewById<FrameLayout>(R.id.scan_bottom_sheet)?.addView(tv)
    }

    fun animateSuccess() {
        binding.identificationText.animate()
            .translationY(-binding.identificationText.height.toFloat())
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    //Animation, to increase the height of the dialog
                    val valueAnimator = ValueAnimator.ofInt(
                        binding.scanBottomSheet.measuredHeight,
                        binding.scanBottomSheet.measuredHeight + 200
                    )
                    valueAnimator.duration = 500L
                    valueAnimator.addUpdateListener {
                        val animatedValue = valueAnimator.animatedValue as Int
                        val layoutParams = binding.scanBottomSheet.layoutParams
                        layoutParams.height = animatedValue
                        binding.scanBottomSheet.layoutParams = layoutParams
                    }
                    valueAnimator.start()
                    valueAnimator.doOnEnd {
                        //Animation to show type of booking
                        binding.nameContainer.visibility = View.VISIBLE
                        binding.nameContainer.animate()
                            .alpha(1.0f)
                        //Animation to show time container
                        binding.bookingInfoContainer.visibility = View.VISIBLE
                        binding.bookingInfoContainer.animate()
                            .alpha(1.0f)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    super.onAnimationEnd(animation)
                                    val widthAnimator = ValueAnimator.ofInt(
                                        binding.progressContainer.measuredWidth,
                                        binding.progressContainer.measuredWidth + 100
                                    )
                                    widthAnimator.duration = 500L
                                    widthAnimator.addUpdateListener {
                                        val animatedValue = widthAnimator.animatedValue as Int
                                        val layoutParams =
                                            binding.progressContainer.layoutParams
                                        layoutParams.width = animatedValue
                                        binding.progressContainer.layoutParams = layoutParams
                                    }
                                    widthAnimator.start()
                                    widthAnimator.doOnEnd {
                                        val anim = ProgressBarAnimation(
                                            binding.progressIndicator,
                                            0F,
                                            100F
                                        )
                                        anim.duration = 2000
                                        anim.setAnimationListener(object :
                                            Animation.AnimationListener {
                                            override fun onAnimationStart(p0: Animation?) {
                                                //do nothing
                                            }

                                            override fun onAnimationEnd(p0: Animation?) {
                                                binding.progressIndicator.progress = 0
                                                dismiss()
                                            }

                                            override fun onAnimationRepeat(p0: Animation?) {
                                                //do nothing
                                            }
                                        })
                                        binding.progressIndicator.startAnimation(anim)
                                    }
                                }
                            })
                    }

                }

                override fun onAnimationStart(animation: Animator) {
                    //animation to move the name container up on place of indent text
                    binding.linearIconContainer.animate()
                        .translationY(-binding.identificationText.height.toFloat())
                }
            })
    }

    // get code of scanned card
    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            if (status > 0) {
                viewModel.sendBookingByCard(oct, this)
            }
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        TODO("Not yet implemented")
    }

    private fun showVerificationAlert() {
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)

        val dlgAlert: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        dlgAlert.setMessage("Please enter your credentials")
        dlgAlert.setTitle("Booking")
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setNegativeButton("Cancel") { dia, _ -> dia.dismiss() }
        dlgAlert.setPositiveButton("OK") { _, _ ->
            val code = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (code.isNotEmpty()) {
                viewModel.sendBookingById(code, pin, this)
            }
        }

        val dialog = dlgAlert.create()
        dialog.setOnShowListener {
            dialogBinding.textInputEditTextVerificationId.isFocusable = true
            dialogBinding.textInputEditTextVerificationId.isFocusableInTouchMode = true
            dialogBinding.textInputEditTextVerificationId.transformationMethod = null
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        dialog.show()
    }

    fun getStatus() = status

    fun setStatus(status: Int) {
        this.status = status
    }

    fun getBinding(): MbSheetFingerprintCardReaderBinding = binding

}