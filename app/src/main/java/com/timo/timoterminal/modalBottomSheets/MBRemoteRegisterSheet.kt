package com.timo.timoterminal.modalBottomSheets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.animation.doOnEnd
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.MainApplication
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.MbRemoteRegisterSheetBinding
import com.timo.timoterminal.fragmentViews.AttendanceFragment
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.TimoRfidListener
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.SoundSource
import com.timo.timoterminal.utils.classes.setSafeOnClickListener
import com.timo.timoterminal.viewModel.MBRemoteRegisterSheetViewModel
import com.zkteco.android.core.sdk.service.RfidService
import com.zkteco.android.lcdk.data.IFingerprintListener
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.Timer
import kotlin.concurrent.schedule

private const val ARG_ID = "id"
private const val ARG_EDITOR = "editor"
private const val ARG_IS_FP = "isFP"
private const val ARG_FINGER = "finger"
private const val ARG_COMMAND_ID = "commandId"

class MBRemoteRegisterSheet : BottomSheetDialogFragment(), TimoRfidListener, IFingerprintListener {

    private val viewModel: MBRemoteRegisterSheetViewModel by sharedViewModel()
    private lateinit var binding: MbRemoteRegisterSheetBinding
    private val languageService: LanguageService by inject()
    private val soundSource: SoundSource by inject()

    private val templates = mutableListOf<ByteArray>()
    private var enrollCount: Int = 0
    private var nDismiss: Boolean = true

    private var id: String? = null
    private var editor: String? = null
    private var isFP = false
    private var finger = -1
    private var commandId: String? = null
    private var image: ImageView? = null

    private var timer = Timer("dismissRemoteRegisterSheet", false)

    fun restartTimer() {
        timer.cancel()
        timer = Timer("dismissRemoteRegisterSheet", false)
        timer.schedule(if (isFP) 20000L else 10000L) {
            this@MBRemoteRegisterSheet.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_ID)
            editor = it.getString(ARG_EDITOR)
            isFP = it.getBoolean(ARG_IS_FP)
            finger = it.getInt(ARG_FINGER, -1)
            commandId = it.getString(ARG_COMMAND_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbRemoteRegisterSheetBinding.inflate(inflater, container, false)

        setUp()
        show()

        return binding.root
    }

    private fun setUp() {
        viewModel.viewModelScope.launch {
            binding.remoteRegisterBottomSheet.setSafeOnClickListener {
                restartTimer()
            }
            binding.buttonClose.setSafeOnClickListener {
                this@MBRemoteRegisterSheet.dismiss()
            }

            if (isFP) {
                binding.cardImage.visibility = View.GONE
                binding.fingerSelectContainer.visibility = View.VISIBLE
                checkForFP()
                binding.actionText.text = languageService.getText("#FingerOnReader")
            } else {
                binding.fingerprintImage.visibility = View.GONE
                binding.actionText.text = languageService.getText("#RFIDScanner")
            }
        }
    }

    private fun checkForFP() {
        viewModel.viewModelScope.launch {
            val color = activity?.resources?.getColorStateList(R.color.green, null)
            if (color != null) {
                MainApplication.lcdk.getFingerPrintTemplate("$id|0")?.let {
                    binding.fingerSelectArrow0.imageTintList = color
                    binding.fingerSelectArrow0.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|1")?.let {
                    binding.fingerSelectArrow1.imageTintList = color
                    binding.fingerSelectArrow1.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|2")?.let {
                    binding.fingerSelectArrow2.imageTintList = color
                    binding.fingerSelectArrow2.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|3")?.let {
                    binding.fingerSelectArrow3.imageTintList = color
                    binding.fingerSelectArrow3.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|4")?.let {
                    binding.fingerSelectArrow4.imageTintList = color
                    binding.fingerSelectArrow4.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|5")?.let {
                    binding.fingerSelectArrow5.imageTintList = color
                    binding.fingerSelectArrow5.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|6")?.let {
                    binding.fingerSelectArrow6.imageTintList = color
                    binding.fingerSelectArrow6.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|7")?.let {
                    binding.fingerSelectArrow7.imageTintList = color
                    binding.fingerSelectArrow7.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|8")?.let {
                    binding.fingerSelectArrow8.imageTintList = color
                    binding.fingerSelectArrow8.alpha = 1F
                }
                MainApplication.lcdk.getFingerPrintTemplate("$id|9")?.let {
                    binding.fingerSelectArrow9.imageTintList = color
                    binding.fingerSelectArrow9.alpha = 1F
                }
            }
            val white = activity?.resources?.getColorStateList(R.color.white, null)

            when (finger) {
                0 -> image = binding.fingerSelectArrow0
                1 -> image = binding.fingerSelectArrow1
                2 -> image = binding.fingerSelectArrow2
                3 -> image = binding.fingerSelectArrow3
                4 -> image = binding.fingerSelectArrow4
                5 -> image = binding.fingerSelectArrow5
                6 -> image = binding.fingerSelectArrow6
                7 -> image = binding.fingerSelectArrow7
                8 -> image = binding.fingerSelectArrow8
                9 -> image = binding.fingerSelectArrow9
            }
            image?.imageTintList = white
            image?.alpha = 1F
            MainApplication.lcdk.getFingerPrintTemplate("$id|$finger")?.let {
                viewModel.delFP(id, finger)
            }
            animate()

            viewModel.getUserName(id) { name ->
                binding.textViewFPUserName.text = name
                null
            }
        }
    }

    private fun animate() {
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
                                animate()
                            }
                        })
                }
            })
    }

    private fun processEnroll(enrollingKey: String, template: ByteArray) {
        if (templates.isEmpty()) {
            MainApplication.lcdk.identifyFingerPrint(template, 70).run {
                if(this.isNotEmpty()) {
                    showMsg(languageService.getText("#FPAlreadyInUse"))
                    return
                }
            }
        } else {
            if (!MainApplication.lcdk.verifyFingerPrint(templates[0], template, 70)) {
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
            MainApplication.lcdk.enrollFingerPrint(enrollingKey, templates).run {
                showLoadMask()
                viewModel.saveFP(id, finger, Base64.encodeToString(this, Base64.NO_WRAP)) { error ->
                    if (error.isEmpty()) {
                        showMsg(languageService.getText("#SavedNewFingerprint"))
                        soundSource.playSound(SoundSource.fingerSaved)
                    } else {
                        activity?.runOnUiThread {
                            Utils.showErrorMessage(requireContext(), error)
                        }
                    }
                    hideLoadMask()
                    val timer2 = Timer("remoteRegisterAfterUpdateDismiss", false)
                    timer2.schedule(5000) {
                        this@MBRemoteRegisterSheet.dismiss()
                    }
                    MainApplication.lcdk.setFingerprintListener(null)
                }
            }

            templates.clear()
            enrollCount = 0
        } else {
            soundSource.playSound(SoundSource.takeFingerAwayAndPutItOnAgain)
        }
    }


    override fun onRfidRead(text: String) {
        if (!isFP) {
            RfidService.unregister()
            timer.cancel()
            val rfidCode = text.toLongOrNull(16)
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
                        soundSource.playSound(SoundSource.successSound)
                        afterUpdate(
                            obj.getBoolean("success"),
                            obj.optString("message", "")
                        )
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

    private fun show() {
        if (isFP) {
            viewModel.viewModelScope.launch {
                val handlerThread = HandlerThread("backgroundThread")
                if (!handlerThread.isAlive) handlerThread.start()
                val handler = Handler(handlerThread.looper)
                handler.postDelayed({
                    activity?.runOnUiThread {
                        val valueAnimator = ValueAnimator.ofInt(
                            binding.remoteRegisterBottomSheet.measuredHeight,
                            binding.remoteRegisterBottomSheet.measuredHeight + 300
                        )
                        valueAnimator.duration = 500L
                        valueAnimator.addUpdateListener {
                            val animatedValue = valueAnimator.animatedValue as Int
                            val layoutParams = binding.remoteRegisterBottomSheet.layoutParams
                            layoutParams.height = animatedValue
                            binding.remoteRegisterBottomSheet.layoutParams = layoutParams
                        }
                        valueAnimator.doOnEnd {
                            val sound = when (finger) {
                                0 -> SoundSource.placeLeftLittleFinger
                                1 -> SoundSource.placeLeftRingFinger
                                2 -> SoundSource.placeLeftMiddleFinger
                                3 -> SoundSource.placeLeftIndexFinger
                                4 -> SoundSource.placeLeftThumb
                                5 -> SoundSource.placeRightThumb
                                6 -> SoundSource.placeRightIndexFinger
                                7 -> SoundSource.placeRightMiddleFinger
                                8 -> SoundSource.placeRightRingFinger
                                9 -> SoundSource.placeRightLittleFinger
                                else -> -1
                            }
                            if (sound != -1)
                                soundSource.playSound(sound)
                            binding.buttonClose.visibility = View.VISIBLE
                        }
                        valueAnimator.start()
                    }
                }, 1000L)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        nDismiss = false
        RfidService.unregister()
        MainApplication.lcdk.setFingerprintListener(null)
        val frag = parentFragmentManager.findFragmentByTag(AttendanceFragment.TAG)
        if (frag == null || !frag.isVisible) {
            (activity as MainActivity?)?.showDefault()
        } else {
            (frag as AttendanceFragment).onResume()
        }
        if (commandId != null)
            viewModel.respondForCommand(commandId!!)
    }

    override fun onResume() {
        super.onResume()

        RfidService.unregister()
        RfidService.setListener(this)
        RfidService.register()
        MainApplication.lcdk.setFingerprintListener(null)
        MainApplication.lcdk.setFingerprintListener(this)
        restartTimer()
    }

    override fun onPause() {
        if (nDismiss) {
            RfidService.unregister()
            MainApplication.lcdk.setFingerprintListener(null)
        }
        timer.cancel()

        super.onPause()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)
        val contentView = View.inflate(context, R.layout.mb_remote_register_sheet, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        behavior.maxWidth = 800
        return dialog
    }

    private fun afterUpdate(success: Boolean, message: String) {
        val timer2 = Timer("remoteRegisterAfterUpdateDismiss", false)
        timer2.schedule(5000) {
            this@MBRemoteRegisterSheet.dismiss()
        }

        activity?.runOnUiThread {
            if (!success) {
                val color = activity?.resources?.getColorStateList(R.color.error_booking, null)
                if (color != null)
                    binding.remoteRegisterInfoContainer.backgroundTintList = color
            }
            binding.textViewRemoteRegisterMessage.text = message
            val valueAnimator = ValueAnimator.ofInt(
                binding.remoteRegisterBottomSheet.measuredHeight,
                binding.remoteRegisterBottomSheet.measuredHeight + 80
            )
            valueAnimator.duration = 500L
            valueAnimator.addUpdateListener {
                val animatedValue = valueAnimator.animatedValue as Int
                val layoutParams = binding.remoteRegisterBottomSheet.layoutParams
                layoutParams.height = animatedValue
                binding.remoteRegisterBottomSheet.layoutParams = layoutParams
            }
            valueAnimator.start()
            valueAnimator.doOnEnd {
                binding.remoteRegisterInfoContainer.visibility = View.VISIBLE
                binding.remoteRegisterInfoContainer.animate().alpha(1.0f)
            }
        }
    }

    companion object {
        const val TAG = "MBRemoteRegisterSheet"

        fun newInstance(
            id: String?,
            editor: String?,
            isFP: Boolean,
            finger: Int = -1,
            commandId: String
        ) =
            MBRemoteRegisterSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_EDITOR, editor)
                    putBoolean(ARG_IS_FP, isFP)
                    putInt(ARG_FINGER, finger)
                    putString(ARG_COMMAND_ID, commandId)
                }
            }

    }

    private fun showMsg(text: String) {
        requireActivity().runOnUiThread {
            Utils.showMessage(parentFragmentManager, text)
        }
    }

    private fun showLoadMask() {
        timer.cancel()
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
    }

    override fun onFingerprintPressed(template: ByteArray): Boolean {
        if (isFP) {
            restartTimer()

            if (!id.isNullOrEmpty() && finger != -1) {
                val enrollingKey = "$id|$finger"

                processEnroll(enrollingKey, template)
            }
        }
        return true
    }

    override fun onFingerprintPressed(template: ByteArray, bmp: Bitmap?) {}
}