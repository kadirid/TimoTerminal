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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.animation.doOnEnd
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.fragmentViews.UserSettingsFragment
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MBUserWaitSheetViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

private const val ARG_ID = "id"
private const val ARG_EDITOR = "editor"
private const val ARG_IS_FP = "isFP"
private const val ARG_IS_DELETE = "isDelete"

class MBUserWaitSheet : BottomSheetDialogFragment(), TimoRfidListener, FingerprintListener {

    private var enrollCount: Int = 0
    private val viewModel: MBUserWaitSheetViewModel by sharedViewModel()
    private lateinit var binding: MbSheetFingerprintCardReaderBinding
    private val languageService: LanguageService by inject()
    private val soundSource: SoundSource by inject()

    private val templates = mutableListOf<String>()
    private var nDismiss: Boolean = true

    private var image: ImageView? = null
    private var id: String? = null
    private var editor: String? = null
    private var isFP = false
    private var isDelete = false
    private var finger = -1
    private var timer: CountDownTimer? = null
    private val timer2 = object : CountDownTimer(5000, 4000) {
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
        viewModel.viewModelScope.launch {
            if (isFP) {
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
                    binding.textViewFPExplanation.visibility = View.VISIBLE
                    binding.textViewFPExplanation.text =
                        languageService.getText("#SelectFingerForRegister")
                    soundSource.playSound(SoundSource.selectFinger)
                }
            }
            binding.buttonClose.setSafeOnClickListener {
                this@MBUserWaitSheet.dismiss()
            }
        }
    }

    private fun processFingerClickListener(it: View?, fingerNo: Int) {
        restartTimer()
        image = (it as ImageView)
        finger = fingerNo
        if (!isDelete) {
            animate()
            binding.textViewFPExplanation.text = languageService.getText("#FingerOnReader")
            soundSource.playSound(SoundSource.placeFingerBase + finger)
        }
        if (isDelete && it.imageTintList != null) {
            val dlgAlert: AlertDialog.Builder =
                AlertDialog.Builder(requireContext(), R.style.MyDialog)
            dlgAlert.setMessage(languageService.getText("#ReallyDeleteFP"))
            dlgAlert.setTitle(languageService.getText("#Attention"))
            dlgAlert.setIcon(
                AppCompatResources.getDrawable(
                    requireContext(), R.drawable.baseline_info_24
                )
            )
            dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
            dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                showLoadMask()
                viewModel.delFP(id, fingerNo) { error ->
                    if (error.isEmpty()) {
                        requireActivity().runOnUiThread {
                            it.imageTintList = null
                            it.alpha = 0F
                        }
                    } else {
                        Utils.showErrorMessage(requireContext(), error)
                    }
                    hideLoadMask()
                }
            }
            val dialog = dlgAlert.create()
            Utils.hideNavInDialog(dialog)
            dialog.setOnShowListener {
                val textView = dialog.findViewById<TextView>(android.R.id.message)
                textView?.textSize = 30f

                val imageView = dialog.findViewById<ImageView>(android.R.id.icon)
                val params = imageView?.layoutParams
                params?.height = 48
                params?.width = 48
                imageView?.layoutParams = params
            }
            dialog.show()
            dialog.window?.setLayout(680, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun setValues() {
        viewModel.viewModelScope.launch {
            timer = object : CountDownTimer(if (isFP) 20000 else 10000, 9000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    dismiss()
                }
            }

            binding.identificationText.visibility = View.GONE
            binding.keyboardImage.visibility = View.GONE
            if (isFP) {
                binding.cardImage.visibility = View.GONE
                binding.fingerSelectContainer.visibility = View.VISIBLE
                checkForFP()
            } else {
                binding.fingerprintImage.visibility = View.GONE
            }
        }
    }

    private fun checkForFP() {
        viewModel.viewModelScope.launch {
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
    }

    private fun animate() {
        val old = image
        image?.animate()
            ?.alpha(1F)
            ?.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)

                    image?.animate()
                        ?.alpha(0.5F)
                        ?.setListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                super.onAnimationEnd(animation)


                                if (old?.imageTintList == null) {
                                    old?.alpha = 0F
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
        if (nDismiss) {
            RfidService.unregister()
            FingerprintService.unregister()
        }
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
            val dlgAlert: AlertDialog.Builder =
                AlertDialog.Builder(requireContext(), R.style.MyDialog)
            dlgAlert.setMessage(languageService.getText("#FPExistsDeleteAndRecreate"))
            dlgAlert.setTitle(languageService.getText("#Attention"))
            dlgAlert.setNegativeButton(languageService.getText("BUTTON#Gen_Cancel")) { dia, _ -> dia.dismiss() }
            dlgAlert.setPositiveButton(languageService.getText("ALLGEMEIN#ok")) { _, _ ->
                viewModel.delFP(id, finger) { error ->
                    if (error.isEmpty()) {
                        activity?.runOnUiThread {
                            processEnroll(enrollingKey, template)
                        }
                    } else {
                        Utils.showErrorMessage(requireContext(), error)
                    }
                    hideLoadMask()
                }
            }
            val dialog = dlgAlert.create()
            Utils.hideNavInDialog(dialog)
            dialog.setOnShowListener {
                Utils.hideNavInDialog(this.dialog)
                val textView = dialog.findViewById<TextView>(android.R.id.message)
                textView?.textSize = 30f
            }
            dialog.setOnDismissListener {
                Utils.hideNavInDialog(this.dialog)
            }
            dialog.show()
            dialog.window?.setLayout(680, 354)
            return
        }

        if (templates.isEmpty()) {
            FingerprintService.identify(template)?.run {
                showMsg(languageService.getText("#FPAlreadyInUse"))
                return
            }
        } else {
            if (!FingerprintService.verify(templates[0], template)) {
                soundSource.playSound(SoundSource.placeSameFingerAgain)
                showMsg(languageService.getText("#PleaseSameFinger"))
                return
            }
        }

        templates.add(template)
        when (enrollCount) {
            0 -> showMsg(languageService.getText("#SaveFirstFP"))

            1 -> showMsg(languageService.getText("#SaveSecondFP"))

            2 -> showMsg(languageService.getText("#SaveThirdFP"))
        }
        enrollCount++

        if (enrollCount == 3) {
            soundSource.playSound(SoundSource.takeFingerAway)
            // This function returns the merged template, which is the template saved by the FP algorithm.
            FingerprintService.enroll(enrollingKey, templates).run {
                showLoadMask()
                viewModel.saveFP(id, finger, this) { error ->
                    if (error.isEmpty()) {
                        requireActivity().runOnUiThread {
                            val color = activity?.resources?.getColorStateList(R.color.green, null)
                            if (color != null)
                                image?.imageTintList = color
                        }

                        showMsg(languageService.getText("#SavedNewFingerprint"))
                        soundSource.playSound(SoundSource.fingerSaved)
                    } else {
                        activity?.runOnUiThread {
                            Utils.showErrorMessage(requireContext(), error)
                        }
                    }
                    hideLoadMask()
                }
            }

            templates.clear()
            enrollCount = 0
            finger = -1
        } else {
            soundSource.playSound(SoundSource.takeFingerAwayAndPutItOnAgain)
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
                viewModel.updateUser(paramMap) { obj ->
                    if (obj.optString("error", "").isEmpty()) {
                        afterUpdate(
                            obj.getBoolean("success"),
                            obj.optString("message", "")
                        )
                        RfidService.unregister()
                        FingerprintService.unregister()
                    } else {
                        activity?.runOnUiThread {
                            Utils.showErrorMessage(requireContext(), obj.getString("error"))
                        }
                    }
                    hideLoadMask()
                }
            }
        }
    }

    private fun showFinger() {
        viewModel.viewModelScope.launch {
            val handlerThread = HandlerThread("backgroundThread")
            if (!handlerThread.isAlive) handlerThread.start()
            val handler = Handler(handlerThread.looper)
            handler.postDelayed({
                activity?.runOnUiThread {
                    val valueAnimator = ValueAnimator.ofInt(
                        binding.scanBottomSheet.measuredHeight,
                        binding.scanBottomSheet.measuredHeight + if (isFP) 300 else 0
                    )
                    valueAnimator.duration = if (isFP) 500L else 0L
                    valueAnimator.addUpdateListener {
                        val animatedValue = valueAnimator.animatedValue as Int
                        val layoutParams = binding.scanBottomSheet.layoutParams
                        layoutParams.height = animatedValue
                        binding.scanBottomSheet.layoutParams = layoutParams
                    }
                    valueAnimator.doOnEnd {
                        binding.buttonClose.visibility = View.VISIBLE
                    }
                    valueAnimator.start()
                }
            }, if (isFP) 1000L else 10L)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        nDismiss = false
        RfidService.unregister()
        FingerprintService.unregister()
        val frag = parentFragmentManager.findFragmentByTag(UserSettingsFragment.TAG)
        if (frag != null && frag.isVisible) {
            (frag as UserSettingsFragment).onResume()
        }
        (activity as MainActivity?)?.restartTimer()
    }

    private fun afterUpdate(success: Boolean, message: String) {
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
        fun newInstance(
            id: String?,
            editor: String?,
            isFP: Boolean,
            isDelete: Boolean = false,
        ) =
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
        this.dialog?.setCanceledOnTouchOutside(false)
        activity?.runOnUiThread {
            binding.sheetLayoutLoadMaks.visibility = View.VISIBLE
        }
    }

    private fun hideLoadMask() {
        this.dialog?.setCanceledOnTouchOutside(true)
        activity?.runOnUiThread {
            binding.sheetLayoutLoadMaks.visibility = View.GONE
        }
        timer?.start()
    }
}