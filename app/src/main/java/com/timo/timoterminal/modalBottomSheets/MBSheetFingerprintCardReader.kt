package com.timo.timoterminal.modalBottomSheets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import androidx.core.widget.doOnTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.DialogVerificationBinding
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.ProgressBarAnimation
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MBSheetFingerprintCardReaderViewModel
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class MBSheetFingerprintCardReader(
    private val callback: () -> Unit?
) : BottomSheetDialogFragment() {
    private val languageService: LanguageService by inject()

    private lateinit var binding: MbSheetFingerprintCardReaderBinding
    private val viewModel: MBSheetFingerprintCardReaderViewModel by sharedViewModel()

    private var status: Int = -1
    private var ranCancel: Boolean = false
    private val timer = object : CountDownTimer(10000, 5000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }

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
        setUpListeners()

        return binding.root
    }

    private fun setValues() {
        //before starting the animation, populate the fields with the correct data! Set the color
        // as well!
        status = arguments?.getInt("status") ?: -1
        val sStatus = when (status) {
            100 -> languageService.getText("#Kommt")
            200 -> languageService.getText("#Geht")
            110 -> languageService.getText("ALLGEMEIN#Pause")
            210 -> languageService.getText("ALLGEMEIN#Pausenende")
            else -> {
                "No known type"
            }
        }
        binding.bookingTypeTextContainer.text = sStatus
        binding.cardImage.contentDescription = languageService.getText("#RFID")
        binding.keyboardImage.contentDescription = languageService.getText("#RFID")
        binding.identificationText.text = languageService.getText("#WaitIdentification")
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        ranCancel = true
        RfidService.unregister()
        FingerprintService.unregister()
        callback()
    }

    private fun setUpListeners() {
        // Can it be removed?
//        binding.fingerprintImage.setOnClickListener {
//            binding.timeTextContainer.text = Utils.getTimeFromGC(Utils.getCal())
//            animateSuccess()
//        }

//      binding.cardImage.setOnClickListener {
//          viewModel.sendBookingByCard("505650110", this)
//      }

        binding.keyboardImage.setSafeOnClickListener {
            showVerificationAlert()
        }

        viewModel.liveDone.observe(viewLifecycleOwner) {
            if (it == true) {
                animateSuccess()
                status = -1
                viewModel.liveDone.value = false
            }
        }
        viewModel.liveShowErrorColor.observe(viewLifecycleOwner) {
            if (it == true) {
                val color = activity?.resources?.getColorStateList(R.color.error_booking, null)
                if (color != null) binding.bookingInfoContainer.backgroundTintList = color
                viewModel.liveShowErrorColor.value = false
            }
        }
        viewModel.liveSetText.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                binding.textViewBookingMessage.text = it
                viewModel.liveSetText.value = ""
            }
        }
        viewModel.liveHideMask.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.sheetLayoutLoadMaks.visibility = View.GONE
                viewModel.liveHideMask.value = false
            }
        }
        viewModel.liveShowMask.observe(viewLifecycleOwner) {
            if (it == true) {
                binding.sheetLayoutLoadMaks.visibility = View.VISIBLE
                viewModel.liveShowMask.value = false
            }
        }
        viewModel.liveOfflineBooking.observe(viewLifecycleOwner) {
            if (it.status != -1) {
                viewModel.processOffline(it)
                it.status = -1
                viewModel.liveOfflineBooking.value = it

                viewModel.liveHideMask.value = true
                viewModel.liveSetText.value = languageService.getText("#BookingTemporarilySaved")
                viewModel.liveDone.value = true
            }
        }
        viewModel.liveShowInfo.observe(viewLifecycleOwner) {
            if (it.first.isNotEmpty()) {
                binding.timeTextContainer.text = it.first
                binding.nameContainer.text = it.second
                viewModel.liveShowMask.value = true

                viewModel.liveShowInfo.value = Pair("", "")
            }
        }
        viewModel.liveSendBooking.observe(viewLifecycleOwner) {
            if (it.status == -1) {
                timer.cancel()
                it.status = status
                viewModel.sendBooking(it)
                viewModel.liveSendBooking.value = it
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)

        val contentView = View.inflate(context, R.layout.mb_sheet_fingerprint_card_reader, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        return dialog
    }

    override fun onResume() {
        super.onResume()

        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(viewModel)
        RfidService.register()
        FingerprintService.setListener(viewModel)
        FingerprintService.register()
        timer.start()
    }

    // remove listener on pause
    override fun onPause() {
        if (!ranCancel) {
            RfidService.unregister()
            FingerprintService.unregister()
            ranCancel = false
        }
        timer.cancel()

        super.onPause()
    }

    private fun animateSuccess() {
        timer.cancel()
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
                        //Animation to value container
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
                                        anim.duration = 5000
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

    private fun showVerificationAlert() {
        val dialogBinding = DialogVerificationBinding.inflate(layoutInflater)
        timer.cancel()

        val dlgAlert: AlertDialog.Builder =
            AlertDialog.Builder(requireContext(), R.style.MySmallDialog)
        dlgAlert.setView(dialogBinding.root)
        dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
            val login = dialogBinding.textInputEditTextVerificationId.text.toString()
            val pin = dialogBinding.textInputEditTextVerificationPin.text.toString()
            if (login.isNotEmpty() && pin.isNotEmpty()) {
                viewModel.sendBookingByLogin(login, pin)
            }
        }
        dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }

        val dialog = dlgAlert.create()
        val alertTimer = object : CountDownTimer(10000, 500) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                dialog.dismiss()
            }
        }

        dialogBinding.textInputEditTextVerificationId.doOnTextChanged { _, _, _, _ ->
            alertTimer.cancel()
            alertTimer.start()
        }
        dialogBinding.textInputEditTextVerificationPin.doOnTextChanged { _, _, _, _ ->
            alertTimer.cancel()
            alertTimer.start()
        }
        dialogBinding.textViewDialogVerificationMessage.text =
            languageService.getText("#EnterCredentials")
        dialog.setOnShowListener {
            Utils.hideNavInDialog(this.dialog)
            dialogBinding.textInputEditTextVerificationId.isFocusable = true
            dialogBinding.textInputEditTextVerificationId.isFocusableInTouchMode = true
            dialogBinding.textInputEditTextVerificationId.transformationMethod = null
            dialogBinding.textInputEditTextVerificationPin.isFocusable = true
            dialogBinding.textInputEditTextVerificationPin.isFocusableInTouchMode = true
        }
        Utils.hideNavInDialog(dialog)
        dialog.setOnDismissListener {
            Utils.hideNavInDialog(this.dialog)
            alertTimer.cancel()
            timer.start()
        }
        dialog.show()
    }
}
