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
import com.timo.timoterminal.databinding.FragmentInfoMessageSheetItemBinding
import com.timo.timoterminal.service.LanguageService
import com.timo.timoterminal.service.serviceUtils.classes.Event
import com.timo.timoterminal.service.serviceUtils.classes.UserInformation
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.utils.classes.BGData
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
    private var res: UserInformation? = null

    companion object {
        const val TAG = "MBFragmentInfoSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoMessageSheetItemBinding.inflate(inflater, container, false)

        res = arguments?.getParcelable("res")!!
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

            binding.textViewInformationValue.text = card
            binding.textViewName.text = res.user
            var ist = res.ist.toDouble()
            if (res.zeitTyp in listOf(1, 4, 6) && res.zeitLB.toString().isNotEmpty()
            ) {
                val gcDate = Utils.getCal()
                val greg = GregorianCalendar()
                greg.time = res.zeitLB

                var diff = gcDate.timeInMillis - greg.timeInMillis
                diff /= 1000
                diff /= 60
                ist += diff
            }
            binding.textviewTimeTarget.text = getText("#Target")
            binding.textviewTimeTargetValue.text = res.soll
            val targetMinutes = Utils.parseTimeToMinutes(res.soll, "HH:mm")

            binding.textviewTimeActual.text = getText("ALLGEMEIN#Ist")
            binding.textviewTimeActualValue.text = Utils.convertTime(ist)
            val actualMinutes = ist


            val listSollIst = ArrayList<BGData>()
            listSollIst.add(BGData(actualMinutes.toFloat(), Color.rgb(0, 255, 128)))
            if (targetMinutes - actualMinutes > 0) {
                listSollIst.add(BGData((targetMinutes - actualMinutes).toFloat(), MaterialColors.getColor(
                    requireContext(),
                    R.attr.colorSurfaceContainerHighest,
                    resources.getColor(R.color.black,null)
                )))
            }
            binding.gaugeTime.setData(listSollIst)

            binding.textviewTimeOvertime.text =getText("PDFSOLLIST#spalteGzGleitzeit")
            binding.textviewTimeOvertimeValue.text =res.overtime

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
            if (rVacation < 0f ) {
                binding.textviewVacationRemainingValue.setTextColor(Color.RED)
            }

            val list = ArrayList<BGData>()

            list.add(BGData(gVacation, Color.rgb(0, 255, 128)))
            list.add(BGData(bVacation, Color.rgb(255, 165, 0)))
            if (rVacation > 0 ) {
                list.add(
                    BGData(
                        rVacation, MaterialColors.getColor(
                            requireContext(),
                            R.attr.colorSurfaceContainerHighest,
                            resources.getColor(R.color.black,null)
                        )
                    )
                )
            }
            binding.gaugeVacation.setData(list)

            //Before visualizing the events in the bar, we need to separate bookings to each day, so
            // we can determine what events we have to show today. We receive Events from 2 Days till
            // today..
            val evs = ArrayList<Event>()
            res.event.sortedWith { a, b ->
                when {
                    a.StartDate!!.before(b.StartDate) -> -1
                    b.StartDate!!.before(a.StartDate) -> 1
                    else -> 0
                }
            }.forEach {
                if (Utils.isSameDay(it.StartDate!!, it.EndDate!!)) {
                    evs.add(it)
                } else {
                    val ev = Event.splitEventToDaily(it)
                    evs.addAll(ev)
                }
            }
            val todayEvs = evs.filter { Utils.isToday(it.StartDate!!) && it.StartDate != it.EndDate }
            loadAttendanceBar(ArrayList(todayEvs))

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

    private fun loadAttendanceBar(events: ArrayList<Event>) {
        val list2 = ArrayList<BGData>()
        val msPerDay = 86400000
        var timethreshold = 0f
        events.forEach {
            run {
                if (it.buchungType > 0) {
                    val startDate = Utils.getTimeInMilliseconds(it.StartDate!!)
                    val endDate = Utils.getTimeInMilliseconds(it.EndDate!!)
                    if (startDate > timethreshold) {
                        list2.add(BGData(startDate - timethreshold, MaterialColors.getColor(
                            requireContext(),
                            R.attr.colorSurfaceContainerHighest,
                            resources.getColor(R.color.black,null)
                        )))
                        timethreshold = endDate.toFloat()
                    }
                    list2.add(BGData((endDate - startDate).toFloat(), Color.parseColor(it.capaColor)))
                    timethreshold = endDate.toFloat()
                }
            }
        }

        if (timethreshold < msPerDay) {
            list2.add(
                BGData(msPerDay - timethreshold,MaterialColors.getColor(
                    requireContext(),
                    R.attr.colorSurfaceContainerHighest,
                    resources.getColor(R.color.black,null)
                ) ))
        }

        //list2.add(BGData(10f, Color.rgb(255, 165, 0)))

        binding.attendanceBarplotView.setData(list2)
    }
}