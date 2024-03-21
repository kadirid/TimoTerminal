package com.timo.timoterminal.modalBottomSheets

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import com.timo.timoterminal.R
import com.timo.timoterminal.components.Gauge.GaugeAnimation
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.serviceUtils.UserInformation
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.InfoFragmentViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.GregorianCalendar

class MBFragmentInfoSheet : BottomSheetDialogFragment() {
    private val languageService: LanguageService by inject()

    private lateinit var binding: FragmentInfoMessageSheetItemBinding

    val viewModel by sharedViewModel<InfoFragmentViewModel>()
    private var card: String = ""
    private var res: UserInformation? = null;

    companion object {
        const val TAG = "MBFragmentInfoSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoMessageSheetItemBinding.inflate(inflater, container, false)

        res = arguments?.getParcelable<UserInformation>("res")!!
        card = arguments?.getString("card") ?: ""

        if (res != null) setText(res!!)

        viewModel.viewModelScope.launch {
            viewModel.liveDismissSheet.value = false
            viewModel.liveDismissSheet.observe(viewLifecycleOwner) {
                if (it == true) {
                    this@MBFragmentInfoSheet.dismiss()
                    viewModel.liveDismissSheet.value = false
                }
            }
            viewModel.liveShowSeconds.value = ""
            viewModel.liveShowSeconds.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    binding.textviewSecondClose.text = it
                    viewModel.liveShowSeconds.value = ""
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        binding.linearTextContainer.setOnClickListener {
            viewModel.restartTimer()
        }

        super.onResume()
    }

    @SuppressLint("SetTextI18n")
    private fun setText(res: UserInformation) {
        viewModel.viewModelScope.launch {
            binding.textViewCurrentDay.text = languageService.getText("ALLGEMEIN#Aktueller Tag")
            binding.textViewCurretLeave.text = languageService.getText("ALLGEMEIN#Urlaub")

            binding.textViewInformation.text = getText("#ActualInformation") + " RFID: $card"
            binding.textViewRfid.text = res.user
            var ist = res.ist.toDouble()
            if (res.zeitTyp in listOf(1, 4, 6) && res.zeitLB.toString().isNotEmpty()
            ) {
                val gcDate = Utils.getCal()
                val greg = GregorianCalendar()
                greg.time = res.zeitLB;

                var diff = gcDate.timeInMillis - greg.timeInMillis
                diff /= 1000
                diff /= 60
                ist += diff
            }
            binding.textviewTimeTarget.text = "${getText("#Target")}: ${res.soll}"
            binding.textviewTimeActual.text =
                "${getText("ALLGEMEIN#Ist")}: ${Utils.convertTime(ist)}"
            binding.textviewTimeStartOfWork.text =
                "${getText("#CheckIn")}: ${res.kommen}"
            binding.textviewTimeBreakTotal.text =
                "${getText("ALLGEMEIN#Pause")}: ${res.pause}"
            binding.textviewTimeEndOfWork.text =
                "${getText("#CheckOut")}: ${res.gehen}"
            binding.textviewTimeOvertime.text =
                "${getText("PDFSOLLIST#spalteGzGleitzeit")}: ${res.overtime}"

            //Populate right side of the sheet
            binding.textviewVacationEntitlement.text = getText("ALLGEMEIN#Anspruch")
            binding.textviewVacationEntitlementValue.text = res.vacation

            binding.textviewVacationTaken.text = getText("ALLGEMEIN#Genommen")
            binding.textviewVacationTakenValue.text = res.gVacation
            val gVacation = res.gVacation.replace(",", ".").toFloat()

            binding.textviewVacationRequested.text = getText("ALLGEMEIN#Beantragt")
            binding.textviewVacationRequestedValue.text = res.bVacation
            val bVacation = res.bVacation.replace(",", ".").toFloat()

            binding.textviewVacationRemaining.text = getText("#Remaining")
            binding.textviewVacationRemainingValue.text = res.rVacation
            val rVacation = res.rVacation.replace(",", ".").toFloat()


            val gaugeAnimator = GaugeAnimation(binding.gaugeVacation, 80f).apply {
                onAnimationEnd = {
                    val anim = GaugeAnimation(binding.gaugeVacation, 40f);
                    binding.gaugeVacation.startAnimation(anim);
                }
            }
            binding.gaugeVacation.startAnimation(gaugeAnimator);
            val map = LinkedHashMap<Float, Int>()
            map[bVacation] = Color.rgb(255, 165, 0)
            map[gVacation] = Color.rgb(0, 255, 128)
            map[rVacation] = MaterialColors.getColor(
                requireContext(),
                R.attr.colorSurfaceContainerHighest,
                resources.getColor(R.color.black)
            )
            binding.gaugeVacation.setData(map)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
        Utils.hideNavInDialog(dialog)
        val contentView = View.inflate(context, R.layout.fragment_info_message_sheet_item, null)
        dialog.setContentView(contentView)

        val behavior = dialog.behavior
        behavior.peekHeight = 999999
        behavior.maxWidth = 900
        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        viewModel.dismissInfoSheet()
    }

    private fun getText(key: String) = languageService.getText(key)
}