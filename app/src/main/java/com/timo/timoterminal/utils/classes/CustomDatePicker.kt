package com.timo.timoterminal.utils.classes

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.wdullaer.materialdatetimepicker.R
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog

class CustomDatePicker : DatePickerDialog() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val header = view?.findViewById<LinearLayout>(R.id.mdtp_day_picker_selected_date_layout)
        if (isThemeDark) {
            header?.setBackgroundColor(
                Color.parseColor("#535353")
            )
        }
        val layout = header?.layoutParams
        if(layout != null) {
            layout.width = 220
            header.layoutParams = layout
        }

        return view
    }

    companion object {
        fun newInstance(
            callBack: OnDateSetListener, year: Int, monthOfYear: Int, dayOfMonth: Int
        ): CustomDatePicker {
            val ret = CustomDatePicker()
            ret.initialize(callBack, year, monthOfYear, dayOfMonth)
            return ret
        }
    }
}