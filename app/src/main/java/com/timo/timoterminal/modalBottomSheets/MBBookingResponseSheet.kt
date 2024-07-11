package com.timo.timoterminal.modalBottomSheets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.MbBookingResponseBinding
import com.timo.timoterminal.enums.SharedPreferenceKeys
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.SharedPrefService
import com.timo.timoterminal.utils.Utils
import org.koin.android.ext.android.inject
import java.util.Timer
import kotlin.concurrent.schedule

class MBBookingResponseSheet : BottomSheetDialogFragment() {
    private val languageService: LanguageService by inject()
    private val sharedPrefService: SharedPrefService by inject()

    private lateinit var binding: MbBookingResponseBinding

    private var timer = Timer("closeBookingResponseSheet", false)
    private var timerLength = sharedPrefService.getLong(SharedPreferenceKeys.BOOKING_MESSAGE_TIMEOUT_IN_SEC, 5)*1000

    companion object {
        const val TAG = "MBBookingResponseSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MbBookingResponseBinding.inflate(inflater, container, false)

        setListeners()
        setView()
        timer.schedule(timerLength) {
            this@MBBookingResponseSheet.dismiss()
        }
        return binding.root
    }

    private fun setListeners() {
        binding.buttonClose.setOnClickListener {
            timer.cancel()
            this@MBBookingResponseSheet.dismiss()
        }
    }

    private fun setView() {
        var image: Int
        val status = arguments?.getInt("status", -1)
        val adjusted = arguments?.getBoolean("adjusted", false) == true
        val offline = arguments?.getBoolean("offline", false) == true
        val failure = arguments?.getBoolean("success", true) == false
        val message = arguments?.getString("message") ?: ""

        val successText = binding.bookingMessageSuccessTypeTextView
        val infoMessage = binding.bookingMessageInfoMessageTextView
        val userText = binding.bookingMessageUserTextView
        val timeText = binding.bookingMessageTimeTextView

        when(status){
            1 -> {
                image = R.drawable.booking_in
                successText.text = languageService.getText("#CheckIn")
            }
            2 -> {
                image = R.drawable.booking_out
                successText.text = languageService.getText("#CheckOut")
            }
            3 -> {
                image = R.drawable.booking_break_start
                successText.text = languageService.getText("ALLGEMEIN#Pausenanfang")
            }
            4 -> {
                image = R.drawable.booking_break_end
                successText.text = languageService.getText("ALLGEMEIN#Pausenende")
            }
            else -> image = R.drawable.booking_error
        }

        val lang = sharedPrefService.getString(SharedPreferenceKeys.LANGUAGE, "")
        val timeAddon = if(lang == "de") " Uhr" else ""

        if(adjusted){
            image = R.drawable.booking_change
        }

        if(failure){
            binding.bookingMessageContainer.setBackgroundResource(R.drawable.booking_failure_bg)
            userText.visibility = View.GONE
            timeText.visibility = View.GONE
            successText.visibility = View.GONE
            image = R.drawable.booking_error

            val errorText = binding.bookingMessageErrorTypeTextView
            errorText.visibility = View.VISIBLE
            errorText.text = arguments?.getString("error") ?: ""
            infoMessage.text = message
        }else{
            userText.text = arguments?.getString("name") ?: ""
        }

        val timeTextValue = Utils.getTimeFromGC(Utils.getCal()) + timeAddon
        if(adjusted){
            timeText.visibility = View.GONE
            infoMessage.visibility = View.GONE
            successText.text = languageService.getText("#BookingWasAdjusted")
            binding.bookingMessageAdjustContainer.visibility = View.VISIBLE

            val oldType = when(status){
                1 -> languageService.getText("#CheckIn")
                2 -> languageService.getText("#CheckOut")
                else -> languageService.getText("ALLGEMEIN#Pause")
            } + " (${languageService.getText("#Old")}):"
            val newType = "${languageService.getText("ALLGEMEIN#Buchung nachher")}:"
            val newTime = message + timeAddon

            binding.bookingMessageAdjustBookingOldType.text = oldType
            binding.bookingMessageAdjustBookingOldValue.text = timeTextValue
            binding.bookingMessageAdjustBookingNewType.text = newType
            binding.bookingMessageAdjustBookingNewValue.text = newTime
        }

        if(!offline && message.isNullOrEmpty()){
            infoMessage.visibility = View.GONE
        }

        if(!adjusted && !failure){
            timeText.text = timeTextValue
            infoMessage.text = if(offline){
                languageService.getText("#BookingTemporarilySaved")
            }else{
                languageService.getText("#Data was saved successfully")
            }
        }

        binding.bookingImageView.setImageResource(image)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)

        val contentView = View.inflate(context, R.layout.mb_booking_response, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        behavior.maxWidth = 800
        return dialog
    }

}