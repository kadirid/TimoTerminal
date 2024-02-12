package com.timo.timoterminal.modalBottomSheets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import androidx.core.animation.doOnEnd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.ProgressBarAnimation
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import org.koin.android.ext.android.inject

class MBBookingMessageSheet : BottomSheetDialogFragment() {
    private val languageService: LanguageService by inject()

    private lateinit var binding: MbSheetFingerprintCardReaderBinding

    private var funcCode: Int = -1
    private var name: String = ""
    private var message: String = ""
    private var success: Boolean = false
    private val soundSource: SoundSource by inject()

    companion object {
        const val TAG = "MBBookingMessageSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbSheetFingerprintCardReaderBinding.inflate(inflater, container, false)

        setValues()
        animateSuccess()

        return binding.root
    }

    private fun setValues() {
        funcCode = arguments?.getInt("funcCode") ?: -1
        name = arguments?.getString("name", "") ?: ""
        message = arguments?.getString("message", "") ?: ""
        success = arguments?.getBoolean("success", false) ?: false

        val sStatus = when (arguments?.getInt("funcCode")) {
            100 -> languageService.getText("#Kommt")
            200 -> languageService.getText("#Geht")
            110 -> languageService.getText("ALLGEMEIN#Pause")
            210 -> languageService.getText("ALLGEMEIN#Pausenende")
            else -> {
                "No known type"
            }
        }

        binding.keyboardImage.visibility = View.INVISIBLE
        binding.fingerprintImage.visibility = View.INVISIBLE
        binding.cardImage.visibility = View.INVISIBLE
        binding.bookingTypeTextContainer.text = sStatus
        binding.nameContainer.text = name
        binding.timeTextContainer.text = Utils.getTimeFromGC(Utils.getCal())
        binding.textViewBookingMessage.text = message

        if (!success) {
            val color = activity?.resources?.getColorStateList(R.color.error_booking, null)
            if (color != null)
                binding.bookingInfoContainer.backgroundTintList = color
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
        if(success){
            soundSource.playSound(SoundSource.successSound)
        }else{
            soundSource.playSound(SoundSource.failedSound)
        }
    }

    private fun animateSuccess() {
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

}