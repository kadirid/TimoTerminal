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
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.doOnEnd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.UserService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MBUserWaitSheetViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import org.koin.android.ext.android.inject

private const val ARG_ID = "id"
private const val ARG_EDITOR = "editor"
private const val ARG_IS_FP = "isFP"
private const val ARG_IS_DELETE = "isDelete"

class MBUserWaitSheet : BottomSheetDialogFragment(), RfidListener, FingerprintListener {

    private var enrollCount: Int = 0
    private val userService: UserService by inject()
    private val viewModel = MBUserWaitSheetViewModel(userService)
    private lateinit var binding: MbSheetFingerprintCardReaderBinding
    private val languageService: LanguageService by inject()

    private val templates = mutableListOf<String>()

    private lateinit var image: ImageView
    private var id: String? = null
    private var editor: String? = null
    private var isFP = false
    private var isDelete = false
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
            isDelete = it.getBoolean(ARG_IS_DELETE)
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
        showFinger()

        return binding.root
    }

    private fun setOnClickListeners() {
        if (isFP) {
            image = binding.fingerSelectArrow6
            binding.fingerSelectArrow0.setSafeOnClickListener {
                processFingerClickListener(it, 0)
            }
            binding.fingerSelectArrow1.setSafeOnClickListener {
                processFingerClickListener(it, 1)
            }
            binding.fingerSelectArrow2.setSafeOnClickListener {
                processFingerClickListener(it, 2)
            }
            binding.fingerSelectArrow3.setSafeOnClickListener {
                processFingerClickListener(it, 3)
            }
            binding.fingerSelectArrow4.setSafeOnClickListener {
                processFingerClickListener(it, 4)
            }
            binding.fingerSelectArrow5.setSafeOnClickListener {
                processFingerClickListener(it, 5)
            }
            binding.fingerSelectArrow6.setSafeOnClickListener {
                processFingerClickListener(it, 6)
            }
            binding.fingerSelectArrow7.setSafeOnClickListener {
                processFingerClickListener(it, 7)
            }
            binding.fingerSelectArrow8.setSafeOnClickListener {
                processFingerClickListener(it, 8)
            }
            binding.fingerSelectArrow9.setSafeOnClickListener {
                processFingerClickListener(it, 9)
            }
            if (!isDelete) {
                animate()
            }
        }
    }

    private fun processFingerClickListener(it: View?, fingerNo: Int) {
        restartTimer()
        if ((it as ImageView).imageTintList == null) {
            image = it
            finger = fingerNo
        }
        if (isDelete) {
            val dlgAlert: AlertDialog.Builder =
                AlertDialog.Builder(requireContext(), R.style.MyDialog)
            dlgAlert.setMessage(languageService.getText("#ReallyDeleteFP"))
            dlgAlert.setTitle(languageService.getText("#Attention"))
            dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
            dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                showLoadMask()
                viewModel.delFP(id, fingerNo, this) {
                    requireActivity().runOnUiThread {
                        it.imageTintList = null
                        it.alpha = 0F
                    }
                }
            }
            val dialog = dlgAlert.create()
            Utils.hideNavInDialog(dialog)
            dialog.setOnShowListener {
                val textView = dialog.findViewById<TextView>(android.R.id.message)
                textView?.textSize = 40f
            }
            dialog.show()
            dialog.window?.setLayout(680, 324)
        }
    }

    private fun setValues() {
        timer = object : CountDownTimer(if (isFP) 20000 else 10000, 5000) {
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
            checkForFP()
        } else {
            binding.fingerprintImage.visibility = View.GONE
        }
    }

    private fun checkForFP() {
        val color = activity?.resources?.getColorStateList(R.color.green, null)
        if (color != null) {
            FingerprintService.getTemplate("$id|0")?.let {
                binding.fingerSelectArrow0.imageTintList = color
                binding.fingerSelectArrow0.alpha = 1F
            }
            FingerprintService.getTemplate("$id|1")?.let {
                binding.fingerSelectArrow1.imageTintList = color
                binding.fingerSelectArrow1.alpha = 1F
            }
            FingerprintService.getTemplate("$id|2")?.let {
                binding.fingerSelectArrow2.imageTintList = color
                binding.fingerSelectArrow2.alpha = 1F
            }
            FingerprintService.getTemplate("$id|3")?.let {
                binding.fingerSelectArrow3.imageTintList = color
                binding.fingerSelectArrow3.alpha = 1F
            }
            FingerprintService.getTemplate("$id|4")?.let {
                binding.fingerSelectArrow4.imageTintList = color
                binding.fingerSelectArrow4.alpha = 1F
            }
            FingerprintService.getTemplate("$id|5")?.let {
                binding.fingerSelectArrow5.imageTintList = color
                binding.fingerSelectArrow5.alpha = 1F
            }
            FingerprintService.getTemplate("$id|6")?.let {
                binding.fingerSelectArrow6.imageTintList = color
                binding.fingerSelectArrow6.alpha = 1F
            }
            FingerprintService.getTemplate("$id|7")?.let {
                binding.fingerSelectArrow7.imageTintList = color
                binding.fingerSelectArrow7.alpha = 1F
            }
            FingerprintService.getTemplate("$id|8")?.let {
                binding.fingerSelectArrow8.imageTintList = color
                binding.fingerSelectArrow8.alpha = 1F
            }
            FingerprintService.getTemplate("$id|9")?.let {
                binding.fingerSelectArrow9.imageTintList = color
                binding.fingerSelectArrow9.alpha = 1F
            }
        }
    }

    private fun animate() {
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


                                if (old.imageTintList == null) {
                                    old.alpha = 0F
                                } else {
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
        Utils.hideNavInDialog(dialog)
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
        if (isFP && !isDelete) {
            restartTimer()

            if (!id.isNullOrEmpty() && finger != -1) {
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
                showLoadMask()
                viewModel.saveFP(id, finger, this, this@MBUserWaitSheet) {
                    requireActivity().runOnUiThread {
                        val color = activity?.resources?.getColorStateList(R.color.green, null)
                        if (color != null)
                            image.imageTintList = color
                    }

                    showMsg("A new fingerprint has been enrolled")
                }
            }

            templates.clear()
            enrollCount = 0
            finger = -1
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        if (!isFP) {
            timer?.cancel()
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
                showLoadMask()
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
                    binding.scanBottomSheet.measuredHeight + if (isFP) 300 else 30
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
        fun newInstance(id: String?, editor: String?, isFP: Boolean, isDelete: Boolean = false) =
            MBUserWaitSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_EDITOR, editor)
                    putBoolean(ARG_IS_FP, isFP)
                    putBoolean(ARG_IS_DELETE, isDelete)
                }
            }

    }

    private fun showMsg(text: String) {
        requireActivity().runOnUiThread {
            Utils.showMessage(parentFragmentManager, text)
        }
    }

    private fun showLoadMask() {
        timer?.cancel()
        activity?.runOnUiThread {
            binding.sheetLayoutLoadMaks.visibility = View.VISIBLE
        }
    }

    fun hideLoadMask() {
        activity?.runOnUiThread {
            binding.sheetLayoutLoadMaks.visibility = View.GONE
        }
        timer?.start()
    }
}