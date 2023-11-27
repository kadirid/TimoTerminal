package com.timo.timoterminal.modalBottomSheets

import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.timo.timoterminal.R
import com.timo.timoterminal.activities.MainActivity
import com.timo.timoterminal.databinding.MbSheetFingerprintCardReaderBinding
import com.timo.timoterminal.service.UserService
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

    private val userService: UserService by inject()
    private val viewModel = MBUserWaitSheetViewModel(userService)
    private lateinit var binding: MbSheetFingerprintCardReaderBinding

    private var id: String? = null
    private var editor: String? = null
    private var isFP = false
    private val timer = object : CountDownTimer(10000, 5000) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dismiss()
        }
    }
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

        return binding.root
    }

    private fun setValues() {
        binding.identificationText.visibility = View.GONE
        binding.keyboardImage.visibility = View.GONE
        binding.nameContainer.visibility = View.GONE
        binding.bookingTypeTextContainer.visibility = View.GONE
        binding.progressContainer.visibility = View.GONE
        binding.timeTextContainer.visibility = View.GONE
        binding.sheetSeparator.visibility = View.GONE
        if (isFP) {
            binding.cardImage.visibility = View.GONE
        } else {
            binding.fingerprintImage.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
        timer.start()
    }

    override fun onPause() {
        RfidService.unregister()
        FingerprintService.unregister()
        timer.cancel()

        super.onPause()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val contentView = View.inflate(context, R.layout.mb_sheet_fingerprint_card_reader, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        return dialog
    }

    override fun onFingerprintPressed(
        fingerprint: String, template: String, width: Int, height: Int
    ) {
        if (isFP) {
            timer.cancel()
            (activity as MainActivity?)?.restartTimer()
            Snackbar.make(binding.root, "Not implemented!", Snackbar.LENGTH_LONG).show()
//            TODO("Not yet implemented")
        }
    }

    override fun onRfidRead(rfidInfo: String) {
        if (!isFP) {
            timer.cancel()
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
}