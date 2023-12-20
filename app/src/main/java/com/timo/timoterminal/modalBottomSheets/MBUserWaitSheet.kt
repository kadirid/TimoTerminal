package com.timo.timoterminal.modalBottomSheets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.MBUserWaitSheetViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import org.koin.android.ext.android.inject

private const val ARG_ID = "id"
private const val ARG_EDITOR = "editor"
private const val ARG_IS_FP = "isFP"

class MBUserWaitSheet : BottomSheetDialogFragment(), RfidListener, FingerprintListener {

    private var enrollCount: Int = 0
    private val userService: UserService by inject()
    private val viewModel = MBUserWaitSheetViewModel(userService)
    private lateinit var binding: MbSheetFingerprintCardReaderBinding

    private val templates = mutableListOf<String>()

    private lateinit var image: ImageView
    private var id: String? = null
    private var editor: String? = null
    private var isFP = false
    private var finger = 6
    private var timer: CountDownTimer? = null
    private val timer2 = object : CountDownTimer(5000, 500) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_ID)
            editor = it.getString(ARG_EDITOR)
            isFP = it.getBoolean(ARG_IS_FP)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbSheetFingerprintCardReaderBinding.inflate(inflater, container, false)

        setValues()
        setOnClickListeners()
        if(isFP)
            showFinger()

        return binding.root
    }

    private fun setOnClickListeners() {
        if(isFP){
            image = binding.fingerSelectArrow6
            binding.fingerSelectArrow0.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 0
                }
            }
            binding.fingerSelectArrow1.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 1
                }
            }
            binding.fingerSelectArrow2.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 2
                }
            }
            binding.fingerSelectArrow3.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 3
                }
            }
            binding.fingerSelectArrow4.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 4
                }
            }
            binding.fingerSelectArrow5.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 5
                }
            }
            binding.fingerSelectArrow6.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 6
                }
            }
            binding.fingerSelectArrow7.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 7
                }
            }
            binding.fingerSelectArrow8.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 8
                }
            }
            binding.fingerSelectArrow9.setOnClickListener {
                restartTimer()
                if(it.tag == null) {
                    image = it as ImageView
                    finger = 9
                }
            }
            animate()
        }
    }

    private fun setValues() {
        timer = object: CountDownTimer(if(isFP) 20000 else 10000, 5000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                dismiss()
            }
        }

        binding.identificationText.visibility = View.GONE
        binding.keyboardImage.visibility = View.GONE
        binding.nameContainer.visibility = View.GONE
        binding.bookingTypeTextContainer.visibility = View.GONE
        binding.progressContainer.visibility = View.GONE
        binding.timeTextContainer.visibility = View.GONE
        binding.sheetSeparator.visibility = View.GONE
        if (isFP) {
            binding.cardImage.visibility = View.GONE
            binding.fingerSelectContainer.visibility = View.VISIBLE
        } else {
            binding.fingerprintImage.visibility = View.GONE
        }
    }

    private fun animate(){
        val old = image
        image.animate()
            .alpha(1F)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    image.animate()
                        .alpha(0.5F)
                        .setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)

                                if(old.tag == null) {
                                    old.alpha = 0F
                                }else{
                                    old.alpha = 1F
                                }
                                animate()
                            }
                        })
                }
            })
    }

    override fun onResume() {
        super.onResume()

        RfidService.unregister()
        FingerprintService.unregister()
        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
        timer?.start()
    }

    override fun onPause() {
        RfidService.unregister()
        FingerprintService.unregister()
        timer?.cancel()

        super.onPause()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val contentView = View.inflate(context, R.layout.mb_sheet_fingerprint_card_reader, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        behavior.maxWidth = 800
        return dialog
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        if (isFP) {
            restartTimer()

            if(!id.isNullOrEmpty() && finger != -1) {
                val enrollingKey = "$id|$finger"

                processEnroll(enrollingKey, template)
            }
        }
    }

    private fun restartTimer() {
        timer?.cancel()
        timer?.start()
    }

    private fun processEnroll(enrollingKey: String, template: String) {
        FingerprintService.getTemplate(enrollingKey)?.let {
            showMsg("Id already in use")
            return
        }

        if (templates.isEmpty()) {
            FingerprintService.identify(template)?.run {
                showMsg("Fingerprint already in use")
                return
            }
        } else {
            if (!FingerprintService.verify(templates[0], template)) {
                showMsg("Please use the same fingerprint")
                return
            }
        }

        templates.add(template)
        enrollCount++
        when (enrollCount) {
            0 -> showMsg("Enrolling first fingerprint")

            1 -> showMsg("Enrolling second fingerprint")

            2 -> showMsg("Enrolling third fingerprint")
        }

        if (enrollCount == 3) {
            // This function returns the merged template, which is the template saved by the FP algorithm.
            FingerprintService.enroll(enrollingKey, templates).run {
                Log.d(javaClass.simpleName, "Enrolled template $this")
                // ToDo("Save 'this' as template for fingerprint in db and server to be able to share it to different terminal")
//                httpOKService.post("http://192.168.0.45/timo_prd/services/rest/iclock/testFP", mapOf(Pair("fp", this)),{},{})
            }

            templates.clear()
            enrollCount = 0
            finger = -1

            image.tag = "Done"
            val color = activity?.resources?.getColorStateList(R.color.green, null)
            if (color != null)
                image.imageTintList = color

            showMsg("A new fingerprint has been enrolled")
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        if (!isFP) {
            timer?.cancel()
            (activity as MainActivity?)?.restartTimer()
            val rfidCode = rfidInfo.toLongOrNull(16)
            if (rfidCode != null) {
                var oct = rfidCode.toString(8)
                while (oct.length < 9) {
                    oct = "0$oct"
                }
                oct = oct.reversed()
                val paramMap = HashMap<String, String>()
                paramMap["id"] = id.toString()
                paramMap["editor"] = editor.toString()
                paramMap["card"] = oct
                viewModel.updateUser(paramMap, this)
            }
        }
    }

    private fun showFinger() {
        val handlerThread = HandlerThread("backgroundThread")
        if (!handlerThread.isAlive) handlerThread.start()
        val handler = Handler(handlerThread.looper)
        handler.postDelayed({
            activity?.runOnUiThread {
                val valueAnimator = ValueAnimator.ofInt(
                    binding.scanBottomSheet.measuredHeight,
                    binding.scanBottomSheet.measuredHeight + 300
                )
                valueAnimator.duration = 500L
                valueAnimator.addUpdateListener {
                    val animatedValue = valueAnimator.animatedValue as Int
                    val layoutParams = binding.scanBottomSheet.layoutParams
                    layoutParams.height = animatedValue
                    binding.scanBottomSheet.layoutParams = layoutParams
                }
                valueAnimator.start()
            }
        }, 1000L)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        RfidService.unregister()
        FingerprintService.unregister()
        if(isFP)
            (activity as MainActivity?)?.restartTimer()
    }

    fun afterUpdate(success: Boolean, message: String) {
        timer2.start()

        activity?.runOnUiThread {
            if (!success) {
                val color = activity?.resources?.getColorStateList(R.color.error_booking, null)
                if (color != null)
                    binding.bookingInfoContainer.backgroundTintList = color
            }
            binding.textViewBookingMessage.text = message
            val valueAnimator = ValueAnimator.ofInt(
                binding.scanBottomSheet.measuredHeight,
                binding.scanBottomSheet.measuredHeight + 50
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
                binding.bookingInfoContainer.visibility = View.VISIBLE
                binding.bookingInfoContainer.animate().alpha(1.0f)
            }
        }
    }

    companion object {
        const val TAG = "MBUserWaitSheet"
        fun newInstance(id: String?, editor: String?, isFP: Boolean) =
            MBUserWaitSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_EDITOR, editor)
                    putBoolean(ARG_IS_FP, isFP)
                }
            }

    }

    private fun showMsg(text:String){
        Utils.showMessage(parentFragmentManager, text)
    }
}