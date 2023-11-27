package com.timo.timoterminal.modalBottomSheets

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.timo.timoterminal.R
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import org.json.JSONObject
import org.koin.android.ext.android.inject
import java.util.GregorianCalendar

class MBFragmentInfoSheet : BottomSheetDialogFragment() {
    private val languageService:LanguageService by inject()

    private lateinit var binding: FragmentInfoMessageSheetItemBinding

    var viewModel: InfoFragmentViewModel? = null
    private var card: String = ""
    private var res: String = ""
    companion object {
        const val TAG = "MBFragmentInfoSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoMessageSheetItemBinding.inflate(inflater, container, false)

        res = arguments?.getString("res") ?: ""
        card = arguments?.getString("card") ?: ""
        setText()

        return binding.root
    }

    override fun onResume() {
        if(viewModel!=null){
            binding.linearTextContainer.setOnClickListener {
                viewModel!!.restartTimer()
            }
        }

        super.onResume()
    }
    @SuppressLint("SetTextI18n")
    private fun setText() {
        binding.textViewCurrentDay.text = languageService.getText("ALLGEMEIN#Aktueller Tag")
        binding.textViewCurretLeave.text = languageService.getText("ALLGEMEIN#Urlaub")
        val res = JSONObject(res)

        binding.textViewRfid.text = "RFID: $card"
        var ist = res.getDouble("ist")
        if (res.optInt("zeitTyp", 0) in listOf(1, 4, 6) && !res.optString("zeitLB", "")
                .isNullOrBlank()
        ) {
            val gcDate = GregorianCalendar()
            val greg = Utils.parseDateTime(res.getString("zeitLB"))
            var diff = gcDate.timeInMillis - greg.timeInMillis
            diff /= 1000
            diff /= 60
            ist += diff
        }
        binding.textviewTimeTarget.text = "${getText("#Target")}: ${res.getString("soll")}"
        binding.textviewTimeActual.text = "${getText("ALLGEMEIN#Ist")}: ${Utils.convertTime(ist)}"
        binding.textviewTimeStartOfWork.text =
            "${getText("#CheckIn")}: ${res.getString("kommen")}"
        binding.textviewTimeBreakTotal.text =
            "${getText("ALLGEMEIN#Pause")}: ${res.getString("pause")}"
        binding.textviewTimeEndOfWork.text =
            "${getText("#CheckOut")}: ${res.getString("gehen")}"
        binding.textviewTimeOvertime.text =
            "${getText("PDFSOLLIST#spalteGzGleitzeit")}: ${res.getString("overtime")}"
        binding.textviewVacationEntitlement.text =
            "${getText("ALLGEMEIN#Anspruch")}: ${res.getString("vacation")}"
        binding.textviewVacationTaken.text =
            "${getText("ALLGEMEIN#Genommen")}: ${res.getString("gVacation")}"
        binding.textviewVacationRequested.text =
            "${getText("ALLGEMEIN#Beantragt")}: ${res.getString("bVacation")}"
        binding.textviewVacationRemaining.text =
            "${getText("#Remaining")}: ${res.getString("rVacation")}"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext())
        val contentView = View.inflate(context, R.layout.fragment_info_message_sheet_item, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        behavior.maxWidth = 900
        return dialog
    }

    fun getBinding(): FragmentInfoMessageSheetItemBinding {
        return binding
    }

    private fun getText(key: String) = languageService.getText(key)
}