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
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.utils.ProgressBarAnimation


class MBSheetFingerprintCardReader : BottomSheetDialogFragment() {

    lateinit var binding: MbSheetFingerprintCardReaderBinding
    lateinit var textView: TextView

    companion object {
        const val TAG = "MBSheetFingerprintCardReader"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MbSheetFingerprintCardReaderBinding.inflate(inflater, container, false)
        binding.fingerprintImage.setOnClickListener {
            //before starting the animation, populate the fields with the correct data! Set the color
            // as well!
            binding.nameContainer.text = "Elias Kadiri"
            binding.bookingTypeTextContainer.text = "Kommen"
            binding.timeTextContainer.text = "15:02"

            animateSucess()

        }
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val contentView = View.inflate(context, R.layout.mb_sheet_fingerprint_card_reader, null);
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        return dialog
    }

    fun addTextView(string: String) {
        var tv = TextView(requireContext())
        tv.text = string
        view?.findViewById<FrameLayout>(R.id.scan_bottom_sheet)?.addView(tv)
    }

    fun animateSucess() {
        binding.identificationText.animate()
            .translationY(-binding.identificationText.height.toFloat())
            .alpha(0.0f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    //Animation, to increate the height of the dialog
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
                                                dismiss();
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
                    //animation to move the name container up on place of ident text
                    binding.linearIconContainer.animate()
                        .translationY(-binding.identificationText.height.toFloat())
                }
            })
    }


}