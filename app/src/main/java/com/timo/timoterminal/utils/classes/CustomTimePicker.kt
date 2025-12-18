package com.timo.timoterminal.utils.classes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import com.wdullaer.materialdatetimepicker.R

class CustomTimePicker : TimePickerDialog() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val timePickerHeader = view?.findViewById<RelativeLayout>(R.id.mdtp_time_display)
        if (isThemeDark) {
            timePickerHeader?.setBackgroundColor(
                Color.parseColor("#535353")
            )
        }

        return view
    }

    companion object {
        fun newInstance(callback: OnTimeSetListener, hours: Int, minutes: Int, is24Hours: Boolean): CustomTimePicker {
            val timePickerDialog = CustomTimePicker()
            timePickerDialog.initialize(callback, hours, minutes, 0, is24Hours)
            return timePickerDialog
        }
    }
}