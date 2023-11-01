package com.timo.timoterminal.fragmentViews

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.journeyapps.barcodescanner.Util
import com.timo.timoterminal.databinding.FragmentAttendanceBinding
import com.timo.timoterminal.modalBottomSheets.MBSheetFingerprintCardReader
import com.timo.timoterminal.service.HttpService
import com.timo.timoterminal.utils.Utils
import com.timo.timoterminal.viewModel.AttendanceFragmentViewModel
import com.zkteco.android.core.interfaces.FingerprintListener
import com.zkteco.android.core.interfaces.RfidListener
import com.zkteco.android.core.sdk.service.FingerprintService
import com.zkteco.android.core.sdk.service.RfidService
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.abs

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class AttendanceFragment : Fragment(), RfidListener, FingerprintListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var lastClick : Long = 0
    private lateinit var binding: FragmentAttendanceBinding
    private val httpService: HttpService = HttpService()
    private val viewModel: AttendanceFragmentViewModel by viewModel()
    private var funcCode = -1
    private lateinit var mbSheetFingerprintCardReader : MBSheetFingerprintCardReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        mbSheetFingerprintCardReader = MBSheetFingerprintCardReader()

        setOnClickListeners()
        adaptLottieAnimationTime()
        setText()
        return binding.root
    }

    private fun setText() {
        val gc = GregorianCalendar()
        binding.textViewDateTimeViewContainer.text = Utils.getDateWithNameFromGC(gc)
        binding.textViewTimeTimeViewContainer.text = Utils.getTimeFromGC(gc)
    }

    override fun onResume() {
        super.onResume()
        adaptLottieAnimationTime()
    }

    // remove listener on pause
    override fun onPause() {
        RfidService.unregister()
        //binding.textViewAttendanceState.text = "Keine Anwesenheit ausgewählt"

        super.onPause()
    }

    // set booking code and start listening
    private fun setOnClickListeners() {
        binding.buttonKommen.setOnClickListener {
            if(abs(lastClick-SystemClock.elapsedRealtime()) > 500){
                funcCode = 100
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
        binding.buttonPauseAnfang.setOnClickListener {
            if(abs(lastClick-SystemClock.elapsedRealtime()) > 500){
                funcCode = 110
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
        binding.buttonPauseEnde.setOnClickListener {
            if(abs(lastClick-SystemClock.elapsedRealtime()) > 500){
                funcCode = 210
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
        binding.buttonGehen.setOnClickListener{
            if(abs(lastClick-SystemClock.elapsedRealtime()) > 500){
                funcCode = 200
                executeClick()
                lastClick = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun executeClick() {
        setListener()
        val bundle = Bundle()
        bundle.putInt("status", funcCode)
        mbSheetFingerprintCardReader.arguments = bundle
        mbSheetFingerprintCardReader.show(parentFragmentManager, MBSheetFingerprintCardReader.TAG)
    }

    // start listening to card reader
    private fun setListener(){
        RfidService.setListener(this)
        RfidService.register()
        FingerprintService.setListener(this)
        FingerprintService.register()
    }

    // send all necessary information to timo to create a booking
    private fun sendBooking(card :String ,inputCode : Int) {
        val dateFormatter  = SimpleDateFormat("dd.MM.yyyy HH:mm:ss")
        viewModel.viewModelScope.launch {
            val url = viewModel.getURl()
            val company = viewModel.getCompany()
            val terminalId = viewModel.getTerminalID()
            if (!company.isNullOrEmpty() && !terminalId.isNullOrEmpty()) {
                httpService.post(
                    "${url}services/rest/zktecoTerminal/booking",
                    mapOf(
                        Pair("card", card),
                        Pair("firma", company),
                        Pair("date", dateFormatter.format(Date())),
                        Pair("funcCode", "$funcCode"),
                        Pair("inputCode", "$inputCode"),
                        Pair("terminalId", terminalId)
                    ),
                    requireContext(),
                    { obj, _, _ ->
                        if (obj != null) {
                            Snackbar.make(binding.root,obj.getString("message"),Snackbar.LENGTH_LONG).show()
                        }
                    }
                )
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AttendanceFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AttendanceFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // get code of scanned card
    override fun onRfidRead(rfidInfo: String) {
        val rfidCode = rfidInfo.toLongOrNull(16)
        mbSheetFingerprintCardReader.dismiss()
        if (rfidCode != null) {
            var oct = rfidCode.toString(8)
            while (oct.length < 9) {
                oct = "0$oct"
            }
            oct = oct.reversed()
            if(funcCode > 0) {
//                sendBooking(oct,2)
                funcCode = -1
                RfidService.unregister()
                //binding.textViewAttendanceState.text = "Keine Anwesenheit ausgewählt"
            }
        }
    }

    override fun onFingerprintPressed(
        fingerprint: String,
        template: String,
        width: Int,
        height: Int
    ) {
        Log.d("FP", fingerprint)
        mbSheetFingerprintCardReader.addTextView("1234556")
        mbSheetFingerprintCardReader.dismiss()
        FingerprintService.unregister()
    }

    private fun adaptLottieAnimationTime() {
        super.onResume()
        val calendar: Calendar = Calendar.getInstance()
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY) +4

        if ( hour < 7 || hour > 20) {
            binding.lottieAnimationView.setMinAndMaxFrame(0, 60)
            binding.lottieAnimationView.playAnimation()
        } else {
            binding.lottieAnimationView.setMinAndMaxFrame(60, 120)
            binding.lottieAnimationView.playAnimation()
        }
    }
}